package com.naukma.network.messaging;

@FunctionalInterface
public interface ClientChannel {
    void send(byte[] responseBytes);
}