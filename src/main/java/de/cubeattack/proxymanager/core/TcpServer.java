package de.cubeattack.proxymanager.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer {

    private static Thread thread;

    public static void run(int port) {

        if (!Config.isConnectTCPServer()) return;

        thread = new Thread(() -> {
            try (ServerSocket socket = new ServerSocket(port)) {

                Core.info("TCP-Server listening on port " + port);
                Core.info("TCP-Server is waiting for client connections on port " + port);

                while (true) {
                    try (Socket client = socket.accept()) {
                        Core.debug("Client with ip " + client.getInetAddress().getHostAddress() + " on port " + client.getPort() + " connected");
                    }
                }

            } catch (IOException ex) {
                Core.severe("Error in TCP-Server", ex);
            }
        }, "TCP-Server");
        thread.start();

    }

    public static void stop() {
        if (thread == null) return;
        thread.interrupt();
    }
}
