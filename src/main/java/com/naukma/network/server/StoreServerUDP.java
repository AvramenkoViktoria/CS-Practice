package com.naukma.network.server;

import com.naukma.model.Warehouse;
import com.naukma.network.messaging.*;
import com.naukma.network.packet.Packet;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;

public class StoreServerUDP {
    private final int port;
    private final Processor processor;
    private final MessageMapper mapper = new MessageMapper();
    private DatagramSocket socket;
    private ExecutorService executor;
    private volatile boolean running = true;

    public StoreServerUDP(int port) {
        this.port = port;
        Warehouse warehouse = Warehouse.createDefault();
        this.processor = new Processor(warehouse);
        this.executor = Executors.newCachedThreadPool();
    }

    public void start() {
        try {
            socket = new DatagramSocket(port);
            System.out.println("UDP Server started on port " + port);
            byte[] buffer = new byte[4096];

            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                byte[] data = new byte[packet.getLength()];
                System.arraycopy(buffer, 0, data, 0, packet.getLength());

                executor.submit(() -> handlePacket(data, packet.getAddress(), packet.getPort()));
            }
        } catch (IOException e) {
            if (running) e.printStackTrace();
        }
    }

    private void handlePacket(byte[] data, InetAddress address, int port) {
        try {
            Message msg = MessageSerializer.deserialize(data);
            Packet packet = mapper.toPacket(msg);
            String result = processor.process(packet);

            Message responseMsg = new Message(200, msg.getUserId(), result.getBytes());
            byte[] response = MessageSerializer.serialize(responseMsg);

            DatagramPacket responsePacket = new DatagramPacket(response, response.length, address, port);
            socket.send(responsePacket);
        } catch (Exception e) {
            try {
                String error = "Error: " + e.getMessage();
                Message errorMsg = new Message(101, 0, error.getBytes());
                byte[] response = MessageSerializer.serialize(errorMsg);
                DatagramPacket responsePacket = new DatagramPacket(response, response.length, address, port);
                socket.send(responsePacket);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void stop() {
        running = false;
        if (socket != null) socket.close();
        executor.shutdown();
    }
}