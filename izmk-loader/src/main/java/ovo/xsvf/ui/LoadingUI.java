package ovo.xsvf.ui;

import ovo.xsvf.common.status.Status;
import ovo.xsvf.common.status.StatusListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadingUI extends JFrame implements StatusListener {
    private static final LoadingUI INSTANCE = new LoadingUI();
    // 全局字体设置，使用支持中文的字体
    private static final Font TITLE_FONT = new Font("Microsoft YaHei", Font.BOLD, 20);
    private static final Font NORMAL_FONT = new Font("Microsoft YaHei", Font.PLAIN, 12);
    private static final Font BOLD_FONT = new Font("Microsoft YaHei", Font.BOLD, 14);
    private static final Font SMALL_FONT = new Font("Microsoft YaHei", Font.PLAIN, 11);
    private final JLabel statusLabel;
    private final JLabel stageLabel;
    private final ProgressBar progressBar;
    private final AtomicBoolean indeterminateMode = new AtomicBoolean(true);
    private final AtomicBoolean errorMode = new AtomicBoolean(false);
    private final AtomicBoolean successMode = new AtomicBoolean(false);
    private final AtomicInteger progress = new AtomicInteger(0);
    private final AtomicBoolean successCountdownActive = new AtomicBoolean(false);
    private ScheduledExecutorService executor;
    private long successCountdownEndTime = 0;  // 成功倒计时结束时间

    private LoadingUI() {
        super("IZMK-Next");
        // 初始化组件
        stageLabel = new JLabel("寻找 Minecraft...");
        statusLabel = new JLabel("搜索游戏进程");
        progressBar = new ProgressBar();

        setUndecorated(true);
        setSize(400, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // 设置窗口为透明，以便阴影显示
        setBackground(new Color(0, 0, 0, 0));

        // 初始化executor
        executor = Executors.newSingleThreadScheduledExecutor();

        // 设置UI管理器的默认字体
        setUIFont();

        // 创建内容面板
        JPanel contentPanel = createMainPanel();

        // 创建带阴影效果的容器面板
        JPanel shadowContainer = new JPanel(new BorderLayout());
        shadowContainer.setOpaque(false);
        shadowContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 为阴影留出空间
        shadowContainer.add(contentPanel, BorderLayout.CENTER);

        // 设置自定义的GlassPane用于绘制阴影
        JPanel glassPane = createShadowPanel();
        setGlassPane(glassPane);
        glassPane.setVisible(true);

        // 添加内容面板到窗口
        setContentPane(shadowContainer);

        // Make window draggable
        WindowDragListener dragListener = new WindowDragListener(this);
        contentPanel.addMouseListener(dragListener);
        contentPanel.addMouseMotionListener(dragListener);

        // Add window listener to handle close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // 只隐藏窗口，不销毁它
                setVisible(false);
                // 不要关闭executor，因为我们需要保持动画状态可恢复
            }
        });

        // Start the animation
        startAnimation();

        // Set rounded corners
        setShape(new RoundRectangle2D.Double(10, 10, getWidth() - 20, getHeight() - 20, 15, 15));
    }

    public static LoadingUI getInstance() {
        return INSTANCE;
    }

    /**
     * 设置UI管理器的默认字体
     */
    private void setUIFont() {
        UIManager.put("Button.font", LoadingUI.BOLD_FONT);
        UIManager.put("Label.font", LoadingUI.BOLD_FONT);
        UIManager.put("TextField.font", LoadingUI.BOLD_FONT);
        UIManager.put("TextArea.font", LoadingUI.BOLD_FONT);
        UIManager.put("ComboBox.font", LoadingUI.BOLD_FONT);
        UIManager.put("CheckBox.font", LoadingUI.BOLD_FONT);
        UIManager.put("RadioButton.font", LoadingUI.BOLD_FONT);
        UIManager.put("Menu.font", LoadingUI.BOLD_FONT);
        UIManager.put("MenuItem.font", LoadingUI.BOLD_FONT);
        UIManager.put("OptionPane.messageFont", LoadingUI.BOLD_FONT);
        UIManager.put("OptionPane.buttonFont", LoadingUI.BOLD_FONT);
    }

    private JPanel titlePanel() {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("IZMK-Next");
        titleLabel.setForeground(new Color(220, 220, 220));
        titleLabel.setFont(TITLE_FONT);
        titlePanel.add(titleLabel, BorderLayout.WEST);

        // Close button
        JLabel closeButton = new JLabel("×");
        closeButton.setForeground(new Color(220, 220, 220));
        closeButton.setFont(TITLE_FONT);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                setVisible(false);
            }

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeButton.setForeground(new Color(255, 80, 80));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeButton.setForeground(new Color(220, 220, 220));
            }
        });
        titlePanel.add(closeButton, BorderLayout.EAST);
        return titlePanel;
    }

    public void setStatus(String status) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(status));
    }

    public void setStage(String stage) {
        SwingUtilities.invokeLater(() -> stageLabel.setText(stage));
    }

    public void setIndeterminate(boolean indeterminate) {
        indeterminateMode.set(indeterminate);
    }

    public void setProgress(int value) {
        progress.set(Math.min(100, Math.max(0, value)));
    }

    public void setError(boolean error) {
        if (error && !errorMode.get()) {
            errorMode.set(true);

            // Flash the progress bar to indicate error but don't exit
            executor.scheduleAtFixedRate(() -> {
                int newProgress = progress.get();
                if (newProgress > 15) {
                    newProgress -= 1;
                }
                setProgress(newProgress);
            }, 0, 50, TimeUnit.MILLISECONDS);

            // Update status appearance for error state
            SwingUtilities.invokeLater(() -> {
                statusLabel.setForeground(new Color(255, 80, 80));
                statusLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
            });
        }
    }

    public void showSuccess() {
        if (successMode.get()) return;

        successMode.set(true);
        indeterminateMode.set(false);
        // 设置成功状态倒计时标志为true
        successCountdownActive.set(true);

        // 计算倒计时结束时间点
        final long duration = 5000; // 5秒倒计时
        successCountdownEndTime = System.currentTimeMillis() + duration;

        // Update UI to show success
        SwingUtilities.invokeLater(() -> {
            setStage("成功!");
            setStatus("IZMK 加载成功");
            statusLabel.setForeground(new Color(50, 205, 50));
            statusLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
            setProgress(100);
        });

        // 注意：不要关闭主要的executor
        // 创建新的倒计时executor
        final ScheduledExecutorService countdownExecutor = Executors.newSingleThreadScheduledExecutor();

        // Start countdown animation (5 seconds)
        final long startTime = System.currentTimeMillis();

        countdownExecutor.scheduleAtFixedRate(() -> {
            long elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime >= duration) {
                // 倒计时完成后只隐藏UI，不退出应用
                countdownExecutor.shutdown();
                // 重置成功状态倒计时标志
                successCountdownActive.set(false);
                SwingUtilities.invokeLater(() -> {
                    setVisible(false);
                });
            } else {
                // Update progress bar based on remaining time
                float remainingPercentage = 100 * (1 - (float) elapsedTime / duration);
                setProgress((int) remainingPercentage);
            }
        }, 0, 16, TimeUnit.MILLISECONDS);
    }

    /**
     * 检查是否正在显示成功状态的倒计时
     *
     * @return 如果正在显示成功状态倒计时，返回true
     */
    public boolean isSuccessCountdownActive() {
        // 如果标志为false，直接返回false
        if (!successCountdownActive.get()) {
            return false;
        }

        // 检查倒计时是否已过期
        if (System.currentTimeMillis() > successCountdownEndTime) {
            successCountdownActive.set(false);
            return false;
        }

        return true;
    }

    private void startAnimation() {
        // 如果executor已经关闭，重新创建一个
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadScheduledExecutor();
        }

        executor.scheduleAtFixedRate(() -> SwingUtilities.invokeLater(progressBar::repaint),
                0, 16, TimeUnit.MILLISECONDS);
    }

    @Override
    public void dispose() {
        // 在实际处理dispose时，需要关闭executor
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
        super.dispose();
    }

    // 添加方法用于检查UI是否可见
    @Override
    public boolean isVisible() {
        return super.isVisible();
    }

    /**
     * 覆盖setVisible方法，在显示UI时重置状态并确保窗口置顶
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            // 确保窗口置顶显示
            setAlwaysOnTop(true);

            // 如果从隐藏状态变为显示状态，重置UI状态
            if (!isVisible()) {
                resetState();
            }
        }
        super.setVisible(visible);
    }

    /**
     * 重置UI状态以便重新使用
     * 在显示前调用这个方法可以恢复初始状态
     */
    public void resetState() {
        SwingUtilities.invokeLater(() -> {
            // 重置状态标志
            errorMode.set(false);
            successMode.set(false);
            indeterminateMode.set(true);
            progress.set(0);

            // 重置UI显示
            stageLabel.setText("准备就绪");
            statusLabel.setText("搜索游戏进程...");
            statusLabel.setForeground(new Color(140, 140, 140));
            statusLabel.setFont(NORMAL_FONT);

            // 重启动画（如果需要）
            if (executor == null || executor.isShutdown()) {
                startAnimation();
            }
        });
    }

    /**
     * 创建带有阴影效果的面板
     */
    private JPanel createShadowPanel() {
        JPanel shadowPanel = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 获取内容面板的位置和大小
                Container contentPane = getContentPane();
                Point contentLocation = contentPane.getLocation();
                Dimension contentSize = contentPane.getSize();

                // 绘制阴影
                int shadowSize = 10;
                int shadowOpacity = 60; // 透明度（0-255）

                // 设置阴影渐变
                for (int i = 0; i < shadowSize; i++) {
                    int opacity = shadowOpacity * (shadowSize - i) / shadowSize;
                    g2d.setColor(new Color(0, 0, 0, opacity));

                    // 绘制阴影边框
                    g2d.drawRoundRect(
                            contentLocation.x + shadowSize - i,
                            contentLocation.y + shadowSize - i,
                            contentSize.width - (shadowSize - i) * 2,
                            contentSize.height - (shadowSize - i) * 2,
                            15, 15
                    );
                }

                g2d.dispose();
            }
        };

        shadowPanel.setOpaque(false);
        return shadowPanel;
    }

    /**
     * 创建主要内容面板
     */
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(30, 30, 35));

        // Title panel
        JPanel titlePanel = titlePanel();
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // Center content panel
        JPanel innerContentPanel = new JPanel();
        innerContentPanel.setLayout(new BoxLayout(innerContentPanel, BoxLayout.Y_AXIS));
        innerContentPanel.setOpaque(false);

        // 设置和添加Stage indicator
        stageLabel.setForeground(new Color(180, 180, 180));
        stageLabel.setFont(BOLD_FONT);
        stageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        innerContentPanel.add(stageLabel);
        innerContentPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // 设置和添加Status indicator
        statusLabel.setForeground(new Color(140, 140, 140));
        statusLabel.setFont(NORMAL_FONT);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        innerContentPanel.add(statusLabel);
        innerContentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // 设置和添加Progress bar
        progressBar.setPreferredSize(new Dimension(370, 10));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        innerContentPanel.add(progressBar);

        mainPanel.add(innerContentPanel, BorderLayout.CENTER);

        // Footer with version
        JLabel versionLabel = new JLabel("v1.0");
        versionLabel.setForeground(new Color(100, 100, 100));
        versionLabel.setFont(SMALL_FONT);
        mainPanel.add(versionLabel, BorderLayout.SOUTH);

        return mainPanel;
    }

    /**
     * 实现StatusListener接口的方法，处理状态变化
     */
    @Override
    public void onStatusChange(Status status) {
        // 如果正在显示成功倒计时，忽略其他状态更新
        if (isSuccessCountdownActive()) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            // 根据状态类型选择对应的处理方式
            int stageCode = status.getCode() / 1000;

            // 根据状态码首位数字判断阶段
            if (stageCode == 1) {
                setStage("加载器阶段");
            } else if (stageCode == 2) {
                setStage("核心阶段");
            } else if (status.getCode() == 9000) {
                setStage("完成");
                showSuccess();
                return;
            } else if (stageCode == 9) {
                setStage("错误");
                setError(true);
            }

            // 设置详细状态描述
            setStatus(status.getDescription());
        });
    }

    // Make window draggable
    private static class WindowDragListener extends MouseAdapter {
        private final JFrame frame;
        private Point dragStart = null;

        public WindowDragListener(JFrame frame) {
            this.frame = frame;
        }

        @Override
        public void mousePressed(java.awt.event.MouseEvent e) {
            dragStart = e.getPoint();
        }

        @Override
        public void mouseReleased(java.awt.event.MouseEvent e) {
            dragStart = null;
        }

        @Override
        public void mouseDragged(java.awt.event.MouseEvent e) {
            if (dragStart != null) {
                Point currentLocation = frame.getLocation();
                frame.setLocation(
                        currentLocation.x + e.getX() - dragStart.x,
                        currentLocation.y + e.getY() - dragStart.y
                );
            }
        }
    }

    // Custom progress bar with animations
    private class ProgressBar extends JComponent {
        private static final float ANIMATION_SPEED = 0.015f;
        private float animationOffset = 0f;

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Background
            g2.setColor(new Color(45, 45, 50));
            g2.fillRect(0, 0, width, height); // Square edges

            // Foreground
            Color progressColor = errorMode.get() ?
                    new Color(220, 50, 50) :
                    successMode.get() ?
                            new Color(50, 205, 50) :
                            new Color(50, 150, 250);
            g2.setColor(progressColor);

            if (indeterminateMode.get()) {
                // Indeterminate mode - moving animation
                int barWidth = width / 4;
                animationOffset = (animationOffset + ANIMATION_SPEED) % 1.0f;

                // Calculate x position for the animation
                int xPos = (int) (width * animationOffset) - barWidth;

                // Draw two bars for seamless looping
                if (xPos + barWidth > 0)
                    g2.fillRect(xPos, 0, barWidth, height); // Square edges

                g2.fillRect(xPos + width, 0, barWidth, height); // Square edges
            } else {
                // Determinate mode
                int progressWidth = (int) (width * (progress.get() / 100.0));
                if (progressWidth > 0) {
                    g2.fillRect(0, 0, progressWidth, height); // Square edges
                }
            }

            g2.dispose();
        }
    }
}
