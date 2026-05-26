package com.naukma.network.packet;

public record AddStockPacket(String productId, int quantity) implements Packet {}
