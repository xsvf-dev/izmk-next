package ovo.xsvf;

import com.allatori.annotations.DoNotRename;
import com.google.gson.JsonObject;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@DoNotRename
public class ServiceMain {
    private static final File log = new File("izmk-loader.log");
    private static final File errorLog = new File("izmk-loader-error.log");

    private static final File self = new File(ServiceMain.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    private static final Set<String> pids = new HashSet<>();
    private static final AtomicBoolean running = new AtomicBoolean(true);
    
    // Loading phases to track progress
    private static final String[] LOADING_PHASES = {
        "Initializing",
        "Searching for game process",
        "Attaching to game",
        "Loading classes",
        "Extracting resources",
        "Setting up class loader",
        "Starting IZMK"
    };
    
    private static int currentPhase = 0;

    static {
        try {
            if (!log.exists() && !log.createNewFile())
                throw new IOException("无法创建日志文件：" + log.getAbsolutePath());
            if (!errorLog.exists() && !errorLog.createNewFile())
                throw new IOException("无法创建错误日志文件：" + errorLog.getAbsolutePath());
            System.setOut(new PrintStream(new FileOutputStream(log, true)));
            System.setErr(new PrintStream(new FileOutputStream(errorLog, true)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @DoNotRename
    public static void main(String[] args) throws Exception {
        // Initialize and show loading UI
        LoadingUI loadingUI = LoadingUI.getInstance();
        loadingUI.setStage("Finding Minecraft...");
        loadingUI.setStatus("Initializing loader");
        loadingUI.setIndeterminate(true);
        loadingUI.show();
        
        // Short delay to allow UI to appear
        TimeUnit.MILLISECONDS.sleep(500);
        
        // Update status to searching
        loadingUI.setStatus("Searching for game process");
        
        while (running.get()) {
            List<VirtualMachineDescriptor> list = VirtualMachine.list();

            // Clean up PIDs of processes that no longer exist
            pids.removeIf(pid -> list.stream().map(VirtualMachineDescriptor::id).noneMatch(it -> it.equals(pid)));

            // Look for target process and attach
            list.stream()
                .filter(it -> !pids.contains(it.id()) && it.displayName().startsWith("cpw.mods.bootstraplauncher.BootstrapLauncher"))
                .findFirst()
                .ifPresent(vmd -> {
                    try {
                        System.out.println("Found Minecraft (" + vmd.id() + ") " + vmd.displayName());
                        loadingUI.setStage("Loading IZMK...");
                        loadingUI.setStatus("Attaching to game process");
                        loadingUI.setIndeterminate(false);
                        currentPhase = 2; // Attaching phase
                        updateProgress(loadingUI);
                        
                        // Attach to the game and inject our agent
                        if (attach(vmd.id(), buildLaunchArgs())) {
                            // Successfully attached
                            pids.add(vmd.id());
                            
                            // Final progress update
                            currentPhase = LOADING_PHASES.length;
                            updateProgress(loadingUI);
                            System.out.println("Successfully loaded IZMK!");
                        }
                    } catch (Exception e) {
                        System.err.println("Error during loading: " + e.getMessage());
                        e.printStackTrace(System.err);
                    }
                });

            // Wait before checking again
            TimeUnit.SECONDS.sleep(1);
        }
    }

    private static JsonObject buildLaunchArgs() {
        JsonObject launchArgs = new JsonObject();
        launchArgs.addProperty("file", self.getAbsolutePath());
        return launchArgs;
    }

    private static boolean attach(String pid, JsonObject launchArgs) {
        LoadingUI loadingUI = LoadingUI.getInstance();
        try {
            VirtualMachine vm = VirtualMachine.attach(pid);
            loadingUI.setStatus("Loading agent into game");
            currentPhase = 3; // Loading classes phase
            updateProgress(loadingUI);
            
            vm.loadAgent(self.getAbsolutePath(), launchArgs.toString());
            System.out.println("Successfully loaded IZMK into process " + pid);
            
            // Dispose of the UI after successful load
            loadingUI.dispose();
            return true;
        } catch (Exception e) {
            System.err.println("Failed to load IZMK into process " + pid + ": " + e.getMessage());
            e.printStackTrace(System.err);
            
            // Show error in the UI but don't exit
            loadingUI.setStage("Failed to load");
            loadingUI.setStatus("Error: " + e.getMessage());
            loadingUI.setError(true);
            
            return false;
        }
    }
    
    private static void updateProgress(LoadingUI ui) {
        // Calculate progress percentage based on current phase
        int progressValue = (int)((currentPhase / (float)LOADING_PHASES.length) * 100);
        ui.setProgress(progressValue);
        
        if (currentPhase < LOADING_PHASES.length) {
            ui.setStatus(LOADING_PHASES[currentPhase]);
        }
    }
}
