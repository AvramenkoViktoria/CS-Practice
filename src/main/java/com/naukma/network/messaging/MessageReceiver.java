package com.naukma.network.messaging;

public interface MessageReceiver {
    RawMessage receive() throws InterruptedException;
}
