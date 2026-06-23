package com.naukma.network.http;

import com.naukma.network.http.auth.JwtUtil;
import com.naukma.service.UserService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.sql.SQLException;

/**
 * POST /login
 * Body: {"login":"admin","password":"secret"}
 * Response: {"token":"<jwt>"}
 */
@AllArgsConstructor
public class LoginHandler implements HttpHandler {

    private final UserService userService;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpHelper.sendJson(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            return;
        }

        String body = HttpHelper.readBody(exchange);

        String login    = extractField(body, "login");
        String password = extractField(body, "password");

        if (login == null || password == null) {
            HttpHelper.sendJson(exchange, 400, "{\"error\":\"Missing login or password\"}");
            return;
        }

        boolean valid;
        try {
            valid = userService.authenticate(login, password);
        } catch (SQLException e) {
            HttpHelper.sendJson(exchange, 500, "{\"error\":\"Internal server error\"}");
            return;
        }

        if (!valid) {
            HttpHelper.sendJson(exchange, 401, "{\"error\":\"Invalid credentials\"}");
            return;
        }

        String token = JwtUtil.generateToken(login);
        HttpHelper.sendJson(exchange, 200, "{\"token\":\"" + token + "\"}");
    }

    private String extractField(String json, String fieldName) {
        String key = "\"" + fieldName + "\"";
        int idx = json.indexOf(key);
        if (idx == -1) return null;
        int colon = json.indexOf(':', idx);
        if (colon == -1) return null;
        int start = json.indexOf('"', colon) + 1;
        int end   = json.indexOf('"', start);
        if (start <= 0 || end <= start) return null;
        return json.substring(start, end);
    }
}