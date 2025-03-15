package ovo.xsvf;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import sun.misc.Unsafe;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.util.*;

public class Bootstrap {
    private static native Class<?> defineClass(String name, ClassLoader loader, byte[] b);
    private static final Unsafe unsafe;
    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        try {
            agentmain0(agentArgs, inst);
        } catch (Exception e) {
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                System.exit(1);
            });
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked, deprecation")
    public static void agentmain0(String agentArgs, Instrumentation inst) throws Exception {
        JsonObject jsonObject = JsonParser.parseString(agentArgs).getAsJsonObject();
        String dll = jsonObject.get("dll").getAsString();
        String file = jsonObject.get("file").getAsString();
        System.load(dll);

        ClassLoader classLoader = null;
        do {
            for (Thread thread : Thread.getAllStackTraces().keySet()) {
                if (thread.getName().contains("Render thread")) {
                    classLoader = thread.getContextClassLoader();
                    break;
                }
            }
        } while (classLoader == null);
        final ClassLoader finalClassLoader = classLoader;

        List<byte[]> binaryFiles = CoreFileProvider.getBinaryFiles(file);
        Map<String, byte[]> binaryMap = new HashMap<>();
        Set<String> packages = new HashSet<>();

        BMWClassLoader bmwClassLoader = new BMWClassLoader((bytes) -> {
            String name = ASMUtil.node(bytes).name;
            packages.add(classToPackage(name.replace("/", ".")));
            binaryMap.put(name.replace("/", "."), bytes);
            return name;
        }, () -> binaryFiles, (name, b) -> defineClass(name, finalClassLoader, b));

        Field parentLoadersField = Class.forName("cpw.mods.cl.ModuleClassLoader")
                .getDeclaredField("parentLoaders");
        Map<String, ClassLoader> parentLoaders = (Map<String, ClassLoader>)
                unsafe.getObject(finalClassLoader, unsafe.objectFieldOffset(parentLoadersField));
        packages.forEach(it -> parentLoaders.put(it, bmwClassLoader));
        Thread.currentThread().setContextClassLoader(finalClassLoader);

        try {
            Class.forName("ovo.xsvf.izmk.Entry", true, finalClassLoader)
                    .getMethod("entry", Instrumentation.class, String.class, boolean.class, Map.class, byte[].class)
                    .invoke(null, inst, file, CoreFileProvider.DEV, binaryMap, new byte[0]);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            System.out.println("Entry class not found or entry method not found!!!!");
            throw e;
        }
    }

    private static String classToPackage(String name) {
        int idx = name.lastIndexOf(46);
        return idx != -1 && idx != name.length() - 1 ? name.substring(0, idx) : "";
    }

    // For testing
    public static void premain(String loaderSrcPath, Instrumentation inst) throws Exception {
        System.out.println("Premain starting..");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("dll", loaderSrcPath + "\\src\\main\\resources\\lib.dll");
        jsonObject.addProperty("mapping", loaderSrcPath + "\\src\\main\\resources\\mapping.srg");
        jsonObject.addProperty("file", loaderSrcPath + "\\build\\libs\\merged-loader.jar");

        new Thread(() -> {
            try {
                agentmain(jsonObject.toString(), inst);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, "IZMK Thread").start();
    }
}
