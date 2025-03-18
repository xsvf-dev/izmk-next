package ovo.xsvf;

import com.allatori.annotations.DoNotRename;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import sun.misc.Unsafe;

import java.io.FileOutputStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@DoNotRename
public class Bootstrap {
    private static final Unsafe unsafe;
    private static final Path assetsDir = Path.of("C:", "ProgramData", "izmk", "runtime_assets");

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static native Class<?> defineClass(String name, ClassLoader loader, byte[] b);

    @DoNotRename
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
            }).start();
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void agentmain0(String agentArgs, Instrumentation inst) throws Exception {
        JsonObject jsonObject = JsonParser.parseString(agentArgs).getAsJsonObject();
        String dll = jsonObject.get("dll").getAsString();
        String mapping = jsonObject.get("mapping").getAsString();
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
        Thread.currentThread().setContextClassLoader(finalClassLoader);

        List<byte[]> classes = CoreFileProvider.getClasses(file);
        extractResources(CoreFileProvider.getResources(file));
        Map<String, byte[]> binaryMap = new HashMap<>();
        Set<String> packages = new HashSet<>();

        BMWClassLoader bmwClassLoader = new BMWClassLoader((bytes) -> {
            String name = ASMUtil.node(bytes).name;
            packages.add(classToPackage(name.replace("/", ".")));
            binaryMap.put(name.replace("/", "."), bytes);
            return name;
        }, () -> classes, name -> {
            try {
                return assetsDir.resolve(name).toUri().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }, finalClassLoader);

        Field parentLoadersField = Class.forName("cpw.mods.cl.ModuleClassLoader")
                .getDeclaredField("parentLoaders");
        Map<String, ClassLoader> parentLoaders = (Map<String, ClassLoader>)
                unsafe.getObject(finalClassLoader, unsafe.objectFieldOffset(parentLoadersField));
        packages.forEach(it -> parentLoaders.put(it, bmwClassLoader));

        byte[] mappingBytes = Files.readAllBytes(Path.of(mapping));

        try {
            Class.forName("ovo.xsvf.izmk.Entry", true, finalClassLoader)
                    .getMethod("entry", Instrumentation.class, boolean.class, Map.class, byte[].class)
                    .invoke(null, inst, true, binaryMap, mappingBytes);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            System.out.println("Entry class not found or entry method not found!!!!");
            throw e;
        }
    }

    private static void extractResources(Map<String, byte[]> resources) throws Exception {
        System.out.println("Extracting resources..");
        for (Map.Entry<String, byte[]> entry : resources.entrySet()) {
            String name = entry.getKey();
            byte[] bytes = entry.getValue();
            Path path = assetsDir.resolve(name);
            Files.createDirectories(path.getParent());
            try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
                fos.write(bytes);
            }
        }
        System.out.println("Resources extracted.");
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
