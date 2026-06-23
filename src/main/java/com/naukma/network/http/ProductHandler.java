package com.naukma.network.http;

import com.naukma.model.Product;
import com.naukma.model.Warehouse;
import com.naukma.network.http.auth.Authenticator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Optional;

/**
 * GET    /products/{id}
 * PUT    /products
 * POST   /products/{id}
 * DELETE /products/{id}
 */
@AllArgsConstructor
public class ProductHandler implements HttpHandler {

    private final Warehouse warehouse;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!Authenticator.authenticate(exchange)) return;

        String method = exchange.getRequestMethod().toUpperCase();
        String path   = exchange.getRequestURI().getPath();

        try {
            switch (method) {
                case "GET"    -> handleGet(exchange, path);
                case "PUT"    -> handlePut(exchange);
                case "POST"   -> handlePost(exchange, path);
                case "DELETE" -> handleDelete(exchange, path);
                default       -> HttpHelper.sendJson(exchange, 405,
                        "{\"error\":\"Method Not Allowed\"}");
            }
        } catch (Exception e) {
            HttpHelper.sendJson(exchange, 500,
                    "{\"error\":\"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    // ── GET /products/{id} ───────────────────────────────────────────────
    private void handleGet(HttpExchange exchange, String path) throws Exception {
        String id = HttpHelper.extractId(exchange);
        Optional<Product> product = warehouse.getProduct(id);

        if (product.isEmpty()) {
            HttpHelper.sendJson(exchange, 404, "{\"error\":\"Product not found\"}");
            return;
        }
        HttpHelper.sendJson(exchange, 200, toJson(product.get()));
    }

    // ── PUT /products ────────────────────────────────────────────────────
    private void handlePut(HttpExchange exchange) throws Exception {
        String body = HttpHelper.readBody(exchange);

        String id    = extractField(body, "id");
        String name  = extractField(body, "name");
        String price = extractField(body, "price");

        if (id == null || name == null || price == null) {
            HttpHelper.sendJson(exchange, 400,
                    "{\"error\":\"Fields id, name, price are required\"}");
            return;
        }

        if (warehouse.getProduct(id).isPresent()) {
            HttpHelper.sendJson(exchange, 409,
                    "{\"error\":\"Product with this id already exists\"}");
            return;
        }

        warehouse.addProduct(id, name, Double.parseDouble(price));
        HttpHelper.sendJson(exchange, 201,
                "{\"message\":\"Product created\",\"id\":\"" + id + "\"}");
    }

    // ── POST /products/{id} ──────────────────────────────────────────────
    private void handlePost(HttpExchange exchange, String path) throws Exception {
        String id   = HttpHelper.extractId(exchange);
        String body = HttpHelper.readBody(exchange);

        Optional<Product> existing = warehouse.getProduct(id);
        if (existing.isEmpty()) {
            HttpHelper.sendJson(exchange, 404, "{\"error\":\"Product not found\"}");
            return;
        }

        String priceStr = extractField(body, "price");
        if (priceStr != null) {
            warehouse.setPrice(id, Double.parseDouble(priceStr));
        }

        String quantityStr = extractField(body, "quantity");
        if (quantityStr != null) {
            int qty = Integer.parseInt(quantityStr);
            if (qty >= 0) warehouse.addStock(id, qty);
        }

        HttpHelper.sendJson(exchange, 200,
                "{\"message\":\"Product updated\",\"id\":\"" + id + "\"}");
    }

    // ── DELETE /products/{id} ────────────────────────────────────────────
    private void handleDelete(HttpExchange exchange, String path) throws Exception {
        String id = HttpHelper.extractId(exchange);

        if (warehouse.getProduct(id).isEmpty()) {
            HttpHelper.sendJson(exchange, 404, "{\"error\":\"Product not found\"}");
            return;
        }

        warehouse.deleteProduct(id);
        HttpHelper.sendJson(exchange, 200,
                "{\"message\":\"Product deleted\",\"id\":\"" + id + "\"}");
    }

    // ── Допоміжні методи ─────────────────────────────────────────────────
    private String toJson(Product p) {
        return String.format(
                "{\"id\":\"%s\",\"name\":\"%s\",\"price\":%.2f,\"quantity\":%d}",
                p.getId(), p.getName(), p.getPrice(), p.getQuantity()
        );
    }

    private String extractField(String json, String fieldName) {
        String key = "\"" + fieldName + "\"";
        int idx = json.indexOf(key);
        if (idx == -1) return null;
        int colon = json.indexOf(':', idx);
        if (colon == -1) return null;
        int afterColon = colon + 1;
        while (afterColon < json.length() && json.charAt(afterColon) == ' ') afterColon++;
        if (json.charAt(afterColon) == '"') {
            int start = afterColon + 1;
            int end   = json.indexOf('"', start);
            return json.substring(start, end);
        } else {
            int end = afterColon;
            while (end < json.length() && ",}".indexOf(json.charAt(end)) == -1) end++;
            return json.substring(afterColon, end).trim();
        }
    }
}
