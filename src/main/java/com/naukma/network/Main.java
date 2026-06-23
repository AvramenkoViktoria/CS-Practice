package com.naukma.network;

import com.naukma.config.Env;
import com.naukma.dao.UserDAO;
import com.naukma.db.DataSourceProvider;
import com.naukma.model.Warehouse;
import com.naukma.network.encryption.DecoderWorker;
import com.naukma.network.encryption.EncoderWorker;
import com.naukma.network.encryption.PacketDecoder;
import com.naukma.network.encryption.PacketEncoder;
import com.naukma.network.messaging.*;
import com.naukma.network.packet.Packet;
import com.naukma.network.server.StoreServerHTTP;
import com.naukma.service.UserService;

import java.sql.SQLException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class Main {

    private static final int TCP_PORT  = 9000;
    private static final int UDP_PORT  = 9001;
    private static final int HTTP_PORT = 9002;

    private static final int DECODER_THREADS   = 2;
    private static final int PROCESSOR_THREADS = 4;
    private static final int ENCODER_THREADS   = 2;
    private static final int SENDER_THREADS    = 1;

    static void main() throws Exception {

        String dbUrl  = Env.get("DB_URL", "jdbc:postgresql://localhost:5432/warehouse");
        String dbUser = Env.require("DB_USER");
        String dbPass = Env.get("DB_PASS", "");

        DataSourceProvider.init(dbUrl, dbUser, dbPass);

        BlockingQueue<RawMessage> rawQueue     = new LinkedBlockingQueue<>();
        BlockingQueue<Packet>     packetQueue  = new LinkedBlockingQueue<>();
        BlockingQueue<Message>    messageQueue = new LinkedBlockingQueue<>();
        BlockingQueue<byte[]>     encodedQueue = new LinkedBlockingQueue<>();

        RequestRegistry registry = new RequestRegistry();
        PacketDecoder decoder   = new PacketDecoder();
        MessageMapper mapper    = new MessageMapper();
        Warehouse     warehouse = Warehouse.createDefault();
        Processor     processor = new Processor(warehouse);
        PacketEncoder encoder   = new PacketEncoder();
        AtomicLong    packetIds = new AtomicLong(0);

        Receiver           receiver = new Receiver(TCP_PORT, UDP_PORT, rawQueue, registry);
        MultiProtocolSender sender  = new MultiProtocolSender(registry);

        UserService userService = new UserService(new UserDAO());
        seedUser(userService, Env.get("ADMIN_USERNAME"), Env.get("ADMIN_PASSWORD"));
        seedUser(userService, Env.get("SEED_USERNAME"),  Env.get("SEED_PASSWORD"));
        StoreServerHTTP httpServer = new StoreServerHTTP(HTTP_PORT, warehouse, userService);
        httpServer.start();

        ExecutorService decoderPool   = Executors.newFixedThreadPool(DECODER_THREADS,   namedFactory("Decoder"));
        ExecutorService processorPool = Executors.newFixedThreadPool(PROCESSOR_THREADS, namedFactory("Processor"));
        ExecutorService encoderPool   = Executors.newFixedThreadPool(ENCODER_THREADS,   namedFactory("Encoder"));
        ExecutorService senderPool    = Executors.newFixedThreadPool(SENDER_THREADS,    namedFactory("Sender"));

        for (int i = 0; i < DECODER_THREADS;   i++)
            decoderPool  .submit(new DecoderWorker  (rawQueue,    packetQueue,  decoder, mapper));
        for (int i = 0; i < PROCESSOR_THREADS; i++)
            processorPool.submit(new ProcessorWorker(packetQueue, messageQueue, processor));
        for (int i = 0; i < ENCODER_THREADS;   i++)
            encoderPool  .submit(new EncoderWorker  (messageQueue, encodedQueue, encoder, (byte) 1, packetIds));
        for (int i = 0; i < SENDER_THREADS;    i++)
            senderPool   .submit(new SenderWorker   (encodedQueue, sender));

        receiver.start();

        System.out.println("=================================================");
        System.out.println("  Warehouse Network Server started");
        System.out.printf ("  TCP  : %d%n", TCP_PORT);
        System.out.printf ("  UDP  : %d%n", UDP_PORT);
        System.out.printf ("  HTTP : http://localhost:%d/login, /products%n", HTTP_PORT);
        System.out.println("=================================================");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down…");
            receiver.stop();
            httpServer.stop();
            shutdownGracefully(senderPool,    "Sender");
            shutdownGracefully(encoderPool,   "Encoder");
            shutdownGracefully(processorPool, "Processor");
            shutdownGracefully(decoderPool,   "Decoder");
            DataSourceProvider.close();
            System.out.println("Shutdown complete.");
        }, "shutdown-hook"));

        Thread.currentThread().join();
    }

    private static void seedUser(UserService userService, String username, String password) throws SQLException {
        if (username == null || username.isBlank() || password == null || password.isBlank()) return;
        userService.registerIfAbsent(username, password);
    }

    private static void shutdownGracefully(ExecutorService pool, String name) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                System.out.println("Force-stopping " + name + " workers…");
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static ThreadFactory namedFactory(String name) {
        return r -> {
            Thread t = new Thread(r, name + "-" + (System.currentTimeMillis() % 10_000));
            t.setDaemon(false);
            return t;
        };
    }
}