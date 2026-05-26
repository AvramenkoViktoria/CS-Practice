package com.naukma.network;

import lombok.AllArgsConstructor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

@AllArgsConstructor
public class EncoderWorker implements Runnable {
    private final BlockingQueue<Message> input;

    private final BlockingQueue<byte[]> output;

    private final PacketEncoder encoder;

    private final byte src;

    private final AtomicLong packetIdGenerator;

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Message message = input.take();

                long packetId =
                        packetIdGenerator.incrementAndGet();

                byte[] encoded =
                        encoder.encode(
                                message,
                                src,
                                packetId
                        );

                output.put(encoded);
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}