package ovo.xsvf;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ovo.xsvf.logging.LogServer;
import ovo.xsvf.logging.Logger;

import java.lang.instrument.Instrumentation;

public class AgentMain {
    private static native Class<?> defineClass(String name, ClassLoader loader, byte[] b);

    public static void agentmain(String agentArgs, Instrumentation inst) throws Exception {
        JsonObject jsonObject = JsonParser.parseString(agentArgs).getAsJsonObject();
        String dll = jsonObject.get("dll").getAsString();
        String file = jsonObject.get("file").getAsString();
        int port = jsonObject.get("port").getAsInt();
        boolean debug = jsonObject.get("debug").getAsBoolean();

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

        logger.debug("loading class...");
        Thread.currentThread().setContextClassLoader(finalClassLoader);
        for (Class<?> clazz : new ClassAnalyzer((name, b) -> defineClass(name, finalClassLoader, b), logger)
                .loadClasses(CoreFileProvider.getBinaryFiles(file))) {
            if (clazz.getName().equals("ovo.xsvf.izmk.Entry")) {
                clazz.getMethod("entry", Instrumentation.class, int.class, String.class)
                        .invoke(null, inst, port, file);
            }
        }
    }

    public static void premain(String agentArgs, Instrumentation inst) throws Exception {
        System.out.println("premain starting..");
        cn.langya.Logger.setHasColorInfo(true);
        cn.langya.Logger.setLogLevel(cn.langya.Logger.LogLevel.DEBUG);
        LogServer.start();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("dll", "D:\\izmk-next\\izmk-loader\\src\\main\\resources\\lib.dll");
        jsonObject.addProperty("file", "D:\\izmk-next\\izmk-loader\\build\\libs\\merged-loader.jar");
        jsonObject.addProperty("port", LogServer.getPort());
        jsonObject.addProperty("debug", true);

        new Thread(() -> {
            try {
                agentmain(jsonObject.toString(), inst);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
