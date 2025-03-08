package ovo.xsvf;

import com.google.gson.JsonObject;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ServiceMain {
    private static final File self = new File(ServiceMain.class
            .getProtectionDomain().getCodeSource().getLocation().getPath());
    private static final File library = self.toPath().resolveSibling("izmk-lib.dll").toFile();
    private static final User32 user32 = Native.load("user32", User32.class);

    public static void main(String[] args) {
        String pid;
        if (args.length < 1) {
            List<VirtualMachineDescriptor> vmds = VirtualMachine.list();
            if (vmds.stream().anyMatch(vmd -> vmd.displayName().startsWith("net.minecraftforge.bootstrap.ForgeBootstrap"))) {
                pid = vmds.stream()
                        .filter(vmd -> vmd.displayName().startsWith("net.minecraftforge.bootstrap.ForgeBootstrap"))
                        .findFirst().orElseThrow().id();
            } else {
                showError(new RuntimeException("无法找到 Minecraft 进程"), "无法启动 IZMK");
                return;
            }
        } else pid = args[0];

        if (!extractLibrary()) {
            user32.showMessage("无法解压 DLL 库文件！", "错误", User32.MB_ICONERROR);
            return;
        }

        library.deleteOnExit();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!library.delete())
                user32.showMessage("无法删除 DLL 库文件！", "警告", User32.MB_ICONWARNING);
        }));

        inject(pid, buildLaunchArgs());
    }

    private static boolean extractLibrary() {
        try (FileOutputStream fos = new FileOutputStream(library);
             InputStream is = ServiceMain.class.getResourceAsStream("/lib.dll")) {
            if (is == null) {
                user32.showMessage("IZMK 资源文件未找到！", "错误", User32.MB_ICONERROR);
                return false;
            }
            fos.write(is.readAllBytes());
            return true;
        } catch (IOException e) {
            showError(e, "无法解压 DLL 库文件 " + library.getAbsolutePath());
            return false;
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

    private static JsonObject buildLaunchArgs() {
        JsonObject launchArgs = new JsonObject();
        launchArgs.addProperty("dll", library.getAbsolutePath());
        launchArgs.addProperty("file", self.getAbsolutePath());
        return launchArgs;
    }

    private static void inject(String pid, JsonObject launchArgs) {
        try {
            VirtualMachine vm = VirtualMachine.attach(pid);
            vm.loadAgent(self.getAbsolutePath(), launchArgs.toString());
        } catch (Exception e) {
            showError(e, "无法加载 IZMK");
            taskKill(pid);
        }
    }

    private static void taskKill(String pid) {
        try {
            Runtime.getRuntime().exec(new String[] {"taskkill", "/F", "/IM", "/PID", pid});
        } catch (IOException e) {
            showError(e, "无法终止进程");
        }
    }

    public interface User32 extends Library {
        int MB_ICONEXCLAMATION = 0x00000030;
        int MB_ICONWARNING = 0x00000030;
        int MB_ICONINFORMATION = 0x00000040;
        int MB_ICONASTERISK = 0x00000040;
        int MB_ICONQUESTION = 0x00000020;
        int MB_ICONSTOP = 0x00000010;
        int MB_ICONERROR = 0x00000010;
        int MB_ICONHAND = 0x00000010;

        int MessageBoxW(int hWnd, WString lpText, WString lpCaption, int uType);

        default int showMessage(String text, String caption, int type) {
            return MessageBoxW(0, new WString(text), new WString(caption), type);
        }
    }
}
