package com.naukma.network.packet;

import com.naukma.model.Warehouse;

import java.sql.SQLException;

public class DeductStockHandler extends AbstractPacketHandler<DeductStockPacket> {

    public DeductStockHandler(Warehouse warehouse) {
        super(warehouse);
    }

    @Override
    protected String doProcess(DeductStockPacket packet) throws SQLException {
        if (packet.quantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
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