package com.naukma.network.messaging;

import com.naukma.network.encryption.PacketEncoder;
import com.naukma.network.packet.UnknownPacketException;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class FakeReceiver implements MessageReceiver {
    private final Random random = new Random();
    private final PacketEncoder encoder = new PacketEncoder();
    private final AtomicLong packetIds = new AtomicLong();

    @Override
    public RawMessage receive() {
        try {
            Thread.sleep(100);

            int value = random.nextInt(4);

            Message message;

            switch (value) {
                case 0 -> message = new Message(1, 100, "P001".getBytes());           // Get stock
                case 1 -> message = new Message(2, 100, "P001:50".getBytes());       // Add stock
                case 2 -> message = new Message(3, 100, "P001:10".getBytes());       // Deduct stock
                case 3 -> message = new Message(4, 100, "G001:Electronics".getBytes()); // Add group
                default -> throw new UnknownPacketException("Unknown packet with value " + value);
            }

            System.out.println("RECEIVED: " + message.getType() + " " + Arrays.toString(message.getPayload()));

            byte[] packet =
                    encoder.encode(
                            message,
                            (byte) 1,
                            packetIds.incrementAndGet()
                    );

            return new RawMessage(packet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}