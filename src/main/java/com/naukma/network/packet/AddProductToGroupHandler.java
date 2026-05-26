package com.naukma.network.packet;

import com.naukma.model.Warehouse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AddProductToGroupHandler implements PacketHandler<AddProductToGroupPacket> {
    private final Warehouse warehouse;

    @Override
    public String process(AddProductToGroupPacket packet) {
        warehouse.addProductToGroup(packet.groupId(), packet.productId());
        return "Product " + packet.productId() + " added to group " + packet.groupId();
    }

    @Override
    public Class<AddProductToGroupPacket> getPacketType() {
        return AddProductToGroupPacket.class;
    }
}
