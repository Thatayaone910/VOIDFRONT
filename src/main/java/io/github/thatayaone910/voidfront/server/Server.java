package io.github.thatayaone910.voidfront.server;

import io.github.thatayaone910.voidfront.sector.Sector;
import io.github.thatayaone910.voidfront.sector.SectorConfig;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Accepts client sockets and shares one sector state across their handlers. */
public class Server implements AutoCloseable {

    private final int port;
    private final Sector sector;
    private final ExecutorService handlerExecutor = Executors.newCachedThreadPool();
    private final Set<Socket> activeSockets = ConcurrentHashMap.newKeySet();
    private volatile boolean running;
    private volatile ServerSocket serverSocket;

    public Server(int port, String configPath) {
        if (port <= 1023) {
            throw new IllegalArgumentException("Port number must be greater than 1023.");
        }
        if (configPath == null || configPath.isBlank()) {
            throw new IllegalArgumentException("configPath cannot be null or empty.");
        }
        if (!configPath.endsWith(".properties")) {
            throw new IllegalArgumentException("Config file must have a .properties extension.");
        }

        this.port = port;
        this.sector = new Sector(new SectorConfig(configPath));
    }

    public int getPort() {
        return port;
    }

    public Sector getSector() {
        return sector;
    }

    public boolean isRunning() {
        return running;
    }

    /** Blocks while accepting clients. Call {@link #close()} to stop the server. */
    public void start() {
        if (running) {
            throw new IllegalStateException("Server is already running.");
        }

        running = true;
        try (ServerSocket listener = new ServerSocket(port)) {
            serverSocket = listener;
            System.out.println("## Server running on port " + port + ", IP: "
                    + Inet4Address.getLocalHost().getHostAddress());

            while (running) {
                Socket socket;
                try {
                    socket = listener.accept();
                } catch (SocketException e) {
                    if (!running) {
                        break;
                    }
                    throw e;
                }

                activeSockets.add(socket);
                try {
                    handlerExecutor.execute(new ClientHandler(socket, sector,
                            () -> activeSockets.remove(socket)));
                } catch (RuntimeException e) {
                    activeSockets.remove(socket);
                    try {
                        socket.close();
                    } catch (IOException closeError) {
                        e.addSuppressed(closeError);
                    }
                    throw e;
                }
            }
        } catch (IOException e) {
            if (running) {
                throw new IllegalStateException("Server failed while accepting connections.", e);
            }
        } finally {
            close();
        }
    }

    @Override
    public void close() {
        running = false;
        ServerSocket listener = serverSocket;
        serverSocket = null;
        if (listener != null && !listener.isClosed()) {
            try {
                listener.close();
            } catch (IOException e) {
                System.err.println("Unable to close server socket: " + e.getMessage());
            }
        }

        for (Socket socket : activeSockets) {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Unable to close client socket: " + e.getMessage());
            }
        }
        activeSockets.clear();
        handlerExecutor.shutdownNow();
    }
}
