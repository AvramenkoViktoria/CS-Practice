package com.naukma.network.tests.lab_three;

import com.naukma.db.DataSourceProvider;
import com.naukma.network.client.StoreClientTCP;
import com.naukma.network.client.StoreClientUDP;
import com.naukma.network.packet.GetStockQuantityPacket;
import com.naukma.network.packet.AddStockPacket;
import com.naukma.network.server.StoreServerTCP;
import com.naukma.network.server.StoreServerUDP;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StoreNetworkTest {

    @BeforeAll
    static void initDatabase() {
        DataSourceProvider.init(
                "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
                "sa",
                ""
        );
        System.out.println("Test H2 database initialized.");
    }

    @Test
    @Order(1)
    void udp_successfulSendWhenServerAvailable() throws Exception {
        int port = 19001;
        StoreServerUDP server = new StoreServerUDP(port);
        ExecutorService serverThread = Executors.newSingleThreadExecutor();
        serverThread.submit(server::start);
        Thread.sleep(200);

        StoreClientUDP client = new StoreClientUDP("localhost", port);
        assertDoesNotThrow(() -> client.send(new GetStockQuantityPacket("prod1")));

        client.close();
        server.stop();
        serverThread.shutdownNow();
    }

    @Test
    @Order(2)
    void udp_clientRetriesOnPacketLoss() throws Exception {
        int port = 19002;
        AtomicInteger receivedCount = new AtomicInteger(0);

        ExecutorService stubThread = Executors.newSingleThreadExecutor();
        CountDownLatch serverReady = new CountDownLatch(1);

        stubThread.submit(() -> {
            try (DatagramSocket serverSocket = new DatagramSocket(port)) {
                serverSocket.setSoTimeout(5000);
                serverReady.countDown();

                byte[] buf = new byte[4096];

                DatagramPacket drop = new DatagramPacket(buf, buf.length);
                serverSocket.receive(drop);
                receivedCount.incrementAndGet();

                DatagramPacket real = new DatagramPacket(buf, buf.length);
                serverSocket.receive(real);
                receivedCount.incrementAndGet();

                DatagramPacket response = new DatagramPacket(
                        real.getData(), real.getLength(),
                        real.getAddress(), real.getPort());
                serverSocket.send(response);
            } catch (IOException ignored) {}
        });

        assertTrue(serverReady.await(3, TimeUnit.SECONDS), "Stub server did not start in time");

        StoreClientUDP client = new StoreClientUDP("localhost", port);
        try {
            client.send(new GetStockQuantityPacket("prod1"));
        } catch (Exception ignored) {}

        client.close();
        stubThread.shutdownNow();

        assertTrue(receivedCount.get() >= 2,
                "Client should have retried at least once, but server received only "
                        + receivedCount.get() + " packet(s)");
    }

    @Test
    @Order(3)
    void udp_clientStopsAfterMaxRetries() {
        int port = 19003;
        StoreClientUDP client = new StoreClientUDP("localhost", port);

        long start = System.currentTimeMillis();
        assertDoesNotThrow(() -> client.send(new GetStockQuantityPacket("prod1")));
        long elapsed = System.currentTimeMillis() - start;

        client.close();

        assertTrue(elapsed < 10_000,
                "send() blocked for too long: " + elapsed + " ms");
    }

    @Test
    @Order(4)
    void tcp_successfulSendWhenServerAvailable() throws Exception {
        int port = 19010;
        StoreServerTCP server = new StoreServerTCP(port);
        ExecutorService serverThread = Executors.newSingleThreadExecutor();
        serverThread.submit(server::start);
        Thread.sleep(300);

        StoreClientTCP client = new StoreClientTCP("localhost", port);
        client.connect();

        assertDoesNotThrow(() -> client.send(new GetStockQuantityPacket("prod1")));

        client.disconnect();
        server.stop();
        serverThread.shutdownNow();
    }

    @Test
    @Order(5)
    void tcp_clientDoesNotSendWhenServerUnavailable() {
        int port = 19011;
        StoreClientTCP client = new StoreClientTCP("localhost", port);
        client.connect();

        long start = System.currentTimeMillis();
        assertDoesNotThrow(() -> client.send(new GetStockQuantityPacket("prod1")));
        long elapsed = System.currentTimeMillis() - start;

        client.disconnect();

        assertTrue(elapsed < 3000,
                "send() blocked the thread for " + elapsed + " ms while server was unavailable");
    }

    @Test
    @Order(6)
    void tcp_clientSchedulesReconnectOnConnectionFailure() throws Exception {
        int port = 19012;
        StoreClientTCP client = new StoreClientTCP("localhost", port);
        assertDoesNotThrow(client::connect);
        Thread.sleep(500);
        assertDoesNotThrow(client::disconnect);
    }

    @Test
    @Order(7)
    void tcp_clientReconnectsAfterServerRestart() throws Exception {
        int port = 19013;

        StoreServerTCP server1 = new StoreServerTCP(port);
        ExecutorService thread1 = Executors.newSingleThreadExecutor();
        thread1.submit(server1::start);
        Thread.sleep(300);

        StoreClientTCP client = new StoreClientTCP("localhost", port);
        client.connect();

        assertDoesNotThrow(() -> client.send(new AddStockPacket("prod1", 5)));

        server1.stop();
        thread1.shutdownNow();
        Thread.sleep(200);

        assertDoesNotThrow(() -> client.send(new GetStockQuantityPacket("prod1")));

        StoreServerTCP server2 = new StoreServerTCP(port);
        ExecutorService thread2 = Executors.newSingleThreadExecutor();
        thread2.submit(server2::start);
        Thread.sleep(6000);

        assertDoesNotThrow(() -> client.send(new GetStockQuantityPacket("prod1")));

        client.disconnect();
        server2.stop();
        thread2.shutdownNow();
    }

    @Test
    @Order(8)
    void tcp_clientHandlesMultipleConsecutiveFailures() {
        int port = 19014;
        StoreClientTCP client = new StoreClientTCP("localhost", port);

        for (int i = 0; i < 3; i++)
            assertDoesNotThrow(client::connect,
                    "connect() threw an exception on attempt " + (i + 1));

        assertDoesNotThrow(client::disconnect);
    }
}