package ovo.xsvf;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ovo.xsvf.logging.LogServer;
import ovo.xsvf.logging.Logger;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.nio.file.Paths;
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
        int port = jsonObject.get("port").getAsInt();
        Logger logger = Logger.of("Agent", port);
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

        Field loaders = Class.forName("net.minecraftforge.securemodules.SecureModuleClassLoader", true, finalClassLoader)
                .getDeclaredField("packageToParentLoader");
        loaders.setAccessible(true);

        Map<String, ClassLoader> pkgToParentLoader = (Map<String, ClassLoader>) loaders.get(finalClassLoader);
        Set<String> packages = new HashSet<>();
        ClassLoader bmw = new BMWClassLoader(Paths.get(file),
                packages::add, (name, b) -> defineClass(name, finalClassLoader, b));
        packages.forEach(pkg -> {
            logger.debug("adding package {} to parent loader", pkg);
            pkgToParentLoader.put(pkg, bmw);
        });

        Thread.currentThread().setContextClassLoader(finalClassLoader);
        try {
            Class.forName("ovo.xsvf.izmk.Entry", true, finalClassLoader)
                    .getMethod("entry", Instrumentation.class, int.class, String.class, boolean.class)
                    .invoke(null, inst, port, file, CoreFileProvider.DEV);
            logger.debug("Entry.entry() called");
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            logger.error("Entry class or method not found", e);
        }
    }

    public static void premain(String loaderSrcPath, Instrumentation inst) throws Exception {
        System.out.println("premain starting..");
        cn.langya.Logger.setHasColorInfo(true);
        cn.langya.Logger.setLogLevel(cn.langya.Logger.LogLevel.DEBUG);
        LogServer.start();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("dll", loaderSrcPath + "\\src\\main\\resources\\lib.dll");
        jsonObject.addProperty("file", loaderSrcPath + "\\build\\libs\\merged-loader.jar");
        jsonObject.addProperty("port", LogServer.getPort());

        new Thread(() -> {
            try {
                agentmain(jsonObject.toString(), inst);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
