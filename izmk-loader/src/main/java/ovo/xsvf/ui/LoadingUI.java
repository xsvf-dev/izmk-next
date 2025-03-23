package ovo.xsvf.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class LoadingUI extends JFrame {
    private static final int WIDTH = 300;
    private static final int HEIGHT = 120; // Increased height for stage label
    private static final Color BACKGROUND_COLOR = new Color(40, 40, 40);
    private static final Color PRIMARY_COLOR = new Color(88, 101, 242);
    private static final Color TEXT_COLOR = new Color(220, 220, 220);
    private static final Color SUCCESS_COLOR = new Color(87, 242, 135);
    private static final Color ERROR_COLOR = new Color(242, 87, 87);

    private final JPanel contentPanel;
    private final JLabel statusLabel;
    private final JLabel stageLabel;
    private final ProgressPanel progressPanel;
    private Point initialClick;
    private final AtomicReference<String> currentStatus = new AtomicReference<>("Initializing...");
    private final AtomicReference<String> currentStage = new AtomicReference<>("");

    // Progress tracking
    private final AtomicBoolean isIndeterminate = new AtomicBoolean(true);
    private float targetProgress = 0;
    private float currentProgress = 0;
    private boolean isExiting = false;
    private float exitProgress = 1.0f;
    private Color progressColor = PRIMARY_COLOR;
    private boolean hasError = false;

    public LoadingUI() {
        setUndecorated(true);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setBackground(new Color(0, 0, 0, 0));
        setType(Type.UTILITY);
        setAlwaysOnTop(true);

        contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(BACKGROUND_COLOR);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                g2d.dispose();
            }
        };
        contentPanel.setLayout(new BorderLayout());

        // Make the window draggable
        contentPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
            }
        });

        contentPanel.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                int thisX = getLocation().x;
                int thisY = getLocation().y;

                int xMoved = thisX + e.getX() - (thisX + initialClick.x);
                int yMoved = thisY + e.getY() - (thisY + initialClick.y);

                setLocation(thisX + xMoved, thisY + yMoved);
            }
        });

        // Header with title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("IZMK Loader");
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Close button
        JLabel closeButton = new JLabel("×");
        closeButton.setForeground(TEXT_COLOR);
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        closeButton.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 15));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.exit(0);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                closeButton.setForeground(new Color(255, 80, 80));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeButton.setForeground(TEXT_COLOR);
            }
        });
        headerPanel.add(closeButton, BorderLayout.EAST);

        contentPanel.add(headerPanel, BorderLayout.NORTH);

        // Center panel for status and stage
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // Status label
        statusLabel = new JLabel(currentStatus.get());
        statusLabel.setForeground(TEXT_COLOR);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 15, 3, 15));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(statusLabel);

        // Stage label
        stageLabel = new JLabel("");
        stageLabel.setForeground(TEXT_COLOR);
        stageLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        stageLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 5, 15));
        stageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(stageLabel);

        contentPanel.add(centerPanel, BorderLayout.CENTER);

        // Progress bar
        progressPanel = new ProgressPanel();
        progressPanel.setPreferredSize(new Dimension(WIDTH, 30));
        progressPanel.setOpaque(false);
        contentPanel.add(progressPanel, BorderLayout.SOUTH);

        setContentPane(contentPanel);

        // Start animation thread
        new Thread(this::animationLoop).start();

        // Start status update thread
        new Thread(this::updateStatus).start();
    }

    private void animationLoop() {
        while (true) {
            // Handle progress animation
            if (isIndeterminate.get()) {
                // Keep indeterminate animation
            } else {
                // Animate progress towards target
                float diff = targetProgress - currentProgress;
                if (Math.abs(diff) > 0.001f) {
                    currentProgress += diff * 0.1f; // Smooth animation
                } else {
                    currentProgress = targetProgress;
                }

                // Handle exit animation
                if (isExiting) {
                    exitProgress -= 0.02f;
                    if (exitProgress <= 0) {
                        SwingUtilities.invokeLater(this::dispose);
                        break;
                    }
                }
            }

            progressPanel.repaint();
            try {
                Thread.sleep(16); // ~60 FPS
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void updateStatus() {
        while (true) {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText(currentStatus.get());
                String stage = currentStage.get();
                stageLabel.setText(stage.isEmpty() ? "" : "Stage: " + stage);
                stageLabel.setVisible(!stage.isEmpty());
            });
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void setStatus(String status) {
        currentStatus.set(status);
    }

    public void setStage(String stage) {
        currentStage.set(stage);
    }

    public void startProgress() {
        isIndeterminate.set(false);
        targetProgress = 0.0f;
        currentProgress = 0.0f;
    }

    public void setProgress(float progress) {
        isIndeterminate.set(false);
        this.targetProgress = Math.max(0, Math.min(1, progress));
    }

    public void setProgressColor(Color color) {
        this.progressColor = color;
    }

    public void completeWithSuccess(String message) {
        setStatus(message);
        setProgressColor(SUCCESS_COLOR);
        setProgress(1.0f);

        // Schedule UI exit after 5 seconds
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                exitUI();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public void completeWithError(String message) {
        setStatus(message);
        setProgressColor(ERROR_COLOR);
        setProgress(1.0f);
        hasError = true;

        // Schedule UI exit after 5 seconds
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                exitUI();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void exitUI() {
        isExiting = true;
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                System.exit(0);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    public boolean hasError() {
        return hasError;
    }

    private class ProgressPanel extends JPanel {
        private float position = 0;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth() - 30;
            int height = 6;
            int x = 15;
            int y = getHeight() - 15;

            // Background bar
            g2d.setColor(new Color(60, 60, 60));
            g2d.fill(new RoundRectangle2D.Float(x, y, width, height, height, height));

            if (isIndeterminate.get()) {
                // Animated indeterminate bar
                position = (position + 0.01f) % 1.0f;

                g2d.setColor(progressColor);

                // Calculate animation positions
                int barWidth = width / 4;
                int pos = (int)(position * (width + barWidth)) - barWidth;

                if (pos < width) {
                    int drawWidth = Math.min(barWidth, width - pos);
                    if (pos >= 0) {
                        g2d.fill(new RoundRectangle2D.Float(x + pos, y, drawWidth, height, height, height));
                    } else {
                        g2d.fill(new RoundRectangle2D.Float(x, y, pos + barWidth, height, height, height));
                    }
                }
            } else {
                // Determinate progress bar
                if (isExiting) {
                    // Exit animation - draw progress bar with decreasing width
                    int progressWidth = (int)(width * currentProgress * exitProgress);
                    g2d.setColor(progressColor);
                    g2d.fill(new RoundRectangle2D.Float(x, y, progressWidth, height, height, height));
                } else {
                    // Regular progress bar
                    int progressWidth = (int)(width * currentProgress);
                    g2d.setColor(progressColor);
                    g2d.fill(new RoundRectangle2D.Float(x, y, progressWidth, height, height, height));
                }
            }

            g2d.dispose();
        }
    }

    // Static convenience method to show the UI
    public static LoadingUI display() {
        LoadingUI ui = new LoadingUI();
        ui.setVisible(true);
        return ui;
    }
}
