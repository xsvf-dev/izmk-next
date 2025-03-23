package ovo.xsvf;

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

public class LoadingUI extends JFrame {
    private static final LoadingUI INSTANCE = new LoadingUI();
    private final JLabel statusLabel;
    private final JLabel stageLabel;
    private final ProgressBar progressBar;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean indeterminateMode = new AtomicBoolean(true);
    private final AtomicBoolean errorMode = new AtomicBoolean(false);
    private final AtomicBoolean successMode = new AtomicBoolean(false);
    private final AtomicInteger progress = new AtomicInteger(0);

    private LoadingUI() {
        super("IZMK-Next");
        setUndecorated(true);
        setSize(400, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(30, 30, 35));

        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("IZMK-Next");
        titleLabel.setForeground(new Color(220, 220, 220));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titlePanel.add(titleLabel, BorderLayout.WEST);

        // Close button
        JLabel closeButton = new JLabel("×");
        closeButton.setForeground(new Color(220, 220, 220));
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 20));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                System.exit(0);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeButton.setForeground(new Color(255, 80, 80));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeButton.setForeground(new Color(220, 220, 220));
            }
        });
        titlePanel.add(closeButton, BorderLayout.EAST);

        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // Center content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // Stage indicator
        stageLabel = new JLabel("Finding Minecraft...");
        stageLabel.setForeground(new Color(180, 180, 180));
        stageLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        stageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(stageLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Status indicator
        statusLabel = new JLabel("Searching for game process");
        statusLabel.setForeground(new Color(140, 140, 140));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(statusLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Progress bar
        progressBar = new ProgressBar();
        progressBar.setPreferredSize(new Dimension(370, 10));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(progressBar);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Footer with version
        JLabel versionLabel = new JLabel("v1.0");
        versionLabel.setForeground(new Color(100, 100, 100));
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        mainPanel.add(versionLabel, BorderLayout.SOUTH);

        add(mainPanel);

        // Make window draggable
        WindowDragListener dragListener = new WindowDragListener(this);
        addMouseListener(dragListener);
        addMouseMotionListener(dragListener);

        // Add window listener to handle close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                executor.shutdownNow();
            }
        });

        // Start the animation
        startAnimation();

        // Set rounded corners
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
    }

    public static LoadingUI getInstance() {
        return INSTANCE;
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
                statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            });
        }
    }
    
    public void showSuccess() {
        if (successMode.get()) return;
        
        successMode.set(true);
        indeterminateMode.set(false);
        
        // Update UI to show success
        SwingUtilities.invokeLater(() -> {
            setStage("Success!");
            setStatus("IZMK loaded successfully");
            statusLabel.setForeground(new Color(50, 205, 50));
            statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            setProgress(100);
        });
        
        // Cancel any existing tasks
        executor.shutdownNow();
        final ScheduledExecutorService countdownExecutor = Executors.newSingleThreadScheduledExecutor();
        
        // Start countdown animation (5 seconds)
        final long startTime = System.currentTimeMillis();
        final long duration = 5000; // 5 seconds
        
        countdownExecutor.scheduleAtFixedRate(() -> {
            long elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime >= duration) {
                // Exit application after countdown completes
                countdownExecutor.shutdown();
                SwingUtilities.invokeLater(() -> {
                    dispose();
                    System.exit(0);
                });
            } else {
                // Update progress bar based on remaining time
                float remainingPercentage = 100 * (1 - (float)elapsedTime / duration);
                setProgress((int)remainingPercentage);
            }
        }, 0, 16, TimeUnit.MILLISECONDS);
    }

    private void startAnimation() {
        executor.scheduleAtFixedRate(() -> SwingUtilities.invokeLater(() -> progressBar.repaint()), 0, 16, TimeUnit.MILLISECONDS);
    }

    @Override
    public void dispose() {
        super.dispose();
        executor.shutdownNow();
    }

    // Custom progress bar with animations
    private class ProgressBar extends JComponent {
        private float animationOffset = 0f;
        private static final float ANIMATION_SPEED = 0.015f;

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
}
