package ovo.xsvf.common.status;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatusServer implements Runnable {
    private final StatusListener listener;
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool;

    public StatusServer(StatusListener listener, int port) throws IOException {
        this.listener = listener;
        this.serverSocket = new ServerSocket(port);
        this.threadPool = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            if (!serverSocket.isClosed()) {
                e.printStackTrace(System.err);
            }
        } finally {
            shutdown();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (clientSocket;  // Java 9+ 自动资源管理
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(clientSocket.getInputStream()))) {

            while (!clientSocket.isClosed()) {
                String input = reader.readLine();
                if (input == null) break;  // 连接关闭时退出

                try {
                    int code = Integer.parseInt(input);
                    listener.onStatusChange(Status.fromCode(code));
                } catch (NumberFormatException e) {
                    System.err.println("无效的状态码: " + input);
                }
            }
        } catch (IOException e) {
            if (!clientSocket.isClosed()) {
                e.printStackTrace(System.err);
            }
        }
    }

    public void shutdown() {
        try {
            serverSocket.close();
            threadPool.shutdownNow();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
}
