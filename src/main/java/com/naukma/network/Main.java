package com.rogueS.network;

import com.rogueS.game.Processor;
import com.rogueS.model.GameState;
import com.rogueS.network.packet.Packet;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<RawMessage> rawQueue = new LinkedBlockingQueue<>();
        BlockingQueue<Packet> packetQueue = new LinkedBlockingQueue<>();
        BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();
        BlockingQueue<byte[]> encodedQueue = new LinkedBlockingQueue<>();

        MessageReceiver receiver = new FakeReceiver();
        Sender sender = new FakeSender();
        PacketDecoder decoder = new PacketDecoder();
        MessageMapper mapper = new MessageMapper();
        GameState state = new GameState();
        Processor processor = new Processor(state);
        PacketEncoder encoder = new PacketEncoder();
        AtomicLong packetIds = new AtomicLong(0);

        ExecutorService receiverExecutor = Executors.newFixedThreadPool(2);
        ExecutorService decoderExecutor = Executors.newFixedThreadPool(2);
        ExecutorService processorExecutor = Executors.newFixedThreadPool(4);
        ExecutorService encoderExecutor = Executors.newFixedThreadPool(3);
        ExecutorService senderExecutor = Executors.newFixedThreadPool(5);

        for (int i = 0; i < 2; i++) {
            receiverExecutor.submit(new ReceiverWorker(receiver, rawQueue));
        }
        for (int i = 0; i < 2; i++) {
            decoderExecutor.submit(new DecoderWorker(rawQueue, packetQueue, decoder, mapper));
        }
        for (int i = 0; i < 4; i++) {
            processorExecutor.submit(new ProcessorWorker(packetQueue, messageQueue, processor));
        }
        for (int i = 0; i < 3; i++) {
            encoderExecutor.submit(new EncoderWorker(messageQueue, encodedQueue, encoder, (byte) 1, packetIds));
        }
        for (int i = 0; i < 5; i++) {
            senderExecutor.submit(new SenderWorker(encodedQueue, sender));
        }
        Thread.sleep(8000);

        shutdownGracefully(receiverExecutor, "Receiver");
        shutdownGracefully(decoderExecutor, "Decoder");
        shutdownGracefully(processorExecutor, "Processor");
        shutdownGracefully(encoderExecutor, "Encoder");
        shutdownGracefully(senderExecutor, "Sender");

        System.out.println("Finished");
    }

    private static void shutdownGracefully(ExecutorService executor, String name) {
        System.out.println("Shutting down " + name + " workers...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}