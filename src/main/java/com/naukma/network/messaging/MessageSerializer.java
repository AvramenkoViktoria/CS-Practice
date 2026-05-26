package com.naukma.network.messaging;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MessageSerializer {

    public static byte[] serialize(Message message) {
        ByteBuffer buffer =
                ByteBuffer.allocate(
                        8 + message.getPayload().length
                );

        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(message.getType());
        buffer.putInt(message.getUserId());
        buffer.put(message.getPayload());

        return buffer.array();
    }

    public static Message deserialize(byte[] bytes) {
        ByteBuffer buffer =
                ByteBuffer.wrap(bytes);

        buffer.order(ByteOrder.BIG_ENDIAN);
        int type = buffer.getInt();
        int userId = buffer.getInt();

        byte[] payload =
                new byte[bytes.length - 8];

        buffer.get(payload);

        return new Message(type, userId, payload);
    }
}
