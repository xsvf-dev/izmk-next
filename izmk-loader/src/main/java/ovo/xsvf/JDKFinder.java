package ovo.xsvf;

import java.io.*;
import java.util.*;

public class JDKFinder {

    /**
     * 遍历指定文件夹下的所有目录，找出JDK版本大于等于21的文件夹中java.exe的绝对路径。
     *
     * @param baseDir 指定的根目录路径
     * @return 满足条件的java.exe绝对路径列表
     */
    public static List<File> findJavaExePaths(File baseDir, int minVersion) {
        List<File> javaExePaths = new ArrayList<>();
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            System.err.println("指定的目录不存在或不是一个文件夹：" + baseDir.getAbsolutePath());
            return javaExePaths;
        }
        searchDirectory(baseDir, javaExePaths, minVersion);
        return javaExePaths;
    }

    /**
     * 递归遍历目录，寻找满足条件的JDK文件夹。
     *
     * @param dir 当前遍历的目录
     * @param javaExePaths 保存符合条件的java.exe路径的列表
     * @param minVersion 最小版本号
     */
    private static void searchDirectory(File dir, List<File> javaExePaths, int minVersion) {
        // 判断当前目录是否包含release文件，release文件通常位于JDK安装目录的根目录下
        File releaseFile = new File(dir, "release");
        if (releaseFile.exists() && releaseFile.isFile()) {
            String javaVersion = getJavaVersion(releaseFile);
            if (javaVersion != null) {
                try {
                    // 假设版本号格式为"21"或"21.0.1"，取第一个“.”前的部分为主版本号
                    String[] versionParts = javaVersion.split("\\.");
                    int majorVersion = Integer.parseInt(versionParts[0]);
                    if (majorVersion >= minVersion) {
                        // 检查 bin\java.exe 是否存在
                        File javaExe = new File(dir, "bin" + File.separator + "java.exe");
                        if (javaExe.exists() && javaExe.isFile()) {
                            javaExePaths.add(javaExe);
                        }
                    }
                } catch (NumberFormatException e) {
                    System.err.println("无法解析版本号：" + javaVersion + "，目录：" + dir.getAbsolutePath());
                }
            }
        }
        // 递归遍历子目录
        File[] subFiles = dir.listFiles();
        if (subFiles != null) {
            for (File sub : subFiles) {
                if (sub.isDirectory()) {
                    searchDirectory(sub, javaExePaths, minVersion);
                }
            }
        }
    }

    /**
     * 读取release文件，解析出JAVA_VERSION的值。
     *
     * @param releaseFile release文件
     * @return JAVA_VERSION的值，未找到时返回null
     */
    private static String getJavaVersion(File releaseFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(releaseFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("JAVA_VERSION=")) {
                    // 例如：JAVA_VERSION="21.0.1"
                    String version = line.substring("JAVA_VERSION=".length()).trim();
                    // 去除引号
                    if (version.startsWith("\"") && version.endsWith("\"")) {
                        version = version.substring(1, version.length() - 1);
                    }
                    return version;
                }
            }
        } catch (IOException e) {
            System.err.println("读取release文件时出错：" + releaseFile.getAbsolutePath());
            e.printStackTrace();
        }
        return null;
    }
}
