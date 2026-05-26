package com.naukma.network.encryption;

import com.naukma.network.messaging.Message;
import com.naukma.network.messaging.MessageSerializer;
import util.CryptoUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class PacketDecoder {

    public Message decode(byte[] bytes) throws Exception {
        if (bytes == null || bytes.length < 16)
            throw new RuntimeException("Packet too short");

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.BIG_ENDIAN);

        byte magic = buffer.get();
        if (magic != 0x13)
            throw new RuntimeException("Invalid magic byte");

        byte src = buffer.get();
        long packetId = buffer.getLong();
        int length = buffer.getInt();
        short receivedHeaderCrc = buffer.getShort();

        byte[] headerBytes = Arrays.copyOfRange(bytes, 0, 14);
        short calculatedHeaderCrc = Crc16.calculate(headerBytes);

        if (receivedHeaderCrc != calculatedHeaderCrc)
            throw new RuntimeException("Header CRC invalid");

        if (bytes.length < 16 + length + 2)
            throw new RuntimeException("Packet truncated");

        byte[] encrypted = new byte[length];
        buffer.get(encrypted);

        short receivedMessageCrc = buffer.getShort();
        short calculatedMessageCrc = Crc16.calculate(encrypted);

        if (receivedMessageCrc != calculatedMessageCrc)
            throw new RuntimeException("Message CRC invalid");

        byte[] decrypted = CryptoUtils.decrypt(encrypted);
        return MessageSerializer.deserialize(decrypted);
    }
}