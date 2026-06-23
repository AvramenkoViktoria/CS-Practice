package com.naukma.network.tests.http;

import com.naukma.dao.UserDAO;
import com.naukma.db.DataSourceProvider;
import com.naukma.model.Warehouse;
import com.naukma.network.http.auth.JwtUtil;
import com.naukma.network.server.StoreServerHTTP;
import com.naukma.service.UserService;
import com.naukma.network.tests.util.DBUtil;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class RestApiTest {

    private static final int PORT = 8891;
    private static final String BASE_URL = "http://localhost:" + PORT;

    private static StoreServerHTTP server;
    private static Warehouse warehouse;
    private static String token;

    @BeforeAll
    static void startServer() throws IOException, SQLException {
        DataSourceProvider.init(
                "jdbc:h2:mem:restdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
                "sa", "");

        warehouse = Warehouse.createDefault();
        UserService users = new UserService(new UserDAO());
        users.register("tester", "pw123");

        server = new StoreServerHTTP(PORT, warehouse, users);
        server.start();

        token = JwtUtil.generateToken("tester");
    }

    @AfterAll
    static void stopServer() {
        if (server != null) server.stop();
        DataSourceProvider.close();
    }

    @BeforeEach
    void seedProduct() throws SQLException {
        DBUtil.clearDatabase();
        warehouse.addProduct("P001", "Laptop", 1000.0);
    }

    @Test
    void loginWithValidCredentialsReturnsToken() throws IOException {
        Response res = request("POST", "/login", "{\"login\":\"tester\",\"password\":\"pw123\"}", null);
        assertEquals(200, res.status);
        assertTrue(res.body.contains("token"), "Body should contain a token: " + res.body);
    }

    @Test
    void loginWithWrongPasswordIsRejected() throws IOException {
        Response res = request("POST", "/login", "{\"login\":\"tester\",\"password\":\"wrong\"}", null);
        assertEquals(401, res.status);
    }

    @Test
    void productsRequireAuthentication() throws IOException {
        Response res = request("GET", "/products/P001", null, null);
        assertEquals(401, res.status);
    }

    @Test
    void getExistingProductReturnsIt() throws IOException {
        Response res = request("GET", "/products/P001", null, token);
        assertEquals(200, res.status);
        assertTrue(res.body.contains("Laptop"), "Body should contain the product: " + res.body);
    }

    @Test
    void getMissingProductReturns404() throws IOException {
        Response res = request("GET", "/products/NOPE", null, token);
        assertEquals(404, res.status);
    }

    @Test
    void putCreatesProduct() throws IOException {
        Response res = request("PUT", "/products",
                "{\"id\":\"P002\",\"name\":\"Mouse\",\"price\":50}", token);
        assertEquals(201, res.status);
        assertEquals(200, request("GET", "/products/P002", null, token).status);
    }

    @Test
    void postUpdatesProduct() throws IOException {
        Response res = request("POST", "/products/P001", "{\"price\":1500}", token);
        assertEquals(200, res.status);
        assertTrue(request("GET", "/products/P001", null, token).body.contains("1500.00"));
    }

    @Test
    void deleteRemovesProduct() throws IOException {
        assertEquals(200, request("DELETE", "/products/P001", null, token).status);
        assertEquals(404, request("GET", "/products/P001", null, token).status);
    }

    private record Response(int status, String body) {}

    private Response request(String method, String path, String body, String token) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(BASE_URL + path).openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");
        if (token != null) conn.setRequestProperty("Authorization", "Bearer " + token);

        if (body != null) {
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
        }

        int status = conn.getResponseCode();
        InputStream is = status >= 400 ? conn.getErrorStream() : conn.getInputStream();
        String responseBody = "";
        if (is != null) {
            try (is) {
                responseBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
        return new Response(status, responseBody);
    }
}
