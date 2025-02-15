package ovo.xsvf.logging;

import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Logger {
    private final OutputStreamWriter writer;
    private final String name;

    public static Logger of(String name, int port) {
        Socket socket;
        try {
            Logger logger = new Logger(new OutputStreamWriter(new Socket("localhost", port).getOutputStream(), StandardCharsets.UTF_8), name);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", "init");
            jsonObject.addProperty("name", name);
            logger.sendJson(jsonObject);
            return logger;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String format(String format, Object... args) {
        for (Object arg : args) {
            format = format.replaceFirst("\\{}", arg.toString());
        }
        return format;
    }

    public void sendJson(JsonObject jsonObject) throws IOException {
        writer.write(Base64.getEncoder().encodeToString(jsonObject.toString().getBytes(StandardCharsets.UTF_8)) + "\n");
        writer.flush();
    }

    public void log(int level, String message) throws IOException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "log");
        jsonObject.addProperty("level", level);
        jsonObject.addProperty("message", message);

        sendJson(jsonObject);
    }

    public void debug(String message, Object... args) throws IOException {
        log(Level.DEBUG, format(message, args));
    }

    public void info(String message, Object... args) throws IOException {
        log(Level.INFO, format(message, args));
    }

    public void warn(String message, Object... args) throws IOException {
        log(Level.WARN, format(message, args));
    }

    public void error(String message, Object... args) throws IOException {
        log(Level.ERROR, format(message, args));
    }

    public static class Level {
        public static final int DEBUG = 0;
        public static final int INFO = 1;
        public static final int WARN = 2;
        public static final int ERROR = 3;
    }
}
