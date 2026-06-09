package com.naukma.network.client;

import com.naukma.network.messaging.*;
import com.naukma.network.packet.*;
import java.io.IOException;
import java.net.*;

public class StoreClientUDP {
    private final String host;
    private final int port;
    private DatagramSocket socket;
    private final int MAX_RETRIES = 3;
    private final int TIMEOUT_MS = 2000;

    private final PacketMapper packetMapper = new PacketMapper();

    public StoreClientUDP(String host, int port) {
        this.host = host;
        this.port = port;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(TIMEOUT_MS);
            System.out.println("UDP Client initialized for " + host + ":" + port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void send(Packet packet) {
        try {
            Message message = packetMapper.toMessage(packet);
            byte[] data = MessageSerializer.serialize(message);
            InetAddress address = InetAddress.getByName(host);

            for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
                try {
                    DatagramPacket sendPacket = new DatagramPacket(data, data.length, address, port);
                    socket.send(sendPacket);

                    byte[] buffer = new byte[4096];
                    DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                    socket.receive(receivePacket);

                    byte[] responseData = new byte[receivePacket.getLength()];
                    System.arraycopy(buffer, 0, responseData, 0, receivePacket.getLength());

                    Message responseMsg = MessageSerializer.deserialize(responseData);
                    String responseText = new String(responseMsg.getPayload());

                    System.out.println("UDP Response: " + responseText);
                    return;
                } catch (IOException e) {
                    System.out.println("Attempt " + (attempt + 1) + " failed: " + e.getMessage());
                    if (attempt == MAX_RETRIES - 1) {
                        System.out.println("Max retries reached. Packet may be lost.");
                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
            }
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + host);
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }

    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}