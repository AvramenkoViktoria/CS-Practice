package com.naukma.network.client;

import com.naukma.network.messaging.*;
import com.naukma.network.packet.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class StoreClientTCP {
    private final String host;
    private final int port;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private volatile boolean connected = false;
    private final ScheduledExecutorService reconnectScheduler = Executors.newScheduledThreadPool(1);
    private final PacketMapper packetMapper = new PacketMapper();

    public StoreClientTCP(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            socket = new Socket(host, port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            connected = true;
            System.out.println("Connected to server " + host + ":" + port);
        } catch (IOException e) {
            connected = false;
            System.out.println("Connection failed: " + e.getMessage());
            scheduleReconnect();
        }
    }

    private void scheduleReconnect() {
        reconnectScheduler.schedule(this::connect, 5, TimeUnit.SECONDS);
    }

    public void send(Packet packet) {
        if (!connected) {
            System.out.println("Not connected. Trying to reconnect...");
            connect();
            if (!connected) return;
        }

        try {
            Message message = packetMapper.toMessage(packet);
            byte[] data = MessageSerializer.serialize(message);

            out.writeInt(data.length);
            out.write(data);
            out.flush();

            int length = in.readInt();
            byte[] responseData = new byte[length];
            in.readFully(responseData);

            Message responseMsg = MessageSerializer.deserialize(responseData);
            System.out.println("Response: " + new String(responseMsg.getPayload()));

        } catch (IOException e) {
            System.out.println("Send failed: " + e.getMessage());
            connected = false;
            scheduleReconnect();
        }
    }

    public void disconnect() {
        try {
            connected = false;
            if (socket != null) socket.close();
            reconnectScheduler.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}