package com.naukma.network.server;

import com.naukma.network.messaging.*;
import java.io.*;
import java.net.*;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

public class StoreServerTCP {
    private final int port;
    private final BlockingQueue<RawMessage> rawQueue;
    private final RequestRegistry registry;
    private ServerSocket serverSocket;
    private volatile boolean running = true;

    public StoreServerTCP(int port, BlockingQueue<RawMessage> rawQueue, RequestRegistry registry) {
        this.port = port;
        this.rawQueue = rawQueue;
        this.registry = registry;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("TCP Server started on port " + port);
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                handleClient(clientSocket);
            } catch (IOException e) {
                if (running) System.err.println("TCP accept error: " + e.getMessage());
            }
        }
    }

    private void handleClient(Socket socket) {
        String peer = socket.getInetAddress() + ":" + socket.getPort();
        System.out.println("TCP client connected: " + peer);

        try (socket;
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            while (running && !socket.isClosed()) {
                int length;
                try {
                    length = in.readInt();
                } catch (EOFException e) {
                    break;
                }

                if (length <= 0 || length > 64 * 1024) {
                    System.err.println("TCP: invalid frame length " + length + " from " + peer);
                    break;
                }

                byte[] data = new byte[length];
                in.readFully(data);

                String requestId = UUID.randomUUID().toString();

                ClientChannel channel = responseBytes -> {
                    try {
                        synchronized (out) {
                            out.writeInt(responseBytes.length);
                            out.write(responseBytes);
                            out.flush();
                        }
                    } catch (IOException e) {
                        System.err.println("TCP: failed to send response to " + peer + ": " + e.getMessage());
                    }
                };

                registry.register(requestId, new PendingRequest(requestId, channel));
                rawQueue.put(new RawMessage(data, requestId));
            }
        } catch (IOException e) {
            if (running) System.out.println("TCP client disconnected: " + peer + " (" + e.getMessage() + ")");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            System.err.println("Error stopping TCP server: " + e.getMessage());
        }
    }
}