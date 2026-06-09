package com.naukma.network;

import com.naukma.model.Warehouse;
import com.naukma.network.encryption.DecoderWorker;
import com.naukma.network.encryption.EncoderWorker;
import com.naukma.network.encryption.PacketDecoder;
import com.naukma.network.encryption.PacketEncoder;
import com.naukma.network.messaging.*;
import com.naukma.network.packet.Packet;

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
        Warehouse warehouse = Warehouse.createDefault();
        Processor processor = new Processor(warehouse);
        PacketEncoder encoder = new PacketEncoder();
        AtomicLong packetIds = new AtomicLong(0);

        ExecutorService receiverExecutor = Executors.newFixedThreadPool(2, namedThreadFactory("Receiver"));
        ExecutorService decoderExecutor = Executors.newFixedThreadPool(2, namedThreadFactory("Decoder"));
        ExecutorService processorExecutor = Executors.newFixedThreadPool(4, namedThreadFactory("Processor"));
        ExecutorService encoderExecutor = Executors.newFixedThreadPool(3, namedThreadFactory("Encoder"));
        ExecutorService senderExecutor = Executors.newFixedThreadPool(5, namedThreadFactory("Sender"));

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

        System.out.println("System started. Running for 8 seconds...");
        Thread.sleep(8000);

        shutdownGracefully(receiverExecutor, "Receiver");
        shutdownGracefully(decoderExecutor, "Decoder");
        shutdownGracefully(processorExecutor, "Processor");
        shutdownGracefully(encoderExecutor, "Encoder");
        shutdownGracefully(senderExecutor, "Sender");

        System.out.println("System finished.");
    }

    private static void shutdownGracefully(ExecutorService executor, String name) {
        System.out.println("Shutting down " + name + " workers...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                System.out.println("Forcing shutdown of " + name);
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static ThreadFactory namedThreadFactory(String name) {
        return r -> new Thread(r, name + "-" + System.currentTimeMillis() % 10000);
    }
}