package ovo.xsvf;

import com.allatori.annotations.DoNotRename;
import com.google.gson.JsonObject;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import ovo.xsvf.ui.LoadingUI;

import java.io.*;
import java.util.Arrays;
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
    private static final File library = self.toPath().resolveSibling("izmk-lib.dll").toFile();
    private static final File mapping = self.toPath().resolveSibling("mapping.srg").toFile();
    private static final Set<String> pids = new HashSet<>();
    private static final AtomicBoolean running = new AtomicBoolean(true);
    private static LoadingUI ui;
    private static boolean showConsole = false;

    // Progress stages
    private static final String[] STAGES = {
        "Initializing",
        "Extracting Resources",
        "Attaching to Game",
        "Loading IZMK Core",
        "Finalizing"
    };

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
        
        // Show the loading UI
        ui = LoadingUI.display();
        ui.setStatus("Starting IZMK Loader...");
        
        // Main application loop
        while (running.get()) {
            if (!ui.isVisible()) {
                System.exit(0);
            }
            if (!ui.hasError()) {
                ui.setStatus("Waiting for Minecraft...");
            }
            List<VirtualMachineDescriptor> list = VirtualMachine.list();

            // Clean up PIDs of processes that no longer exist
            pids.removeIf(pid -> list.stream().map(VirtualMachineDescriptor::id).noneMatch(it -> it.equals(pid)));

            // Find and attach to target process
            list.stream()
                .filter(it -> !pids.contains(it.id()) && it.displayName().startsWith("cpw.mods.bootstraplauncher.BootstrapLauncher"))
                .findFirst()
                .ifPresent(vmd -> {
                    ui.setStatus("Found Minecraft (PID: " + vmd.id() + ")");
                    ui.startProgress();
                    ui.setStage(STAGES[0]);
                    ui.setProgress(0.1f);
                    
                    try {
                        Thread.sleep(300); // Small delay for UI update
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    
                    attach(vmd.id(), buildLaunchArgs());
                    pids.add(vmd.id());
                });

            // Wait before checking again
            TimeUnit.SECONDS.sleep(1);
        }
    }

    private static boolean extractResources() {
        ui.setStage(STAGES[1]);
        ui.setProgress(0.3f);
        ui.setStatus("Extracting IZMK resources...");
        
        try {
            // Extract library
            try (FileOutputStream fos = new FileOutputStream(library);
                 InputStream is = ServiceMain.class.getResourceAsStream("/lib.dll")) {
                if (is == null) {
                    ui.setStatus("Error: Library resource not found");
                    System.err.println("IZMK 资源文件lib.dll未找到！");
                    return false;
                }
                fos.write(is.readAllBytes());
                ui.setProgress(0.4f);
            }
            
            // Extract mapping
            try (FileOutputStream fos = new FileOutputStream(mapping);
                 InputStream is = ServiceMain.class.getResourceAsStream("/mapping.srg")) {
                if (is == null) {
                    ui.setStatus("Error: Mapping resource not found");
                    System.err.println("IZMK 资源文件mapping.srg未找到！");
                    return false;
                }
                fos.write(is.readAllBytes());
                ui.setProgress(0.5f);
            }
            
            return true;
        } catch (IOException e) {
            ui.setStatus("Error extracting resources: " + e.getMessage());
            System.err.println("无法解压资源文件 " + library + " 或 " + mapping + ": " + e.getMessage());
            e.printStackTrace(System.err);
            return false;
        }
    }

    private static JsonObject buildLaunchArgs() {
        JsonObject launchArgs = new JsonObject();
        launchArgs.addProperty("dll", library.getAbsolutePath());
        launchArgs.addProperty("mapping", mapping.getAbsolutePath());
        launchArgs.addProperty("file", self.getAbsolutePath());
        return launchArgs;
    }

    private static void attach(String pid, JsonObject launchArgs) {
        try {
            // Extract resources stage
            if (!extractResources()) {
                ui.completeWithError("Failed to extract resources");
                System.err.println("无法加载IZMK，请检查日志文件。");
                return;
            }
            
            // Attaching stage
            ui.setStage(STAGES[2]);
            ui.setProgress(0.6f);
            ui.setStatus("Attaching to Minecraft...");
            
            try {
                Thread.sleep(300); // Small delay for UI transition
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            VirtualMachine vm = VirtualMachine.attach(pid);
            
            // Loading core stage
            ui.setStage(STAGES[3]);
            ui.setProgress(0.8f);
            ui.setStatus("Loading IZMK Core...");
            
            vm.loadAgent(self.getAbsolutePath(), launchArgs.toString());
            
            // Finalizing stage
            ui.setStage(STAGES[4]);
            ui.setProgress(0.95f);
            
            try {
                Thread.sleep(300); // Small delay for UI transition
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            ui.completeWithSuccess("Successfully loaded IZMK!");
            System.out.println("成功加载IZMK到进程 " + pid);
            
        } catch (Exception e) {
            ui.completeWithError("Error: " + e.getMessage());
            System.err.println("无法加载IZMK到进程 " + pid + ": " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}
