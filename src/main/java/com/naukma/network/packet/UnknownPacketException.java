package com.naukma.network.packet;

public class UnknownPacketException extends RuntimeException {
    public UnknownPacketException(String message) {
        super(message);
    }
}
