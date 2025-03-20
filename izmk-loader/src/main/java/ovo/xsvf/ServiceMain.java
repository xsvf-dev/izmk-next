package ovo.xsvf;

import com.allatori.annotations.DoNotRename;
import com.google.gson.JsonObject;
import com.sun.jna.Native;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@DoNotRename
public class ServiceMain {
    private static final String SERVICE_NAME = "izmk-loader";
    private static final File log = new File("izmk-loader.log");
    private static final File errorLog = new File("izmk-loader-error.log");

    private static final File self = new File(ServiceMain.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    private static final File library = self.toPath().resolveSibling("izmk-lib.dll").toFile();
    private static final File mapping = self.toPath().resolveSibling("mapping.srg").toFile();
    private static final Set<String> pids = new HashSet<>();
    private static final AtomicBoolean running = new AtomicBoolean(true);
    private static final Winsvc.SERVICE_STATUS serviceStatus = new Winsvc.SERVICE_STATUS();
    private static Winsvc.SERVICE_STATUS_HANDLE serviceStatusHandle = null;
    private static Thread serviceThread = null;

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
        try {
            System.out.println("IZMK服务启动，参数: " + String.join(" ", args));
            System.out.println("当前工作目录: " + System.getProperty("user.dir"));
            System.out.println("Java版本: " + System.getProperty("java.version"));
            System.out.println("Java主目录: " + System.getProperty("java.home"));
            System.out.println("JAR文件路径: " + self.getAbsolutePath());
            System.out.println("DLL文件路径: " + library.getAbsolutePath());
            System.out.println("Mapping文件路径: " + mapping.getAbsolutePath());

            // 检查是否作为服务运行
            if (args.length == 0 || !args[0].equals("-service")) {
                System.err.println("错误：此程序必须以服务方式运行");
                System.exit(1);
            }

            // 提取资源文件 - 在启动服务前提取
            if (!(library.exists() && mapping.exists())) {
                System.out.println("正在提取资源文件...");
                if (!extractLibrary()) {
                    System.err.println("无法加载所需的资源文件");
                    System.exit(1);
                }
                System.out.println("资源文件提取完成");
            }

            System.out.println("以服务模式启动");

            // 创建服务表条目
            SERVICE_TABLE_ENTRY entry = new SERVICE_TABLE_ENTRY();
            entry.lpServiceName = SERVICE_NAME;
            entry.lpServiceProc = (var1, var2) -> {
                serviceMain(var1, var2.getStringArray(var1));
            };

            SERVICE_TABLE_ENTRY[] entries = (SERVICE_TABLE_ENTRY[]) entry.toArray(2);
            System.out.println("正在启动服务控制分发器...");

            // 启动服务控制分发器
            if (!Advapi32.INSTANCE.StartServiceCtrlDispatcher(entries)) {
                int error = Native.getLastError();
                System.err.println("启动服务控制分发器失败，错误码: " + error);
                System.exit(error);
            }
        } catch (Exception e) {
            System.err.println("服务启动失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    // 服务主函数，这是Windows服务的入口点
    private static void serviceMain(int dwArgc, String[] lpszArgv) {
        System.out.println("服务主函数被调用");

        try {
            // 注册服务控制处理器
            System.out.println("正在注册服务控制处理器...");
            serviceStatusHandle = Advapi32.INSTANCE.RegisterServiceCtrlHandlerEx(SERVICE_NAME, (dwControl, dwEventType, lpEventData, lpContext) -> {
                System.out.println("收到服务控制命令: " + dwControl);
                return switch (dwControl) {
                    case Winsvc.SERVICE_CONTROL_STOP, Winsvc.SERVICE_CONTROL_SHUTDOWN -> {
                        stopService();
                        yield 0;
                    }
                    case Winsvc.SERVICE_CONTROL_INTERROGATE -> {
                        updateServiceStatus(serviceStatus.dwCurrentState);
                        yield 0;
                    }
                    default -> 0;
                };
            }, null);

            if (serviceStatusHandle == null) {
                throw new RuntimeException("服务控制处理器注册失败");
            }

            // 初始化服务状态
            serviceStatus.dwServiceType = WinNT.SERVICE_WIN32_OWN_PROCESS;
            serviceStatus.dwControlsAccepted = Winsvc.SERVICE_ACCEPT_STOP | Winsvc.SERVICE_ACCEPT_SHUTDOWN;
            serviceStatus.dwWin32ExitCode = 0;
            serviceStatus.dwServiceSpecificExitCode = 0;
            serviceStatus.dwCheckPoint = 0;

            // 通知SCM服务正在启动
            System.out.println("正在启动服务...");
            updateServiceStatus(Winsvc.SERVICE_START_PENDING);

            // 启动主服务线程
            startServiceThread();

            // 通知SCM服务已启动
            System.out.println("服务已启动");
            updateServiceStatus(Winsvc.SERVICE_RUNNING);

            // 主线程需要保持运行状态，否则服务会退出
            while (running.get()) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println("服务主函数执行失败: " + e.getMessage());
            e.printStackTrace();
            updateServiceStatus(Winsvc.SERVICE_STOPPED, WinNT.ERROR_SERVICE_SPECIFIC_ERROR, 1);
        }
    }

    private static void startServiceThread() {
        // 创建并启动监视线程
        serviceThread = new Thread(() -> {
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

        serviceThread.setName("ServiceMonitorThread");
        serviceThread.setDaemon(true);
        serviceThread.start();
        System.out.println("监视线程已启动");
    }

    private static void stopService() {
        System.out.println("正在停止服务...");
        updateServiceStatus(Winsvc.SERVICE_STOP_PENDING);

        running.set(false);

        if (serviceThread != null && serviceThread.isAlive()) {
            serviceThread.interrupt();
            try {
                // 给线程一点时间优雅地关闭
                serviceThread.join(3000);
            } catch (InterruptedException ignored) {
            }
        }

        // 通知SCM服务已停止
        updateServiceStatus(Winsvc.SERVICE_STOPPED);
        System.out.println("服务已停止");
    }

    private static void updateServiceStatus(int currentState) {
        updateServiceStatus(currentState, 0, 0);
    }

    private static void updateServiceStatus(int currentState, int win32ExitCode, int serviceSpecificExitCode) {
        try {
            serviceStatus.dwCurrentState = currentState;
            serviceStatus.dwWin32ExitCode = win32ExitCode;
            serviceStatus.dwServiceSpecificExitCode = serviceSpecificExitCode;
            serviceStatus.dwCheckPoint = (currentState == Winsvc.SERVICE_RUNNING || currentState == Winsvc.SERVICE_STOPPED) ? 0 : serviceStatus.dwCheckPoint + 1;
            serviceStatus.dwWaitHint = (currentState == Winsvc.SERVICE_RUNNING || currentState == Winsvc.SERVICE_STOPPED) ? 0 : 3000;

            if (serviceStatusHandle != null) {
                boolean result = Advapi32.INSTANCE.SetServiceStatus(serviceStatusHandle, serviceStatus);
                if (!result) {
                    int error = Native.getLastError();
                    System.err.println("更新服务状态失败，错误码: " + error);
                }
            }
        } catch (Exception e) {
            System.err.println("更新服务状态时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean extractLibrary() {
        try (FileOutputStream fos = new FileOutputStream(library); FileOutputStream mappingFos = new FileOutputStream(mapping); InputStream is = ServiceMain.class.getResourceAsStream("/lib.dll"); InputStream mappingIs = ServiceMain.class.getResourceAsStream("/mapping.srg")) {
            if (is == null || mappingIs == null) {
                System.err.println("IZMK 资源文件未找到！");
                return false;
            }
            fos.write(is.readAllBytes());
            mappingFos.write(mappingIs.readAllBytes());
            return true;
        } catch (IOException e) {
            System.err.println("无法解压资源文件 " + library + " 或 " + mapping + ": " + e.getMessage());
            e.printStackTrace();
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
            System.out.println("成功加载IZMK到进程 " + pid);
        } catch (Exception e) {
            System.err.println("无法加载IZMK到进程 " + pid + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
