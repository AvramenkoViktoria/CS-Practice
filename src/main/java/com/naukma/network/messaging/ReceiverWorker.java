package com.naukma.network;

import lombok.AllArgsConstructor;

import java.util.concurrent.BlockingQueue;

@AllArgsConstructor
public class ReceiverWorker implements Runnable {
    private final MessageReceiver receiver;
    private final BlockingQueue<RawMessage> queue;

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                RawMessage message = receiver.receive();
                queue.put(message);
            }
        } catch (InterruptedException ignored) {
        }
    }
}