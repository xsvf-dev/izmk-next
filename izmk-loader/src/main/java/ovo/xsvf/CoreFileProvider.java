package ovo.xsvf;

import com.allatori.annotations.DoNotRename;

import java.util.List;
import java.util.Map;

/**
 * This class will be replaced at the compile time by buildSrc.
 */
@DoNotRename
public class CoreFileProvider {
    public static boolean DEV = true;

    /**
     * Get the list of classes in the given jar file(only core jar).
     * This method will be replaced at the compile time by buildSrc.
     *
     * @param jarPath The path of the jar file.
     * @return The list of classes in the given jar file.
     */
    public static List<byte[]> getClasses(String jarPath) {
        return List.of();
    }

    /**
     * Get the list of resources in the given jar file(only core jar).
     * This method will be replaced at the compile time by buildSrc.
     *
     * @param jarPath The path of the jar file.
     * @return The list of resources in the given jar file.
     */
    public static Map<String, byte[]> getResources(String jarPath) {
        return Map.of();
    }
}
