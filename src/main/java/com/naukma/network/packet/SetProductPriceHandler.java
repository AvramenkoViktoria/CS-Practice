package com.naukma.network.packet;

import com.naukma.model.Warehouse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SetProductPriceHandler implements PacketHandler<SetProductPricePacket> {
    private final Warehouse warehouse;

    @Override
    public String process(SetProductPricePacket packet) {
        warehouse.setPrice(packet.productId(), packet.price());
        return "Price for " + packet.productId() + " set to " + packet.price();
    }

    @Override
    public Class<SetProductPricePacket> getPacketType() {
        return SetProductPricePacket.class;
    }
}
