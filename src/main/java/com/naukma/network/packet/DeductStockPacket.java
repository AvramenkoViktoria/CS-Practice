package com.naukma.network.packet;

public record DeductStockPacket(String productId, int quantity) implements Packet {}
