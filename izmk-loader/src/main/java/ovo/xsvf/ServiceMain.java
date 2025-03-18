package ovo.xsvf;

import com.allatori.annotations.DoNotRename;
import com.google.gson.JsonObject;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@DoNotRename
public class ServiceMain {
    private static final File log = new File("izmk-loader.log");
    private static final File errorLog = new File("izmk-loader-error.log");

    private static final File self = new File(ServiceMain.class
            .getProtectionDomain().getCodeSource().getLocation().getPath());
    private static final File library = self.toPath().resolveSibling("izmk-lib.dll").toFile();
    private static final File mapping = self.toPath().resolveSibling("mapping.srg").toFile();
    private static final User32 user32 = Native.load("user32", User32.class);
    private static final Set<String> pids = new HashSet<>();

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
    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) {
        while (true) {
            System.out.println("Waiting for HeyPixel...");
            List<VirtualMachineDescriptor> list = VirtualMachine.list();
            pids.forEach(pid -> {
                if (list.stream().map(VirtualMachineDescriptor::id).noneMatch(it -> it.equals(pid)))
                    pids.remove(pid);
            });
            list.stream()
                    .filter(it -> !pids.contains(it.id()) &&
                                    it.displayName().startsWith("cpw.mods.bootstraplauncher.BootstrapLauncher")
//                                   && it.displayName().contains("pc.bjd-mc.com")
                    )
                    .findFirst()
                    .ifPresentOrElse(vmd -> {
                        System.out.println("Attach to (" + vmd.id() + ") " + vmd.displayName());
                        if (!library.exists() && !extractLibrary()) {
                            showError(new FileNotFoundException("无法加载 DLL 库文件 " + library), "错误");
                            return;
                        }
                        attach(vmd.id(), buildLaunchArgs());
                        pids.add(vmd.id());
                    }, () -> {
                    });
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private static void showError(Throwable e, String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(msg).append("：").append(e.getMessage());
        for (StackTraceElement ste : e.getStackTrace()) {
            sb.append("\n at ").append(ste.toString());
        }
        user32.showMessage(sb.toString(), "错误", User32.MB_ICONERROR);
    }

    private static boolean extractLibrary() {
        try (FileOutputStream fos = new FileOutputStream(library);
             FileOutputStream mappingFos = new FileOutputStream(mapping);
             InputStream is = ServiceMain.class.getResourceAsStream("/lib.dll");
             InputStream mappingIs = ServiceMain.class.getResourceAsStream("/mapping.srg")) {
            if (is == null || mappingIs == null) {
                user32.showMessage("IZMK 资源文件未找到！", "错误", ServiceMain.User32.MB_ICONERROR);
                return false;
            }
            fos.write(is.readAllBytes());
            mappingFos.write(mappingIs.readAllBytes());
            return true;
        } catch (IOException e) {
            showError(e, "无法解压资源文件 " + library + " 或 " + mapping);
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
            VirtualMachine vm = VirtualMachine.attach(pid);
            vm.loadAgent(self.getAbsolutePath(), launchArgs.toString());
        } catch (Exception e) {
            showError(e, "无法加载 IZMK");
        }
    }

    public interface User32 extends Library {
        int MB_ICONERROR = 0x00000010;

        int MessageBoxW(int hWnd, WString lpText, WString lpCaption, int uType);

        default int showMessage(String text, String caption, int type) {
            return MessageBoxW(0, new WString(text), new WString(caption), type);
        }
    }
}
