package ovo.xsvf;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ovo.xsvf.common.BMWClassLoader;
import ovo.xsvf.common.status.Status;
import ovo.xsvf.common.status.StatusReporter;
import sun.misc.Unsafe;

import java.io.FileOutputStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

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

    public static void agentmain(String agentArgs, Instrumentation inst) {
        try {
            agentmain0(agentArgs, inst);
            System.out.println("Successfully loaded IZMK!");
        } catch (Exception e) {
            System.err.println("Failed to load IZMK: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    @SuppressWarnings("unchecked")
    public static void agentmain0(String agentArgs, Instrumentation inst) throws Exception {
        JsonObject jsonObject = JsonParser.parseString(agentArgs).getAsJsonObject();
        String file = jsonObject.get("file").getAsString();
        Consumer<Status> statusConsumer = (status) -> {
        };

        // 获取状态服务器端口（如果存在）
        if (jsonObject.has("statusPort")) {
            int statusPort = jsonObject.get("statusPort").getAsInt();
            // 设置为系统属性，Core可以读取
            System.setProperty("izmk.status.port", String.valueOf(statusPort));
            StatusReporter.init(statusPort);
            statusConsumer = StatusReporter::report;
            System.out.println("Status reporter initialized on port " + statusPort);
        }

        statusConsumer.accept(Status.LOADER_FINDING_CLASSLOADER);
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

        statusConsumer.accept(Status.LOADER_EXTRACTING_RESOURCES);
        List<byte[]> classes = CoreFileProvider.getClasses(file);
        extractResources(CoreFileProvider.getResources(file));

        statusConsumer.accept(Status.LOADER_SETUP_CLASSLOADER);
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

        try {
            Class.forName("ovo.xsvf.izmk.Entry", true, finalClassLoader)
                    .getMethod("entry", Instrumentation.class, boolean.class, Map.class)
                    .invoke(null, inst, !CoreFileProvider.DEV, binaryMap);
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
        jsonObject.addProperty("file", loaderSrcPath + "\\build\\libs\\merged-loader.jar");

        new Thread(() -> {
            try {
                agentmain(jsonObject.toString(), inst);
            } catch (Exception e) {
                System.err.println("Failed to load IZMK: " + e.getMessage());
                e.printStackTrace(System.err);
            }
        }, "IZMK Thread").start();
    }
}
