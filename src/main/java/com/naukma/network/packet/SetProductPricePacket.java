package com.naukma.network.packet;

public record SetProductPricePacket(String productId, double price) implements Packet {}
