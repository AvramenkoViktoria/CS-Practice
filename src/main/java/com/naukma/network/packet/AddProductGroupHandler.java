package com.naukma.network.packet;

import com.naukma.model.Warehouse;

import java.sql.SQLException;

public class AddProductGroupHandler extends AbstractPacketHandler<AddProductGroupPacket> {

    public AddProductGroupHandler(Warehouse warehouse) {
        super(warehouse);
    }

    @Override
    protected String doProcess(AddProductGroupPacket packet) throws SQLException {
        if (packet.groupId() == null || packet.groupId().isBlank()) {
            throw new IllegalArgumentException("Group ID cannot be empty");
        }
        if (packet.groupName() == null || packet.groupName().isBlank()) {
            throw new IllegalArgumentException("Group name cannot be empty");
        }

        warehouse.addGroup(packet.groupId(), packet.groupName());
        return "Group created: " + packet.groupName() + " (ID: " + packet.groupId() + ")";
    }

    @Override
    public Class<AddProductGroupPacket> getPacketType() {
        return AddProductGroupPacket.class;
    }
}