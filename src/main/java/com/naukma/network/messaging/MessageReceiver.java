package com.naukma.network;

public interface MessageReceiver {
    RawMessage receive() throws InterruptedException;
}
