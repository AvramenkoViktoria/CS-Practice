package com.naukma.dao;

import com.naukma.db.DataSourceProvider;
import com.naukma.model.ProductGroup;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GroupDAO {

    public void insert(ProductGroup g) throws SQLException {
        final String sql = "INSERT INTO product_groups (id, name) VALUES (?, ?)";
        try (Connection c = DataSourceProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, g.getId());
            ps.setString(2, g.getName());
            ps.executeUpdate();
        }
    }

    public Optional<ProductGroup> findById(String id) throws SQLException {
        final String sql = "SELECT id, name FROM product_groups WHERE id = ?";
        try (Connection c = DataSourceProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                ProductGroup g = new ProductGroup(rs.getString("id"), rs.getString("name"));
                loadMembers(c, g);
                return Optional.of(g);
            }
        }
    }

    public List<ProductGroup> findAll() throws SQLException {
        final String sql = "SELECT id, name FROM product_groups ORDER BY name";
        List<ProductGroup> result = new ArrayList<>();
        try (Connection c = DataSourceProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ProductGroup g = new ProductGroup(rs.getString("id"), rs.getString("name"));
                loadMembers(c, g);
                result.add(g);
            }
        }
        return result;
    }

    public void update(ProductGroup g) throws SQLException {
        final String sql = "UPDATE product_groups SET name = ? WHERE id = ?";
        try (Connection c = DataSourceProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, g.getName());
            ps.setString(2, g.getId());
            ps.executeUpdate();
        }
    }

    public void delete(String id) throws SQLException {
        final String sql = "DELETE FROM product_groups WHERE id = ?";
        try (Connection c = DataSourceProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }

    public void addMember(String groupId, String productId) throws SQLException {
        final String sql =
                "INSERT INTO product_group_members (group_id, product_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection c = DataSourceProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, groupId);
            ps.setString(2, productId);
            ps.executeUpdate();
        }
    }

    public void removeMember(String groupId, String productId) throws SQLException {
        final String sql = "DELETE FROM product_group_members WHERE group_id = ? AND product_id = ?";
        try (Connection c = DataSourceProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, groupId);
            ps.setString(2, productId);
            ps.executeUpdate();
        }
    }

    private void loadMembers(Connection c, ProductGroup g) throws SQLException {
        final String sql = "SELECT product_id FROM product_group_members WHERE group_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, g.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) g.addProduct(rs.getString("product_id"));
            }
        }
    }
}