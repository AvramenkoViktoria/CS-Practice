package com.naukma.network;

import com.naukma.network.packet.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MessageMapper {

    private final Map<Integer, Function<Message, Packet>> mappers = new HashMap<>();

    public MessageMapper() {
        registerMappers();
    }

    private void registerMappers() {
        mappers.put(1, msg -> new GetStockQuantityPacket(new String(msg.getPayload())));

        mappers.put(2, msg -> {
            String[] p = new String(msg.getPayload()).split(":");
            return new AddStockPacket(p[0], Integer.parseInt(p[1]));
        });

        mappers.put(3, msg -> {
            String[] p = new String(msg.getPayload()).split(":");
            return new DeductStockPacket(p[0], Integer.parseInt(p[1]));
        });

        mappers.put(4, msg -> {
            String[] p = new String(msg.getPayload()).split(":");
            return new AddProductGroupPacket(p[0], p[1]);
        });

        mappers.put(5, msg -> {
            String[] p = new String(msg.getPayload()).split(":");
            return new AddProductToGroupPacket(p[0], p[1]);
        });

        mappers.put(6, msg -> {
            String[] p = new String(msg.getPayload()).split(":");
            return new SetProductPricePacket(p[0], Double.parseDouble(p[1]));
        });
    }

    public Packet toPacket(Message message) {
        Function<Message, Packet> mapper = mappers.get(message.getType());
        if (mapper == null) {
            throw new RuntimeException("Unknown message type: " + message.getType());
        }
        return mapper.apply(message);
    }
}