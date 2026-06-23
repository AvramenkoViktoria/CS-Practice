package com.naukma.network.tests.http;

import com.naukma.dao.UserDAO;
import com.naukma.db.DataSourceProvider;
import com.naukma.network.http.auth.PasswordUtil;
import com.naukma.service.UserService;
import org.junit.jupiter.api.*;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class UserAuthTest {

    private UserService users;

    @BeforeAll
    static void initDb() {
        DataSourceProvider.init(
                "jdbc:h2:mem:usersdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
                "sa",
                ""
        );
    }

    @AfterAll
    static void closeDb() {
        DataSourceProvider.close();
    }

    @BeforeEach
    void setUp() throws SQLException {
        try (var c = DataSourceProvider.getConnection();
             var st = c.createStatement()) {
            st.executeUpdate("DELETE FROM users");
        }
        users = new UserService(new UserDAO());
    }

    @Test
    void hashIsNotPlaintextAndVerifies() {
        String hash = PasswordUtil.hash("secret");
        assertNotEquals("secret", hash, "Hash must not equal the raw password");
        assertTrue(hash.contains(":"), "Hash should be iterations:salt:hash");
        assertTrue(PasswordUtil.verify("secret", hash));
        assertFalse(PasswordUtil.verify("wrong", hash));
    }

    @Test
    void samePasswordProducesDifferentHashes() {
        String h1 = PasswordUtil.hash("secret");
        String h2 = PasswordUtil.hash("secret");
        assertNotEquals(h1, h2, "Salt should make hashes differ");
        assertTrue(PasswordUtil.verify("secret", h1));
        assertTrue(PasswordUtil.verify("secret", h2));
    }

    @Test
    void authenticateSucceedsForRegisteredUser() throws SQLException {
        users.register("admin", "secret");
        assertTrue(users.authenticate("admin", "secret"));
    }

    @Test
    void authenticateFailsForWrongPassword() throws SQLException {
        users.register("admin", "secret");
        assertFalse(users.authenticate("admin", "wrong"));
    }

    @Test
    void authenticateFailsForUnknownUser() throws SQLException {
        assertFalse(users.authenticate("ghost", "whatever"));
    }

    @Test
    void passwordIsStoredHashedInDb() throws SQLException {
        users.register("admin", "secret");
        String stored = users.findByUsername("admin").orElseThrow().getPasswordHash();
        assertNotEquals("secret", stored, "DB must not store the raw password");
        assertTrue(PasswordUtil.verify("secret", stored));
    }

    @Test
    void registerRejectsDuplicateUsername() throws SQLException {
        users.register("admin", "secret");
        assertThrows(IllegalArgumentException.class, () -> users.register("admin", "other"));
    }

    @Test
    void registerIfAbsentIsIdempotent() throws SQLException {
        users.registerIfAbsent("admin", "secret");
        users.registerIfAbsent("admin", "changed"); // no-op, must not overwrite
        assertTrue(users.authenticate("admin", "secret"));
        assertFalse(users.authenticate("admin", "changed"));
    }
}
