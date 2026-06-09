package com.naukma.network.packet;

import com.naukma.model.Warehouse;

import java.sql.SQLException;

public class AddProductToGroupHandler extends AbstractPacketHandler<AddProductToGroupPacket> {

    public AddProductToGroupHandler(Warehouse warehouse) {
        super(warehouse);
    }

    @Override
    protected String doProcess(AddProductToGroupPacket packet) throws SQLException {
        if (packet.groupId() == null || packet.productId() == null) {
            throw new IllegalArgumentException("Group ID and Product ID cannot be null");
        }

        warehouse.addProductToGroup(packet.groupId(), packet.productId());
        return "Product " + packet.productId() + " added to group " + packet.groupId();
    }

    @Override
    public Class<AddProductToGroupPacket> getPacketType() {
        return AddProductToGroupPacket.class;
    }
}