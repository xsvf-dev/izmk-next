package ovo.xsvf;

import ovo.xsvf.logging.LogServer;
import ovo.xsvf.logging.Logger;

import java.io.IOException;

public class Launcher {
    private static final Logger logger;
    static {
        try {
            LogServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        logger = Logger.Companion.of("Launcher", LogServer.getPort());
    }

    public static void main(String[] args) throws Exception {
        cn.langya.Logger.setLogFilePath("izmk.0721.log");

        printLogo();

        if (!JNAUtil.tryEnableAnsiSupport()) {
            System.out.println("无法启用彩色输出。");
        } else {
            cn.langya.Logger.setHasColorInfo(true);
        }

        logger.info("觉得工具好用就赞助一下作者吧！ https://afdian.com/a/xsvf0721");
    }

    private static void printLogo() {
        System.out.println("""
                 ___   ________   _____ ______    ___  __      \s
                |\\  \\ |\\_____  \\ |\\   _ \\  _   \\ |\\  \\|\\  \\    \s
                \\ \\  \\ \\|___/  /|\\ \\  \\\\\\__\\ \\  \\\\ \\  \\/  /|_  \s
                 \\ \\  \\    /  / / \\ \\  \\\\|__| \\  \\\\ \\   ___  \\ \s
                  \\ \\  \\  /  /_/__ \\ \\  \\    \\ \\  \\\\ \\  \\\\ \\  \\\s
                   \\ \\__\\|\\________\\\\ \\__\\    \\ \\__\\\\ \\__\\\\ \\__\\
                    \\|__| \\|_______| \\|__|     \\|__| \\|__| \\|__| \s
                    \s
                """);
    }
}
