package com.naukma.network;

import util.CryptoUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class PacketEncoder {

    public byte[] encode(Message message, byte src, long packetId) throws Exception {
        byte[] serialized = MessageSerializer.serialize(message);
        byte[] encrypted = CryptoUtils.encrypt(serialized);

        ByteBuffer header = ByteBuffer.allocate(16);
        header.order(ByteOrder.BIG_ENDIAN);

        header.put((byte) 0x13);        // magic
        header.put(src);                // source
        header.putLong(packetId);       // packet id
        header.putInt(encrypted.length); // length

        byte[] headerForCrc = Arrays.copyOf(header.array(), 14);
        short headerCrc = Crc16.calculate(headerForCrc);

        ByteBuffer packet = ByteBuffer.allocate(16 + encrypted.length + 2);
        packet.order(ByteOrder.BIG_ENDIAN);

        packet.put((byte) 0x13);
        packet.put(src);
        packet.putLong(packetId);
        packet.putInt(encrypted.length);
        packet.putShort(headerCrc);

        packet.put(encrypted);
        packet.putShort(Crc16.calculate(encrypted));

        return packet.array();
    }
}
