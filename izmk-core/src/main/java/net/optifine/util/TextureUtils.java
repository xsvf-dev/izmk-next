package net.optifine.util;

public class TextureUtils {
    public static String fixResourcePath(String path, String basePath) {
        String s = "assets/minecraft/";
        if (path.startsWith(s)) {
            return path.substring(s.length());
        } else if (path.startsWith("./")) {
            path = path.substring(2);
            if (!basePath.endsWith("/")) {
                basePath = basePath + "/";
            }

            return basePath + path;
        } else {
            if (path.startsWith("/~")) {
                path = path.substring(1);
            }

            String s1 = "optifine/";
            if (path.startsWith("~/")) {
                path = path.substring(2);
                return s1 + path;
            } else {
                return path.startsWith("/") ? s1 + path.substring(1) : path;
            }
        }
    }

    public static String getBasePath(String path) {
        int i = path.lastIndexOf(47);
        return i < 0 ? "" : path.substring(0, i);
    }
}