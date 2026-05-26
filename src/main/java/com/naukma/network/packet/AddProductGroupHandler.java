package com.naukma.network.packet;

import com.naukma.model.Warehouse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AddProductGroupHandler implements PacketHandler<AddProductGroupPacket> {
    private final Warehouse warehouse;

    @Override
    public String process(AddProductGroupPacket packet) {
        warehouse.addGroup(packet.groupId(), packet.groupName());
        return "Group created: " + packet.groupName() + " (ID: " + packet.groupId() + ")";
    }

    @Override
    public Class<AddProductGroupPacket> getPacketType() {
        return AddProductGroupPacket.class;
    }
}
