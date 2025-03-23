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
        while (running.get()) {
            System.out.println("Waiting for HeyPixel...");
            List<VirtualMachineDescriptor> list = VirtualMachine.list();

            // 清理已不存在的进程ID
            pids.removeIf(pid -> list.stream().map(VirtualMachineDescriptor::id).noneMatch(it -> it.equals(pid)));

            // 查找目标进程并附加
            list.stream().filter(it -> !pids.contains(it.id()) && it.displayName().startsWith("cpw.mods.bootstraplauncher.BootstrapLauncher")).findFirst().ifPresent(vmd -> {
                System.out.println("Attach to (" + vmd.id() + ") " + vmd.displayName());
                attach(vmd.id(), buildLaunchArgs());
                pids.add(vmd.id());
            });

            // 等待一秒再检查
            TimeUnit.SECONDS.sleep(1);
        }
    }

    private static JsonObject buildLaunchArgs() {
        JsonObject launchArgs = new JsonObject();
        launchArgs.addProperty("file", self.getAbsolutePath());
        return launchArgs;
    }

    private static void attach(String pid, JsonObject launchArgs) {
        try {
            VirtualMachine vm = VirtualMachine.attach(pid);
            vm.loadAgent(self.getAbsolutePath(), launchArgs.toString());
            System.out.println("成功加载IZMK到进程 " + pid);
        } catch (Exception e) {
            System.err.println("无法加载IZMK到进程 " + pid + ": " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}
