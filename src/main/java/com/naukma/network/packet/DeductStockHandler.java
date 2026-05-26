package com.naukma.network.packet;

import com.naukma.model.Warehouse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeductStockHandler implements PacketHandler<DeductStockPacket> {
    private final Warehouse warehouse;

    @Override
    public String process(DeductStockPacket packet) {
        boolean success = warehouse.deductStock(packet.productId(), packet.quantity());
        return success
                ? "Successfully deducted " + packet.quantity() + " from " + packet.productId()
                : "Error: Not enough stock for " + packet.productId();
    }

    @Override
    public Class<DeductStockPacket> getPacketType() {
        return DeductStockPacket.class;
    }
}