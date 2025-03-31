package ovo.xsvf.ui;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 用于生成和保存托盘图标的工具类
 */
public class TrayIconGenerator {
    // 使用支持中文的字体，调整为更小的字体大小
    private static final Font ICON_FONT = new Font("Microsoft YaHei", Font.BOLD, 10);

    /**
     * 生成一个简单的蓝色图标
     */
    public static Image generateIcon() {
        try {
            // 增大图标尺寸以提高清晰度
            int iconSize = 32; // 从16x16增加到32x32
            BufferedImage image = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();

            // 设置抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // 绘制圆形背景
            g2d.setColor(new Color(30, 30, 35));
            g2d.fillOval(0, 0, iconSize, iconSize);

            // 绘制字母"I"，调整位置和大小
            g2d.setColor(new Color(50, 150, 250));
            g2d.setFont(new Font("Arial", Font.BOLD, 20)); // 使用更大的字体
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth("I");
            int textHeight = fm.getHeight();
            int x = (iconSize - textWidth) / 2;
            int y = (iconSize - textHeight) / 2 + fm.getAscent();
            g2d.drawString("I", x, y);

            g2d.dispose();
            return image;
        } catch (Exception e) {
            System.err.println("生成图标时出错，使用备用方法: " + e.getMessage());
            return generateFallbackIcon();
        }
    }

    /**
     * 生成备用图标，使用最简单的方法确保在任何情况下都能生成有效图标
     */
    private static Image generateFallbackIcon() {
        // 创建一个简单的纯色图标
        int size = 16; // 使用较小但标准的尺寸
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // 使用蓝色填充
        g.setColor(new Color(30, 144, 255));
        g.fillRect(0, 0, size, size);
        // 添加白色边框
        g.setColor(Color.WHITE);
        g.drawRect(0, 0, size-1, size-1);
        g.dispose();
        return image;
    }

    /**
     * 将图像保存为PNG文件
     */
    public static void saveIconToFile(Image image, String path) {
        try {
            BufferedImage bufferedImage;
            if (image instanceof BufferedImage) {
                bufferedImage = (BufferedImage) image;
            } else {
                bufferedImage = new BufferedImage(
                        image.getWidth(null),
                        image.getHeight(null),
                        BufferedImage.TYPE_INT_ARGB
                );
                Graphics2D g2d = bufferedImage.createGraphics();
                g2d.drawImage(image, 0, 0, null);
                g2d.dispose();
            }

            File outputFile = new File(path);
            ImageIO.write(bufferedImage, "png", outputFile);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * 在运行时生成图标并保存到资源目录
     */
    public static void generateAndSaveIcon() {
        try {
            Path resourcesDir = Paths.get("izmk-loader", "src", "main", "resources");
            if (!Files.exists(resourcesDir)) {
                Files.createDirectories(resourcesDir);
            }

            Image icon = generateIcon();
            saveIconToFile(icon, resourcesDir.resolve("icon.png").toString());
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * 将图像转换为字节数组
     */
    public static byte[] imageToByteArray(Image image) throws IOException {
        BufferedImage bufferedImage;
        if (image instanceof BufferedImage) {
            bufferedImage = (BufferedImage) image;
        } else {
            bufferedImage = new BufferedImage(
                    image.getWidth(null),
                    image.getHeight(null),
                    BufferedImage.TYPE_INT_ARGB
            );
            Graphics2D g2d = bufferedImage.createGraphics();
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        return baos.toByteArray();
    }

    public static void main(String[] args) {
        // 测试生成图标
        generateAndSaveIcon();
        System.out.println("图标已生成");
    }
} 