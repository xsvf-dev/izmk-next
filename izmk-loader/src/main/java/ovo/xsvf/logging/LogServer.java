package ovo.xsvf.logging;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.CopyOnWriteArrayList;

public class LogServer {
    private static ServerSocket serverSocket;
    private static final CopyOnWriteArrayList<ClientHandler> clientHandlers = new CopyOnWriteArrayList<>();

    /**
     * 启动日志服务器并开始监听客户端连接
     */
    public static void start() throws IOException {
        serverSocket = new ServerSocket(0);
        new Thread(() -> {
            try {
                while (!serverSocket.isClosed()) {
                    System.out.println("等待客户端连接...");
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("新客户端接入: " + clientSocket.getRemoteSocketAddress());

                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    clientHandlers.add(clientHandler);
                    new Thread(clientHandler, "ClientHandler-" + clientSocket.getPort()).start();
                }
            } catch (IOException e) {
                if (!serverSocket.isClosed()) {
                    System.err.println("服务器异常: " + e.getMessage());
                }
            }
        }, "LogServer-Acceptor").start();
    }

    /**
     * 获取服务器监听的端口号
     */
    public static int getPort() {
        return serverSocket.getLocalPort();
    }

    /**
     * 关闭服务器并释放所有资源
     */
    public static void close() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("关闭服务器时异常: " + e.getMessage());
        }

        clientHandlers.forEach(ClientHandler::close);
        clientHandlers.clear();
        System.out.println("服务器已关闭");
    }

    /**
     * 客户端处理线程
     */
    static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private final BufferedReader reader;
        private String clientName = "UK";

        public ClientHandler(Socket socket) throws IOException {
            this.clientSocket = socket;
            this.reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
            );
        }

        @Override
        public void run() {
            try (clientSocket; reader) {
                String receivedLine;
                while ((receivedLine = reader.readLine()) != null) {
                    processMessage(receivedLine);
                }
            } catch (IOException e) {
                System.err.println("客户端通信异常: " + e.getMessage());
            } finally {
                clientHandlers.remove(this);
                System.out.println("客户端断开: " + clientName);
            }
        }

        /**
         * 处理接收到的消息
         */
        private void processMessage(String base64Message) {
            try {
                // Base64解码
                byte[] decodedData = Base64.getDecoder().decode(base64Message);
                String jsonMessage = new String(decodedData, StandardCharsets.UTF_8);

                // JSON解析
                JsonObject messageObj = JsonParser.parseString(jsonMessage).getAsJsonObject();
                String messageType = messageObj.get("type").getAsString();

                switch (messageType) {
                    case "init":
                        handleInitMessage(messageObj);
                        break;
                    case "log":
                        handleLogMessage(messageObj);
                        break;
                    default:
                        System.err.println("未知消息类型: " + messageType);
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Base64解码失败: " + e.getMessage());
            } catch (JsonSyntaxException e) {
                System.err.println("JSON解析失败: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("消息处理异常: " + e.getMessage());
            }
        }

        /**
         * 处理初始化消息
         */
        private void handleInitMessage(JsonObject initMessage) {
            clientName = initMessage.get("name").getAsString();
            System.out.println("客户端初始化完成: " + clientName);
        }

        /**
         * 处理日志消息
         */
        private void handleLogMessage(JsonObject logMessage) {
            int logLevel = logMessage.get("level").getAsInt();
            String logContent = logMessage.get("message").getAsString();
            outputLog(logLevel, logContent);
        }

        /**
         * 输出格式化日志
         */
        private void outputLog(int level, String content) {
            String formattedMessage = String.format("[%s] %s", clientName, content);

            switch (level) {
                case Logger.Level.DEBUG:
                    cn.langya.Logger.debug(formattedMessage);
                    break;
                case Logger.Level.INFO:
                    cn.langya.Logger.info(formattedMessage);
                    break;
                case Logger.Level.WARN:
                    cn.langya.Logger.warn(formattedMessage);
                    break;
                case Logger.Level.ERROR:
                    cn.langya.Logger.error(formattedMessage);
                    break;
                default:
                    System.err.println("未知日志等级: " + level + " - " + formattedMessage);
            }
        }

        /**
         * 关闭客户端连接
         */
        public void close() {
            try {
                if (!clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println("关闭客户端连接时异常: " + e.getMessage());
            }
        }
    }
}