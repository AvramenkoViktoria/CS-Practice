package com.naukma.network.packet;

import com.naukma.model.Warehouse;

import java.sql.SQLException;

public class AddStockHandler extends AbstractPacketHandler<AddStockPacket> {

    public AddStockHandler(Warehouse warehouse) {
        super(warehouse);
    }

    @Override
    protected String doProcess(AddStockPacket packet) throws SQLException {
        if (packet.quantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        warehouse.addStock(packet.productId(), packet.quantity());
        return "Added " + packet.quantity() + " units to product " + packet.productId();
    }

    @Override
    public Class<AddStockPacket> getPacketType() {
        return AddStockPacket.class;
    }
}