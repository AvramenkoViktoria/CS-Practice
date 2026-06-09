package com.naukma.network.packet;

import com.naukma.network.messaging.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PacketMapper {

    private final Map<Class<? extends Packet>, Function<Packet, Message>> mappers = new HashMap<>();

    public PacketMapper() {
        registerMappers();
    }

    private void registerMappers() {
        mappers.put(GetStockQuantityPacket.class, p -> {
            GetStockQuantityPacket pkt = (GetStockQuantityPacket) p;
            return new Message(1, 1, pkt.productId().getBytes());
        });

        mappers.put(AddStockPacket.class, p -> {
            AddStockPacket pkt = (AddStockPacket) p;
            String payload = pkt.productId() + ":" + pkt.quantity();
            return new Message(2, 1, payload.getBytes());
        });

        mappers.put(DeductStockPacket.class, p -> {
            DeductStockPacket pkt = (DeductStockPacket) p;
            String payload = pkt.productId() + ":" + pkt.quantity();
            return new Message(3, 1, payload.getBytes());
        });

        mappers.put(AddProductGroupPacket.class, p -> {
            AddProductGroupPacket pkt = (AddProductGroupPacket) p;
            String payload = pkt.groupId() + ":" + pkt.groupName();
            return new Message(4, 1, payload.getBytes());
        });

        mappers.put(AddProductToGroupPacket.class, p -> {
            AddProductToGroupPacket pkt = (AddProductToGroupPacket) p;
            String payload = pkt.groupId() + ":" + pkt.productId();
            return new Message(5, 1, payload.getBytes());
        });

        mappers.put(SetProductPricePacket.class, p -> {
            SetProductPricePacket pkt = (SetProductPricePacket) p;
            String payload = pkt.productId() + ":" + pkt.price();
            return new Message(6, 1, payload.getBytes());
        });
    }

    public Message toMessage(Packet packet) {
        Function<Packet, Message> mapper = mappers.get(packet.getClass());
        if (mapper == null)
            throw new RuntimeException("Unknown packet type: " + packet.getClass().getSimpleName());
        return mapper.apply(packet);
    }
}