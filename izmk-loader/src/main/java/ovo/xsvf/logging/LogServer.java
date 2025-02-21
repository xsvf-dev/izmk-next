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
                    Socket clientSocket = serverSocket.accept();

                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    clientHandlers.add(clientHandler);
                    new Thread(clientHandler, "ClientHandler-" + clientSocket.getPort()).start();
                }
            } catch (IOException ignored) {
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
            cn.langya.Logger.error("Failed to close server socket: " + e.getMessage());
        }

        clientHandlers.forEach(ClientHandler::close);
        clientHandlers.clear();
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
                cn.langya.Logger.error("Error occurred while handling client message: " + e.getMessage());
            } finally {
                clientHandlers.remove(this);
                cn.langya.Logger.info("Client connection closed: " + clientName);
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
                        cn.langya.Logger.error("Unknown message type: " + messageType);
                }
            } catch (IllegalArgumentException e) {
                cn.langya.Logger.error("Failed to decode message: " + e.getMessage());
            } catch (JsonSyntaxException e) {
                cn.langya.Logger.error("Failed to parse JSON message: " + e.getMessage());
            } catch (Exception e) {
                cn.langya.Logger.error("Failed to process message: " + e.getMessage());
            }
        }

        /**
         * 处理初始化消息
         */
        private void handleInitMessage(JsonObject initMessage) {
            clientName = initMessage.get("name").getAsString();
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
                    cn.langya.Logger.warn("Unknown log level: " + level + " - " + formattedMessage);
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
                cn.langya.Logger.error("Error occurred while closing client socket: " + e.getMessage());
            }
        }
    }
}