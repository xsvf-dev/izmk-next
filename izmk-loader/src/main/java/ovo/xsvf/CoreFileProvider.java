package ovo.xsvf;

import java.util.List;
import java.util.Map;

/**
 * This class will be replaced at the compile time by buildSrc.
 */
public class CoreFileProvider {
    public static boolean DEV = true;

    public static List<byte[]> getClasses(String jarPath) {
        return List.of();
    }

    public static Map<String, byte[]> getResources(String jarPath) {
        return Map.of();
    }
}
