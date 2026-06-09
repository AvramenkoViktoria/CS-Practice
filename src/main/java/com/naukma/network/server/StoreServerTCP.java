package com.naukma.network.server;

import com.naukma.model.Warehouse;
import com.naukma.network.messaging.*;
import com.naukma.network.packet.Packet;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class StoreServerTCP {
    private final int port;
    private final Processor processor;
    private final MessageMapper mapper = new MessageMapper();
    private ServerSocket serverSocket;
    private ExecutorService executor;
    private volatile boolean running = true;

    public StoreServerTCP(int port) {
        this.port = port;
        Warehouse warehouse = Warehouse.createDefault();
        this.processor = new Processor(warehouse);
        this.executor = Executors.newCachedThreadPool();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("TCP Server started on port " + port);
            while (running) {
                Socket clientSocket = serverSocket.accept();
                executor.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            if (running) {
                e.printStackTrace();
            }
        }
    }

    private void handleClient(Socket socket) {
        try (socket;
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            System.out.println("Client connected: " + socket.getInetAddress());

            while (running) {
                int length = in.readInt();
                if (length <= 0) break;

                byte[] data = new byte[length];
                in.readFully(data);

                Message msg = MessageSerializer.deserialize(data);

                try {
                    Packet packet = mapper.toPacket(msg);
                    String result = processor.process(packet);

                    Message responseMsg = new Message(200, msg.getUserId(), result.getBytes());
                    byte[] response = MessageSerializer.serialize(responseMsg);

                    out.writeInt(response.length);
                    out.write(response);
                    out.flush();
                } catch (Exception e) {
                    String error = "Error: " + e.getMessage();
                    Message errorMsg = new Message(101, msg.getUserId(), error.getBytes());
                    byte[] response = MessageSerializer.serialize(errorMsg);
                    out.writeInt(response.length);
                    out.write(response);
                    out.flush();
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
            executor.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}