package ovo.xsvf.common.status;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class StatusReporter {
    private static PrintWriter writer = null;

    public static void init(int port) throws IOException {
        Socket socket = new Socket("localhost", port);
        writer = new PrintWriter(socket.getOutputStream(), true);
    }

    public static void report(Status status) {
        if (writer == null) {
            try {
                init(8080);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            writer.println(status.getCode());
        }
    }

    public static void report(int code) {
        if (writer == null) {
            try {
                init(8080);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            writer.println(code);
        }
    }

    public static void close() {
        if (writer != null) {
            writer.close();
        }
    }
}
