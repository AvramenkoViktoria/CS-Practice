package com.naukma.network.packet;

public interface PacketHandler<T extends Packet> {
    String process(T packet);
    Class<T> getPacketType();
}
