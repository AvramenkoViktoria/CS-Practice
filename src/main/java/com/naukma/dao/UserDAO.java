package com.naukma.dao;

import com.naukma.db.DataSourceProvider;
import com.naukma.model.User;

import java.sql.*;
import java.util.Optional;

public class UserDAO {

    public void insert(User u) throws SQLException {
        final String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        try (Connection c = DataSourceProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPasswordHash());
            ps.executeUpdate();
        }
    }

    public Optional<User> findByUsername(String username) throws SQLException {
        final String sql = "SELECT username, password_hash FROM users WHERE username = ?";
        try (Connection c = DataSourceProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getString("username"),
                rs.getString("password_hash")
        );
    }
}
