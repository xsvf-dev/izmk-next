package ovo.xsvf;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AgentMain {
    private static native Class<?> defineClass(String name, ClassLoader loader, byte[] b);

    @SuppressWarnings("unchecked")
    public static void agentmain(String agentArgs, Instrumentation inst) throws Exception {
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

        Field packageToParentLoader = Class.forName("net.minecraftforge.securemodules.SecureModuleClassLoader", true, finalClassLoader)
                .getDeclaredField("packageToParentLoader");
        packageToParentLoader.setAccessible(true);
        Map<String, ClassLoader> pkgToParentLoader = (Map<String, ClassLoader>) packageToParentLoader.get(finalClassLoader);
        Set<String> packages = new HashSet<>();

        BMWClassLoader bmwClassLoader = new BMWClassLoader((bytes) -> {
            String name = ASMUtil.node(bytes).name;
            packages.add(classToPackage(name.replace("/", ".")));
            return name;
        }, () -> CoreFileProvider.getBinaryFiles(file), (name, b) -> defineClass(name, finalClassLoader, b));
        packages.forEach(pkg -> pkgToParentLoader.put(pkg, bmwClassLoader));
        Thread.currentThread().setContextClassLoader(finalClassLoader);

        try {
            Class.forName("ovo.xsvf.izmk.Entry", true, finalClassLoader)
                    .getMethod("entry", Instrumentation.class, String.class, boolean.class)
                    .invoke(null, inst, file, CoreFileProvider.DEV);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            System.out.println("Entry class not found or entry method not found!!!!");
        }
    }

    public static void premain(String loaderSrcPath, Instrumentation inst) throws Exception {
        System.out.println("premain starting..");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("dll", loaderSrcPath + "\\src\\main\\resources\\lib.dll");
        jsonObject.addProperty("file", loaderSrcPath + "\\build\\libs\\merged-loader.jar");

        new Thread(() -> {
            try {
                agentmain(jsonObject.toString(), inst);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, "IZMK Thread").start();
    }

    private static String classToPackage(String name) {
        int idx = name.lastIndexOf(46);
        return idx != -1 && idx != name.length() - 1 ? name.substring(0, idx) : "";
    }
}
