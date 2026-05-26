package com.naukma.network.messaging;

import com.naukma.model.Warehouse;
import com.naukma.network.packet.*;

import java.util.HashMap;
import java.util.Map;

public class Processor {

    private final Warehouse warehouse;
    private final Map<Class<?>, PacketHandler<?>> handlers = new HashMap<>();

    public Processor(Warehouse warehouse) {
        this.warehouse = warehouse;
        registerHandlers();
    }

    private void registerHandlers() {
        register(new GetStockQuantityHandler(warehouse));
        register(new AddStockHandler(warehouse));
        register(new DeductStockHandler(warehouse));
        register(new AddProductGroupHandler(warehouse));
        register(new AddProductToGroupHandler(warehouse));
        register(new SetProductPriceHandler(warehouse));
    }

    @SuppressWarnings("unchecked")
    private <T extends Packet> void register(PacketHandler<T> handler) {
        handlers.put(handler.getPacketType(), handler);
    }

    public String process(Packet packet) {
        @SuppressWarnings("unchecked")
        PacketHandler<Packet> handler = (PacketHandler<Packet>) handlers.get(packet.getClass());

        if (handler != null) {
            System.out.println("PROCESSING: " + handler.getPacketType());
            return handler.process(packet);
        }
        return "Unknown packet: " + packet.getClass().getSimpleName();
    }
}
