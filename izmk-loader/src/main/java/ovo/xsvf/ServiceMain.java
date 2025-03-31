package ovo.xsvf;

import com.google.gson.JsonObject;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import ovo.xsvf.common.status.Status;
import ovo.xsvf.common.status.StatusReporter;
import ovo.xsvf.common.status.StatusServer;
import ovo.xsvf.ui.LoadingUI;
import ovo.xsvf.ui.TrayIconGenerator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class ServiceMain {
    public static final int STATUS_SERVER_PORT = 8080;
    private static final File log = new File("izmk-loader.log");
    private static final File errorLog = new File("izmk-loader-error.log");

    private static final File self = new File(ServiceMain.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    private static final AtomicBoolean running = new AtomicBoolean(true);
    // 存储已加载的JVM进程ID
    private static final Set<String> injectedPids = new HashSet<>();
    // Loading phases to track progress
    private static final String[] LOADING_PHASES = {
            "初始化中",
            "搜索游戏进程",
            "连接到游戏",
            "加载类文件",
            "提取资源文件",
            "设置类加载器",
            "启动 IZMK"
    };
    // 设置全局字体
    private static final Font CHINESE_FONT = new Font("Microsoft YaHei", Font.PLAIN, 12);
    private static TrayIcon trayIcon;
    private static boolean showUiOnDetection = true;
    private static int currentPhase = 0;

    static {
        try {
            if (!log.exists() && !log.createNewFile())
                throw new IOException("无法创建日志文件：" + log.getAbsolutePath());
            if (!errorLog.exists() && !errorLog.createNewFile())
                throw new IOException("无法创建错误日志文件：" + errorLog.getAbsolutePath());
            System.setOut(new PrintStream(new FileOutputStream(log, true)));
            System.setErr(new PrintStream(new FileOutputStream(errorLog, true)));

            // 设置全局字体
            setGlobalFont();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置全局字体，使中文正常显示
     */
    private static void setGlobalFont() {
        // 设置字体时尝试获取系统上的中文字体
        Font chineseFont = getChineseFont();

        // 设置UIManager中的所有字体
        UIManager.put("Button.font", chineseFont);
        UIManager.put("Label.font", chineseFont);
        UIManager.put("Menu.font", chineseFont);
        UIManager.put("MenuItem.font", chineseFont);
        UIManager.put("PopupMenu.font", chineseFont);
        UIManager.put("CheckboxMenuItem.font", chineseFont);
        UIManager.put("ToolTip.font", chineseFont);
        UIManager.put("OptionPane.messageFont", chineseFont);
        UIManager.put("OptionPane.buttonFont", chineseFont);
        UIManager.put("OptionPane.font", chineseFont);
        UIManager.put("Dialog.font", chineseFont);
        UIManager.put("Panel.font", chineseFont);

        // 设置JDialog和JFrame的默认字体
        try {
            Font titleFont = chineseFont.deriveFont(Font.BOLD, 14);
            UIManager.put("InternalFrame.titleFont", titleFont);
            JDialog.setDefaultLookAndFeelDecorated(true);
        } catch (Exception e) {
            System.err.println("设置标题字体失败: " + e.getMessage());
        }
    }

    private static Font getChineseFont() {
        String[] fontFamilies = {"Microsoft YaHei", "SimSun", "NSimSun", "SimHei", "Dialog"};
        Font chineseFont = null;

        // 尝试按优先级依次获取字体
        for (String fontFamily : fontFamilies) {
            try {
                Font font = new Font(fontFamily, Font.PLAIN, 12);
                if (font.canDisplay('中') && font.canDisplay('文')) {
                    chineseFont = font;
                    break;
                }
            } catch (Exception e) {
                // 忽略异常，继续尝试下一个字体
            }
        }

        // 如果找不到支持中文的字体，就使用默认字体
        if (chineseFont == null) {
            chineseFont = CHINESE_FONT;
        }
        return chineseFont;
    }

    public static void main(String[] args) {
        // 初始化状态报告器
        initStatusServer();
        initStatusReporter();
        try {

            // 报告初始状态
            StatusReporter.report(Status.LOADER_INIT);

            // 检查系统托盘支持
            if (!SystemTray.isSupported()) {
                System.out.println("系统托盘不受支持，将直接启动UI界面");
                startWithUi(args.length > 0 && args[0].equals("dev"));
                return;
            }

            // 创建托盘图标
            setupTrayIcon();

            // 启动后台扫描线程
            startBackgroundScanner(args.length > 0 && args[0].equals("dev"));
        } catch (Throwable t) {
            // 报告错误状态
            try {
                StatusReporter.report(Status.ERROR_GENERAL);
            } catch (Exception ignored) {
                // 忽略状态报告错误
            }

            t.printStackTrace(System.err);
        }
    }

    private static void setupTrayIcon() {
        try {
            SystemTray tray = SystemTray.getSystemTray();

            // 创建托盘图标的图像
            Image image = null;

            // 尝试从资源加载图标
            try {
                java.net.URL iconUrl = ServiceMain.class.getResource("/icon.png");
                if (iconUrl != null) {
                    image = Toolkit.getDefaultToolkit().createImage(iconUrl);
                    System.out.println("从资源加载托盘图标: " + iconUrl);
                } else {
                    System.out.println("未找到托盘图标资源文件，将生成默认图标");
                }
            } catch (Exception e) {
                System.err.println("加载托盘图标资源时出错: " + e.getMessage());
            }

            // 如果没有图标文件，使用TrayIconGenerator生成
            if (image == null) {
                image = TrayIconGenerator.generateIcon();
                System.out.println("已生成默认托盘图标");

                // 可选：保存生成的图标以便调试
                try {
                    TrayIconGenerator.saveIconToFile(image, "izmk-tray-icon.png");
                    System.out.println("已保存生成的图标到: izmk-tray-icon.png");
                } catch (Exception e) {
                    System.err.println("保存生成的图标时出错: " + e.getMessage());
                }
            }

            // 创建弹出菜单 - 使用英文避免方块显示问题
            PopupMenu popup = getPopupMenu();

            // 创建托盘图标
            trayIcon = new TrayIcon(image, "IZMK-Next", popup);
            trayIcon.setImageAutoSize(true);

            // 单击托盘图标切换UI显示状态（显示/隐藏）
            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
                        // 单击切换UI显示状态
                        toggleUi();
                    }
                }
            });

            // 添加托盘图标到系统托盘
            tray.add(trayIcon);
            trayIcon.displayMessage("IZMK-Next", "正在后台运行中...", TrayIcon.MessageType.INFO);

        } catch (Exception e) {
            System.err.println("创建托盘图标出错: " + e.getMessage());
            e.printStackTrace(System.err);

            // 如果托盘创建失败，回退到UI模式
            showUi();
        }
    }

    private static PopupMenu getPopupMenu() {
        PopupMenu popup = new PopupMenu();

        // 由于系统托盘菜单在某些Windows版本上不支持非英文字符，使用英文

        // 添加菜单项 - 显示UI
        MenuItem showItem = new MenuItem("Show/Hide Interface");
        showItem.setFont(CHINESE_FONT);
        showItem.addActionListener(e -> {
            // 切换UI显示状态
            toggleUi();
        });
        popup.add(showItem);

        // 添加菜单项 - 切换自动显示
        CheckboxMenuItem autoShowItem = new CheckboxMenuItem("Auto Show on Game Detected", showUiOnDetection);
        autoShowItem.setFont(CHINESE_FONT);
        autoShowItem.addItemListener(e -> showUiOnDetection = autoShowItem.getState());
        popup.add(autoShowItem);

        popup.addSeparator();

        // 添加退出选项
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setFont(CHINESE_FONT);
        exitItem.addActionListener(e -> {
            running.set(false);
            SystemTray.getSystemTray().remove(trayIcon);
            System.exit(0);
        });
        popup.add(exitItem);
        return popup;
    }

    private static void startBackgroundScanner(boolean dev) {
        Thread scannerThread = new Thread(() -> {
            try {
                while (running.get()) {
                    scanAndAttach(false, dev);
                    // 保持固定扫描间隔，无论是否加载成功
                    TimeUnit.SECONDS.sleep(2);
                }
            } catch (InterruptedException e) {
                System.out.println("后台扫描线程被中断");
            }
        }, "IZMK-Scanner");

        // 修改：将守护线程改为非守护线程，确保程序不会自动退出
        scannerThread.setDaemon(false);
        scannerThread.start();
    }

    /**
     * 切换UI显示状态
     * 如果UI当前可见，则隐藏
     * 如果UI当前不可见，则显示
     */
    private static void toggleUi() {
        LoadingUI ui = LoadingUI.getInstance();
        if (ui.isVisible()) {
            // 如果界面当前可见，则隐藏
            ui.setVisible(false);
        } else {
            // 如果界面当前不可见，则显示
            SwingUtilities.invokeLater(() -> {
                ui.setStage("准备就绪");
                ui.setStatus("搜索游戏进程...");
                ui.setIndeterminate(true);
                ui.setVisible(true);
            });
        }
    }

    private static void showUi() {
        SwingUtilities.invokeLater(() -> {
            LoadingUI ui = LoadingUI.getInstance();
            ui.setStage("准备就绪");
            ui.setStatus("搜索游戏进程...");
            ui.setIndeterminate(true);
            ui.setVisible(true);
        });
    }

    private static void startWithUi(boolean dev) {
        // 显示加载界面
        LoadingUI loadingUI = LoadingUI.getInstance();
        loadingUI.setStage("寻找 Minecraft...");
        loadingUI.setStatus("初始化加载器");
        loadingUI.setIndeterminate(true);
        loadingUI.setVisible(true);

        // 启动扫描线程
        Thread scannerThread = new Thread(() -> {
            try {
                while (running.get()) {
                    scanAndAttach(true, dev);
                    // 保持固定扫描间隔为2秒
                    TimeUnit.SECONDS.sleep(2);
                }
            } catch (InterruptedException e) {
                System.out.println("扫描线程被中断");
            }
        }, "IZMK-UI-Scanner");

        // 确保扫描线程为非守护线程，防止程序自动退出
        scannerThread.setDaemon(false);
        scannerThread.start();
    }

    private static boolean scanAndAttach(boolean updateUi, boolean dev) {
        // 首先检查LoadingUI是否处于成功倒计时状态，如果是则跳过
        LoadingUI ui = LoadingUI.getInstance();
        if (ui.isSuccessCountdownActive()) {
            return true; // 正在显示成功状态，跳过扫描
        }

        // 检查已加载的进程是否仍在运行
        checkInjectedProcesses();

        // 报告开始搜索状态
        StatusReporter.report(Status.LOADER_SEARCH_MINECRAFT);

        LoadingUI loadingUI = updateUi ? LoadingUI.getInstance() : null;

        if (loadingUI != null) {
            loadingUI.setStage("寻找 Minecraft...");
            loadingUI.setStatus("搜索Java进程...");
        }

        // 搜索Minecraft进程
        try {
            // 扫描Java进程列表
            List<VirtualMachineDescriptor> descriptors = VirtualMachine.list();
            Predicate<VirtualMachineDescriptor> predicate = d -> d.displayName()
                    .startsWith("cpw.mods.bootstraplauncher.BootstrapLauncher");
            if (!dev) predicate = predicate.and(d -> d.displayName().contains("pc.bjd-mc.com"));

            Optional<VirtualMachineDescriptor> optionalTarget = descriptors.stream()
                    .filter(predicate)
                    .findFirst();

            if (optionalTarget.isEmpty()) {
                // 没有找到Minecraft进程
                return false;
            }

            VirtualMachineDescriptor target = optionalTarget.get();
            String pid = target.id();

            // 检查该进程是否已经被加载
            if (injectedPids.contains(pid)) {
                System.out.println("进程 " + pid + " 已经被加载，跳过");
                return true; // 已经加载过，视为成功
            }

            // 报告找到Minecraft进程
            StatusReporter.report(Status.LOADER_FOUND_MINECRAFT);

            System.out.println("找到 Minecraft (" + pid + ") " + target.displayName());

            if (loadingUI != null) {
                loadingUI.setStatus("找到Minecraft进程: " + pid);
            }

            // 报告正在连接
            StatusReporter.report(Status.LOADER_CONNECT_PROCESS);

            if (loadingUI != null) {
                loadingUI.setStage("正在连接...");
                loadingUI.setStatus("连接到Minecraft进程...");
            }

            // 检查是否需要显示UI
            if (showUiOnDetection && !LoadingUI.getInstance().isVisible()) {
                // 如果配置为在检测到进程时显示UI，则显示
                SwingUtilities.invokeLater(() -> {
                    LoadingUI.getInstance().setStage("正在连接...");
                    LoadingUI.getInstance().setStatus("连接到Minecraft进程...");
                    LoadingUI.getInstance().setVisible(true);
                });
            }

            // 向用户通知正在加载
            if (trayIcon != null) {
                trayIcon.displayMessage("IZMK-Next", "检测到 Minecraft 进程，正在加载...", TrayIcon.MessageType.INFO);
            }

            // 构建启动参数
            JsonObject launchArgs = buildLaunchArgs();

            // 尝试附加到JVM并加载
            boolean success = attach(pid, launchArgs);

            if (success) {
                // 添加到已加载列表
                injectedPids.add(pid);

                // 报告成功
                StatusReporter.report(Status.SUCCESS);

                // 向用户通知成功
                if (trayIcon != null) {
                    trayIcon.displayMessage("IZMK-Next", "成功加载到 Minecraft!", TrayIcon.MessageType.INFO);
                }

                // 成功加载后显示成功UI
                if (loadingUI != null || LoadingUI.getInstance().isVisible()) {
                    LoadingUI.getInstance().showSuccess();
                }

                return true;
            } else {
                // 报告加载失败
                StatusReporter.report(Status.ERROR_INJECTION);

                // 加载失败，显示错误
                if (loadingUI != null) {
                    loadingUI.setError(true);
                    loadingUI.setStatus("加载失败，请检查日志");
                }

                return false;
            }
        } catch (Exception e) {
            // 报告错误
            StatusReporter.report(Status.ERROR_GENERAL);

            e.printStackTrace(System.err);

            // 出现异常，显示错误
            if (loadingUI != null) {
                loadingUI.setError(true);
                loadingUI.setStatus("发生错误: " + e.getMessage());
            }

            return false;
        }
    }

    private static JsonObject buildLaunchArgs() {
        JsonObject launchArgs = new JsonObject();
        launchArgs.addProperty("file", self.getAbsolutePath());
        // 添加状态服务器端口
        launchArgs.addProperty("statusPort", STATUS_SERVER_PORT);
        return launchArgs;
    }

    private static boolean attach(String pid, JsonObject launchArgs) {
        LoadingUI loadingUI = LoadingUI.getInstance();
        boolean uiVisible = loadingUI.isVisible();

        try {
            VirtualMachine vm = VirtualMachine.attach(pid);

            if (uiVisible) {
                loadingUI.setStatus("正在向游戏加载代理");
                currentPhase = 3; // 加载类阶段
                updateProgress(loadingUI);
            }

            vm.loadAgent(self.getAbsolutePath(), launchArgs.toString());
            System.out.println("成功将 IZMK 加载到进程 " + pid);

            return true;
        } catch (Exception e) {
            System.err.println("无法将 IZMK 加载到进程 " + pid + ": " + e.getMessage());
            e.printStackTrace(System.err);

            if (uiVisible) {
                // 在UI中显示错误，但不退出
                loadingUI.setStage("加载失败");
                loadingUI.setStatus("错误: " + e.getMessage());
                loadingUI.setError(true);
            }

            // 显示托盘通知
            if (trayIcon != null) {
                trayIcon.displayMessage("IZMK-Next", "加载失败: " + e.getMessage(), TrayIcon.MessageType.ERROR);
            }

            return false;
        }
    }

    private static void updateProgress(LoadingUI ui) {
        // 基于当前阶段计算进度百分比
        int progressValue = (int) ((currentPhase / (float) LOADING_PHASES.length) * 100);
        ui.setProgress(progressValue);

        if (currentPhase < LOADING_PHASES.length) {
            ui.setStatus(LOADING_PHASES[currentPhase]);
        }
    }

    /**
     * 初始化状态报告器
     */
    private static void initStatusReporter() {
        try {
            StatusReporter.init(STATUS_SERVER_PORT);
            System.out.println("状态报告器已初始化");
        } catch (Exception e) {
            System.err.println("初始化状态报告器失败: " + e.getMessage());
            // 继续执行，不中断程序
        }
    }

    /**
     * 初始化状态接收服务器
     */
    private static void initStatusServer() {
        try {
            StatusServer server = new StatusServer(LoadingUI.getInstance(), STATUS_SERVER_PORT);
            var statusServerThread = new Thread(server, "StatusServerThread");
            statusServerThread.setDaemon(true); // 设为守护线程，以便JVM退出时自动结束
            statusServerThread.start();
            System.out.println("状态服务器已在端口 " + STATUS_SERVER_PORT + " 启动");
        } catch (IOException e) {
            System.err.println("无法启动状态服务器: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    /**
     * 检查已加载的进程是否仍在运行
     * 如果进程不再存在，则从已加载列表中移除
     */
    private static void checkInjectedProcesses() {
        if (injectedPids.isEmpty()) {
            return; // 没有已加载的进程，直接返回
        }

        try {
            // 获取当前所有Java进程
            List<VirtualMachineDescriptor> runningVMs = VirtualMachine.list();
            Set<String> runningPids = new HashSet<>();

            // 收集所有运行中的进程ID
            for (VirtualMachineDescriptor vm : runningVMs) {
                runningPids.add(vm.id());
            }

            // 找出已加载但不再运行的进程
            Set<String> stoppedPids = new HashSet<>(injectedPids);
            stoppedPids.removeAll(runningPids);

            // 从已加载列表中移除不再运行的进程
            if (!stoppedPids.isEmpty()) {
                for (String stoppedPid : stoppedPids) {
                    System.out.println("进程 " + stoppedPid + " 已停止，从已加载列表中移除");
                    injectedPids.remove(stoppedPid);
                }
            }
        } catch (Exception e) {
            System.err.println("检查已加载进程状态时出错: " + e.getMessage());
            e.printStackTrace();
            // 出错时不影响主程序流程，继续执行
        }
    }
}
