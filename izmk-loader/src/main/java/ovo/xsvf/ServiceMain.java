package ovo.xsvf;

import com.allatori.annotations.DoNotRename;
import com.google.gson.JsonObject;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.Winsvc;
import com.sun.jna.platform.win32.Winsvc.SERVICE_TABLE_ENTRY;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@DoNotRename
public class ServiceMain {
    private static final File log = new File("izmk-loader.log");
    private static final File errorLog = new File("izmk-loader-error.log");

    private static final File self = new File(ServiceMain.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    private static final File library = self.toPath().resolveSibling("izmk-lib.dll").toFile();
    private static final File mapping = self.toPath().resolveSibling("mapping.srg").toFile();
    private static final User32 user32 = Native.load("user32", User32.class);
    private static final Set<String> pids = new HashSet<>();
    private static final AtomicBoolean running = new AtomicBoolean(true);
    private static final CountDownLatch serviceLatch = new CountDownLatch(1);
    private static final Winsvc.SERVICE_STATUS serviceStatus = new Winsvc.SERVICE_STATUS();
    // 服务状态相关变量
    private static Winsvc.SERVICE_STATUS_HANDLE serviceStatusHandle = null;
    private static Thread monitorThread = null;

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
    public static void main(String[] args) {
        System.out.println("IZMK服务启动，参数: " + String.join(" ", args));

        // 检查是否作为服务运行
        if (args.length > 0 && args[0].equals("-service")) {
            System.out.println("以服务模式启动");
            startAsService();
        } else {
            System.out.println("以普通模式启动");
            startMonitorThread();
            // 保持主线程运行但不阻塞
            try {
                serviceLatch.await();
            } catch (InterruptedException e) {
                System.err.println("主线程被中断: " + e.getMessage());
            }
        }
    }

    private static void startAsService() {
        SERVICE_TABLE_ENTRY entry = new SERVICE_TABLE_ENTRY();
        entry.lpServiceName = "izmk-loader";
        entry.lpServiceProc = (dwArgc, lpszArgv) -> startService();

        SERVICE_TABLE_ENTRY[] entries = (SERVICE_TABLE_ENTRY[]) entry.toArray(2);
        Advapi32.INSTANCE.StartServiceCtrlDispatcher(entries);
    }

    private static void startService() {
        // 注册服务控制处理器
        serviceStatusHandle = Advapi32.INSTANCE.RegisterServiceCtrlHandlerEx("izmk-loader", (dwControl, dwEventType, lpEventData, lpContext) -> switch (dwControl) {
            case Winsvc.SERVICE_CONTROL_STOP, Winsvc.SERVICE_CONTROL_SHUTDOWN -> {
                stopService();
                yield 0;
            }
            case Winsvc.SERVICE_CONTROL_INTERROGATE -> {
                updateServiceStatus(serviceStatus.dwCurrentState);
                yield 0;
            }
            default -> 0;
        }, null);

        // 初始化服务状态
        serviceStatus.dwServiceType = WinNT.SERVICE_WIN32_OWN_PROCESS;
        serviceStatus.dwControlsAccepted = Winsvc.SERVICE_ACCEPT_STOP | Winsvc.SERVICE_ACCEPT_SHUTDOWN;
        serviceStatus.dwWin32ExitCode = 0;
        serviceStatus.dwServiceSpecificExitCode = 0;
        serviceStatus.dwCheckPoint = 0;

        // 通知SCM服务正在启动
        updateServiceStatus(Winsvc.SERVICE_START_PENDING);

        try {
            // 启动监视线程
            startMonitorThread();

            // 通知SCM服务已启动
            updateServiceStatus(Winsvc.SERVICE_RUNNING);

            // 等待服务停止信号
            serviceLatch.await();
        } catch (Exception e) {
            System.err.println("服务启动失败: " + e.getMessage());
            e.printStackTrace();
            updateServiceStatus(Winsvc.SERVICE_STOPPED);
        }
    }

    private static void startMonitorThread() {
        // 提取资源文件
        if (!(library.exists() && mapping.exists()) && !extractLibrary()) {
            System.err.println("无法加载所需的资源文件");
            return;
        }

        // 创建并启动监视线程
        monitorThread = new Thread(() -> {
            System.out.println("监视线程启动");
            while (running.get()) {
                try {
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
                } catch (InterruptedException e) {
                    System.out.println("监视线程被中断");
                    break;
                } catch (Exception e) {
                    System.err.println("监视线程异常: " + e.getMessage());
                    e.printStackTrace();
                    try {
                        // 遇到异常时等待更长时间再继续
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException ignored) {
                        break;
                    }
                }
            }
            System.out.println("监视线程结束");
        });

        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    private static void stopService() {
        System.out.println("正在停止服务...");
        updateServiceStatus(Winsvc.SERVICE_STOP_PENDING);

        running.set(false);
        if (monitorThread != null) {
            monitorThread.interrupt();
            try {
                // 给线程一点时间优雅地关闭
                monitorThread.join(3000);
            } catch (InterruptedException ignored) {
            }
        }

        // 通知等待的主线程
        serviceLatch.countDown();

        // 通知SCM服务已停止
        updateServiceStatus(Winsvc.SERVICE_STOPPED);
        System.out.println("服务已停止");
    }

    private static void updateServiceStatus(int currentState) {
        serviceStatus.dwCurrentState = currentState;
        serviceStatus.dwCheckPoint = (currentState == Winsvc.SERVICE_RUNNING || currentState == Winsvc.SERVICE_STOPPED) ? 0 : serviceStatus.dwCheckPoint + 1;
        serviceStatus.dwWaitHint = (currentState == Winsvc.SERVICE_RUNNING || currentState == Winsvc.SERVICE_STOPPED) ? 0 : 3000;

        if (serviceStatusHandle != null) {
            Advapi32.INSTANCE.SetServiceStatus(serviceStatusHandle, serviceStatus);
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
        try (FileOutputStream fos = new FileOutputStream(library); FileOutputStream mappingFos = new FileOutputStream(mapping); InputStream is = ServiceMain.class.getResourceAsStream("/lib.dll"); InputStream mappingIs = ServiceMain.class.getResourceAsStream("/mapping.srg")) {
            if (is == null || mappingIs == null) {
                user32.showMessage("IZMK 资源文件未找到！", "错误", User32.MB_ICONERROR);
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
            System.err.println("无法加载IZMK到进程 " + pid + ": " + e.getMessage());
            e.printStackTrace();
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
