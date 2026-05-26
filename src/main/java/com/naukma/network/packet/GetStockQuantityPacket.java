package com.naukma.network.packet;

public record GetStockQuantityPacket(String productId) implements Packet {}
