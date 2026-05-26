package com.naukma.network.packet;

import com.naukma.model.Warehouse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AddStockHandler implements PacketHandler<AddStockPacket> {
    private final Warehouse warehouse;

    @Override
    public String process(AddStockPacket packet) {
        warehouse.addStock(packet.productId(), packet.quantity());
        return "Added " + packet.quantity() + " units to product " + packet.productId();
    }

    @Override
    public Class<AddStockPacket> getPacketType() {
        return AddStockPacket.class;
    }
}