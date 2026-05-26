package com.naukma.network.packet;

public record AddProductToGroupPacket(String groupId, String productId) implements Packet {}
