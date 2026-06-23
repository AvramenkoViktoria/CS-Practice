package com.naukma.service;

import com.naukma.dao.UserDAO;
import com.naukma.model.User;
import com.naukma.network.http.auth.PasswordUtil;
import lombok.AllArgsConstructor;

import java.sql.SQLException;
import java.util.Optional;

@AllArgsConstructor
public class UserService {

    private final UserDAO dao;

    public void register(String username, String rawPassword) throws SQLException {
        requireNonBlank(username, "Username");
        requireNonBlank(rawPassword, "Password");
        if (dao.findByUsername(username).isPresent())
            throw new IllegalArgumentException("User already exists: " + username);
        dao.insert(new User(username, PasswordUtil.hash(rawPassword)));
    }

    public void registerIfAbsent(String username, String rawPassword) throws SQLException {
        requireNonBlank(username, "Username");
        requireNonBlank(rawPassword, "Password");
        if (dao.findByUsername(username).isEmpty())
            dao.insert(new User(username, PasswordUtil.hash(rawPassword)));
    }

    public boolean authenticate(String username, String rawPassword) throws SQLException {
        if (username == null || rawPassword == null) return false;
        return dao.findByUsername(username)
                .map(u -> PasswordUtil.verify(rawPassword, u.getPasswordHash()))
                .orElse(false);
    }

    public Optional<User> findByUsername(String username) throws SQLException {
        return dao.findByUsername(username);
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException(field + " must not be blank");
    }
}
