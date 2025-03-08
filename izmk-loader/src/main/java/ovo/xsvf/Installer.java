package ovo.xsvf;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.prefs.Preferences;

public class Installer {
    private static final File self = new File(ServiceMain.class
            .getProtectionDomain().getCodeSource().getLocation().getPath());
    private static final File target = Path.of("C:", "ProgramData", "izmk", "izmk.jar").toFile();
    private static final File library = target.toPath().resolveSibling("izmk-lib.dll").toFile();
    private static final User32 user32 = Native.load("user32", User32.class);
    private static final File javaHome;
    private static final Preferences prefs = Preferences.userNodeForPackage(Installer.class);
    private static final String INSTALLED_KEY = "izmk-qwq-awa";

    static {
        String path4399 = getRegistryValue("Software\\Netease\\PC4399_MCLauncher", "DownloadPath");
        String pathNE = getRegistryValue("Software\\Netease\\MCLauncher", "DownloadPath");

        File ext;
        if (path4399 == null && pathNE == null) {
            showError(new Exception("无法找到网易我的世界安装目录"), "安装失败");
            throw new RuntimeException("Cannot find Netease Minecraft installation directory");
        } else if (pathNE != null && path4399 != null) {
            int result = user32.showMessage("""
                            检测到网易我的世界存在两个版本，请选择一个安装目录：
                            【确定】：使用网易我的世界版本 【取消】：使用PC4399版本""", "提示",
                    (int) (ServiceMain.User32.MB_ICONASTERISK | 0x00000001L));
            if (result == 1) ext = new File(pathNE, "ext");
            else ext = new File(path4399, "ext");
        } else ext = new File(Objects.requireNonNullElse(pathNE, path4399), "ext");
        if (!ext.exists()) {
            showError(new Exception("网易我的世界 Java 目录不存在"), "安装失败");
            throw new RuntimeException("Java directory not exists: " + ext.getAbsolutePath());
        }

        List<File> jdks = JDKFinder.findJavaExePaths(ext, 21);
        if (jdks.isEmpty()) {
            showError(new Exception("无法找到网易我的世界 Java 目录"), "安装失败");
            throw new RuntimeException("Could not find any Java installation in " + ext.getAbsolutePath());
        } else {
            javaHome = jdks.getFirst();
            user32.showMessage("找到 Java 目录：" + javaHome.getAbsolutePath(), "提示",
                    User32.MB_ICONINFORMATION);
        }
    }

    public static void main(String[] args) {
        String serviceName = "izmk-loader";
        String displayName = "\"IZMK 启动器\"";
        String cmd = "\"" + javaHome.getAbsolutePath() + " -jar " + target.getAbsolutePath() + "\"";

        if (prefs.getBoolean(INSTALLED_KEY, false)) {
            if (user32.showMessage("已经安装过了，是否重新安装？", "提示",
                    (int) (User32.MB_ICONINFORMATION | 0x00000004L)) == 6) {
                try {
                    Runtime.getRuntime().exec(new String[] {"sc.exe", "delete", serviceName});
                } catch (IOException e) {
                    showError(e, "无法删除已安装的 IZMK 启动器服务");
                    return;
                }
                if (target.exists() && !target.delete()) {
                    showError(new IOException("无法删除已安装的 IZMK 启动器"), "删除失败");
                    return;
                }
                if (library.exists() && !library.delete()) {
                    showError(new IOException("无法删除已安装的 IZMK 库文件"), "删除失败");
                    return;
                }
            } else return;
        }

        if (!copySelfTo(target)) {
            showError(new IOException("无法复制文件到目标目录"), "安装失败");
            return;
        }

        if (!extractLibrary()) {
            user32.showMessage("无法解压 DLL 库文件", "IZMK 加载失败", ServiceMain.User32.MB_ICONERROR);
            return;
        }

        String exec = "sc.exe create " + serviceName + " binPath= " + cmd + " DisplayName= " + displayName + " start= auto";
        try {
            System.out.println(exec);
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    Runtime.getRuntime().exec(exec.split(" ")).getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            br.close();

            prefs.putBoolean(INSTALLED_KEY, true);
            user32.showMessage("IZMK 安装成功！", "提示", User32.MB_ICONINFORMATION);
        } catch (Exception e) {
            showError(e, "注册服务失败");
        }
    }

    private static boolean extractLibrary() {
        try (FileOutputStream fos = new FileOutputStream(library);
             InputStream is = ServiceMain.class.getResourceAsStream("/lib.dll")) {
            if (is == null) {
                user32.showMessage("IZMK 资源文件未找到！", "错误", ServiceMain.User32.MB_ICONERROR);
                return false;
            }
            fos.write(is.readAllBytes());
            return true;
        } catch (IOException e) {
            showError(e, "无法解压 DLL 库文件 " + library);
            return false;
        }
    }

    private static String getRegistryValue(String path, String key) {
        try {
            return Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, path, key);
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean copySelfTo(File dest) {
        try (InputStream is = new FileInputStream(self);
             OutputStream os = new FileOutputStream(dest)) {
            os.write(is.readAllBytes());
            return true;
        } catch (IOException e) {
            showError(e, "复制文件失败");
            return false;
        }
    }

    private static void showError(Throwable e, String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(msg).append("：").append(e.getMessage());
        for (StackTraceElement ste : e.getStackTrace()) {
            sb.append("\n at ").append(ste.toString());
        }
        user32.showMessage(sb.toString(), "错误", ServiceMain.User32.MB_ICONERROR);
    }

    public interface User32 extends Library {
        int MB_ICONINFORMATION = 0x00000040;

        int MessageBoxW(int hWnd, WString lpText, WString lpCaption, int uType);

        default int showMessage(String text, String caption, int type) {
            return MessageBoxW(0, new WString(text), new WString(caption), type);
        }
    }
}
