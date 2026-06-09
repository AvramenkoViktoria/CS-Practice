package com.naukma.network.packet;

import com.naukma.model.Warehouse;

import java.sql.SQLException;

public class GetStockQuantityHandler extends AbstractPacketHandler<GetStockQuantityPacket> {

    public GetStockQuantityHandler(Warehouse warehouse) {
        super(warehouse);
    }

    @Override
    protected String doProcess(GetStockQuantityPacket packet) throws SQLException {
        int qty = warehouse.getStockQuantity(packet.productId());
        return "Stock quantity for " + packet.productId() + ": " + qty;
    }

    @Override
    public Class<GetStockQuantityPacket> getPacketType() {
        return GetStockQuantityPacket.class;
    }
}