package com.naukma.network.server;

import com.naukma.model.Warehouse;
import com.naukma.network.http.LoginHandler;
import com.naukma.network.http.ProductHandler;
import com.naukma.service.UserService;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class StoreServerHTTP {

    private final int port;
    private final Warehouse warehouse;
    private final UserService userService;
    private HttpServer server;

    public StoreServerHTTP(int port, Warehouse warehouse, UserService userService) {
        this.port = port;
        this.warehouse = warehouse;
        this.userService = userService;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/login", new LoginHandler(userService));
        server.createContext("/products", new ProductHandler(warehouse));
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("HTTP (REST API) Server started on port " + port);
    }

    public void stop() {
        if (server != null) server.stop(0);
    }
}
