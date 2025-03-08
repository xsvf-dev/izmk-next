package ovo.xsvf;

import com.google.gson.JsonObject;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.tools.attach.VirtualMachine;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class ServiceMain {
    private static final File log = new File("izmk-loader.log");
    private static final File errorLog = new File("izmk-loader-error.log");

    private static final File self = new File(ServiceMain.class
            .getProtectionDomain().getCodeSource().getLocation().getPath());
    private static final File library = self.toPath().resolveSibling("izmk-lib.dll").toFile();
    private static final User32 user32 = Native.load("user32", User32.class);

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

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) {
        while (true) {
            System.out.println("Waiting for Netease ForgeBootstrap...");
            VirtualMachine.list().stream()
                    .filter(it ->
                            it.displayName().startsWith("net.minecraftforge.bootstrap.ForgeBootstrap") &&
                                    it.displayName().contains("pc.bjd-mc.com"))
                    .findFirst()
                    .ifPresentOrElse(
                            (vmd) -> {
                                attach(vmd.id(), buildLaunchArgs());
                            }, () -> {});
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException ignored) {}
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

    private static void attach(String pid, JsonObject launchArgs) {
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
            Runtime.getRuntime().exec(new String[]{"taskkill", "/F", "/IM", "/PID", pid});
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
