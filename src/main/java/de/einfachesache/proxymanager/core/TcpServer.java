package de.einfachesache.proxymanager.core;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer {

    private static final int port = 6060;

    private static Thread thread;
    private static ServerSocket serverSocket;

    public static void run() {
        if (!Config.isConnectTCPServer()) return;

        if (thread != null && thread.isAlive()) {
            Core.warn("TCP-Server is already running.");
            return;
        }

        thread = new Thread(() -> {
            try (ServerSocket socket = new ServerSocket(port)) {
                serverSocket = socket;

                Core.info("TCP-Server is listening for client connections on port " + port);

                while (!Thread.currentThread().isInterrupted()) {
                    try (Socket client = socket.accept()) {
                        Core.debug("Client with ip " + client.getInetAddress().getHostAddress() + " on port " + client.getPort() + " connected");
                    } catch (IOException ex) {
                        if (!socket.isClosed()) {
                            Core.warn("Error accepting client: " + ex.getMessage());
                        }
                    }
                }

            } catch (BindException ex) {
                Core.warn("TCP-Server is already running: " + ex.getLocalizedMessage());
            } catch (IOException ex) {
                Core.severe("Error in TCP-Server", ex);
            }
        }, "TCP-Server");
        thread.start();
    }

    public static void stop() {
        if (thread == null || !thread.isAlive()) return;

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            Core.warn("Error closing ServerSocket: " + e.getMessage());
        }

        thread.interrupt();
    }
}