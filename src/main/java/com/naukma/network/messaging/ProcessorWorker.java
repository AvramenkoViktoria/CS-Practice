package com.naukma.network.messaging;
import com.naukma.network.packet.Packet;
import lombok.AllArgsConstructor;

import java.util.concurrent.BlockingQueue;

@AllArgsConstructor
public class ProcessorWorker implements Runnable {

    private final BlockingQueue<Packet> input;
    private final BlockingQueue<Message> output;
    private final Processor processor;

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Packet packet = input.take();
                String response = processor.process(packet);
                Message message = new Message(200, 0, response.getBytes());
                output.put(message);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}