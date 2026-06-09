package com.naukma.network.packet;

import com.naukma.model.Warehouse;

import java.sql.SQLException;

public class SetProductPriceHandler extends AbstractPacketHandler<SetProductPricePacket> {

    public SetProductPriceHandler(Warehouse warehouse) {
        super(warehouse);
    }

    @Override
    protected String doProcess(SetProductPricePacket packet) throws SQLException {
        if (packet.price() < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }

        warehouse.setPrice(packet.productId(), packet.price());
        return "Price for " + packet.productId() + " set to " + packet.price();
    }

    @Override
    public Class<SetProductPricePacket> getPacketType() {
        return SetProductPricePacket.class;
    }
}