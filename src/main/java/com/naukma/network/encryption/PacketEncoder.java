package com.naukma.network.encryption;

import com.naukma.network.messaging.Message;
import com.naukma.network.messaging.MessageSerializer;
import util.CryptoUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PacketEncoder {

    public byte[] encode(Message message, byte src, long packetId) throws Exception {
        byte[] serialized = MessageSerializer.serialize(message);
        byte[] encrypted = CryptoUtils.encrypt(serialized);

        ByteBuffer headerBuffer = ByteBuffer.allocate(14);
        headerBuffer.order(ByteOrder.BIG_ENDIAN);

        headerBuffer.put((byte) 0x13);        // magic
        headerBuffer.put(src);                // source
        headerBuffer.putLong(packetId);       // packet id
        headerBuffer.putInt(encrypted.length);// length

        byte[] headerBytes = headerBuffer.array();
        short headerCrc = Crc16.calculate(headerBytes);

        ByteBuffer packet = ByteBuffer.allocate(16 + encrypted.length + 2);
        packet.order(ByteOrder.BIG_ENDIAN);

        packet.put(headerBytes);
        packet.putShort(headerCrc);

        packet.put(encrypted);
        packet.putShort(Crc16.calculate(encrypted));

        return packet.array();
    }
}
