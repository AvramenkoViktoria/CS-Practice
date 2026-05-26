package com.naukma.network;

import lombok.AllArgsConstructor;

import java.util.concurrent.BlockingQueue;

@AllArgsConstructor
public class SenderWorker implements Runnable {
    private final BlockingQueue<byte[]> queue;

    private final Sender sender;

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                sender.send(queue.take());
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}