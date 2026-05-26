package com.naukma.network;

import util.CryptoUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class PacketDecoder {
    public Message decode(byte[] bytes) throws Exception {
        ByteBuffer buffer =
                ByteBuffer.wrap(bytes);

        buffer.order(ByteOrder.BIG_ENDIAN);

        byte magic = buffer.get();

        if (magic != 0x13) {
            throw new RuntimeException(
                    "Invalid magic"
            );
        }

        byte src = buffer.get();

        long packetId = buffer.getLong();

        int length = buffer.getInt();

        short receivedHeaderCrc =
                buffer.getShort();

        byte[] headerBytes =
                Arrays.copyOfRange(bytes, 0, 14);

        short calculatedHeaderCrc =
                Crc16.calculate(headerBytes);

        if (receivedHeaderCrc
                != calculatedHeaderCrc) {
            throw new RuntimeException(
                    "Header CRC invalid"
            );
        }

        byte[] encrypted =
                new byte[length];

        buffer.get(encrypted);

        short receivedMessageCrc =
                buffer.getShort();

        short calculatedMessageCrc =
                Crc16.calculate(encrypted);

        if (receivedMessageCrc
                != calculatedMessageCrc) {
            throw new RuntimeException(
                    "Message CRC invalid"
            );
        }

        byte[] decrypted =
                CryptoUtils.decrypt(encrypted);

        return MessageSerializer.deserialize(
                decrypted
        );
    }
}