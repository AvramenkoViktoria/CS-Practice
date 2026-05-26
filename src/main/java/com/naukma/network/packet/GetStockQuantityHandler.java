package com.naukma.network.packet;

import com.naukma.model.Warehouse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetStockQuantityHandler implements PacketHandler<GetStockQuantityPacket> {
    private final Warehouse warehouse;

    @Override
    public String process(GetStockQuantityPacket packet) {
        int qty = warehouse.getStockQuantity(packet.productId());
        return "Stock quantity for " + packet.productId() + ": " + qty;
    }

    @Override
    public Class<GetStockQuantityPacket> getPacketType() {
        return GetStockQuantityPacket.class;
    }
}