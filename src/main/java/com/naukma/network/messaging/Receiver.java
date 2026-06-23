package com.naukma.network.messaging;

import com.naukma.network.server.StoreServerTCP;
import com.naukma.network.server.StoreServerUDP;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class Receiver {

    private static final Logger log = Logger.getLogger(Receiver.class.getName());

    private final int tcpPort;
    private final int udpPort;

    private final BlockingQueue<RawMessage> rawQueue;
    private final RequestRegistry registry;

    private final ExecutorService serverExecutor = Executors.newFixedThreadPool(2, namedFactory("server"));

    private volatile boolean running = false;

    private StoreServerTCP tcpServer;
    private StoreServerUDP udpServer;

    public Receiver(int tcpPort, int udpPort,
                    BlockingQueue<RawMessage> rawQueue,
                    RequestRegistry registry) {
        this.tcpPort  = tcpPort;
        this.udpPort  = udpPort;
        this.rawQueue = rawQueue;
        this.registry = registry;
    }

    public void start() {
        running = true;

        tcpServer = new StoreServerTCP(tcpPort, rawQueue, registry);
        udpServer = new StoreServerUDP(udpPort, rawQueue, registry);

        serverExecutor.submit(() -> {
            try {
                tcpServer.start();
            } catch (IOException e) {
                log.severe("TCP server error: " + e.getMessage());
            }
        });

        serverExecutor.submit(() -> {
            try {
                udpServer.start();
            } catch (SocketException e) {
                log.severe("UDP server error: " + e.getMessage());
            }
        });

        log.info("Receiver started — TCP:" + tcpPort + "  UDP:" + udpPort);
    }

    public void stop() {
        running = false;

        if (tcpServer != null) tcpServer.stop();
        if (udpServer != null) udpServer.stop();

        serverExecutor.shutdownNow();

        log.info("Receiver stopped.");
    }

    private static ThreadFactory namedFactory(String prefix) {
        return r -> {
            Thread t = new Thread(r, prefix + "-" + (System.currentTimeMillis() % 10_000));
            t.setDaemon(false);
            return t;
        };
    }
}
