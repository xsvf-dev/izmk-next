package ovo.xsvf.logging;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class LogServer {
    private static ServerSocket serverSocket;
    private static final List<ClientHandler> clients = new ArrayList<>();

    public static void start() throws IOException {
        serverSocket = new ServerSocket(0);
        new Thread(() -> {
            while (true) {
                try {
                    InputStream inputStream = serverSocket.accept().getInputStream();
                    ClientHandler clientHandler = new ClientHandler(new BufferedReader(new InputStreamReader(inputStream)));
                    clientHandler.run();
                    clients.add(clientHandler);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static int getPort() {
        return serverSocket.getLocalPort();
    }

    public static void close() throws IOException {
        serverSocket.close();
    }

    @RequiredArgsConstructor
    public static class ClientHandler implements Runnable {
        private final BufferedReader reader;
        private String name;

        @Override
        public void run() {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    byte[] decode = Base64.getDecoder().decode(line.getBytes(StandardCharsets.UTF_8));
                    JsonObject jsonObject = JsonParser.parseString(new String(decode, StandardCharsets.UTF_8)).getAsJsonObject();
                    switch (jsonObject.get("type").getAsString()) {
                        case "log" -> {
                            int level = jsonObject.get("level").getAsInt();
                            String message = jsonObject.get("message").getAsString();
                            printLog(level, message);
                        }
                        case "init" -> {
                            name = jsonObject.get("name").getAsString();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void printLog(int level, String message) {
            message = String.format("[%s] %s", name, message);
            switch (level) {
                case Logger.Level.DEBUG -> {
                    cn.langya.Logger.debug(message);
                }
                case Logger.Level.INFO -> {
                    cn.langya.Logger.info(message);
                }
                case Logger.Level.WARN -> {
                    cn.langya.Logger.warn(message);
                }
                case Logger.Level.ERROR -> {
                    cn.langya.Logger.error(message);
                }
            }
        }
    }
}
