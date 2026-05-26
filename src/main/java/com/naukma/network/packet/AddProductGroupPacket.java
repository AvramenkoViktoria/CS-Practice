package com.naukma.network.packet;

public record AddProductGroupPacket(String groupId, String groupName) implements Packet {}
