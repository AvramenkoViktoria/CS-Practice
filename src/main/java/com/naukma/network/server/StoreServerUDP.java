package com.naukma.network.server;

import com.naukma.network.messaging.*;
import java.io.IOException;
import java.net.*;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

public class StoreServerUDP {
    private final int port;
    private final BlockingQueue<RawMessage> rawQueue;
    private final RequestRegistry registry;
    private DatagramSocket socket;
    private volatile boolean running = true;

    public StoreServerUDP(int port, BlockingQueue<RawMessage> rawQueue, RequestRegistry registry) {
        this.port = port;
        this.rawQueue = rawQueue;
        this.registry = registry;
    }

    public void start() throws SocketException {
        socket = new DatagramSocket(port);
        System.out.println("UDP Server started on port " + port);
        byte[] buffer = new byte[64 * 1024];

        while (running) {
            try {
                DatagramPacket dgram = new DatagramPacket(buffer, buffer.length);
                socket.receive(dgram);

                byte[] data = new byte[dgram.getLength()];
                System.arraycopy(buffer, 0, data, 0, dgram.getLength());

                InetAddress addr = dgram.getAddress();
                int port = dgram.getPort();
                String requestId = UUID.randomUUID().toString();

                DatagramSocket sock = socket;
                ClientChannel channel = responseBytes -> {
                    try {
                        DatagramPacket reply = new DatagramPacket(responseBytes, responseBytes.length, addr, port);
                        sock.send(reply);
                    } catch (IOException e) {
                        System.err.println("UDP: failed to send response to " + addr + ":" + port
                                + " — " + e.getMessage());
                    }
                };

                registry.register(requestId, new PendingRequest(requestId, channel));
                rawQueue.put(new RawMessage(data, requestId));
            } catch (IOException e) {
                if (running) System.err.println("UDP receive error: " + e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void stop() {
        running = false;
        if (socket != null) socket.close();
    }
}