package com.naukma.dao;

import com.naukma.db.DataSourceProvider;
import com.naukma.model.Page;
import com.naukma.model.Product;
import com.naukma.model.ProductFilter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDAO {

    public void insert(Product p) throws SQLException {
        final String sql = "INSERT INTO products (id, name, price, quantity) VALUES (?, ?, ?, ?)";
        try (Connection c = DataSourceProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getId());
            ps.setString(2, p.getName());
            ps.setDouble(3, p.getPrice());
            ps.setInt(4, p.getQuantity());
            ps.executeUpdate();
        }
    }

    public Optional<Product> findById(String id) throws SQLException {
        final String sql = "SELECT id, name, price, quantity FROM products WHERE id = ?";
        try (Connection c = DataSourceProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public void update(Product p) throws SQLException {
        final String sql = "UPDATE products SET name = ?, price = ?, quantity = ? WHERE id = ?";
        try (Connection c = DataSourceProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setDouble(2, p.getPrice());
            ps.setInt(3, p.getQuantity());
            ps.setString(4, p.getId());
            ps.executeUpdate();
        }
    }

    public void delete(String id) throws SQLException {
        final String sql = "DELETE FROM products WHERE id = ?";
        try (Connection c = DataSourceProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }

    public void addStock(String id, int qty) throws SQLException {
        final String sql = "UPDATE products SET quantity = quantity + ? WHERE id = ?";
        try (Connection c = DataSourceProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setString(2, id);
            ps.executeUpdate();
        }
    }

    public boolean deductStock(String id, int qty) throws SQLException {
        final String sql = "UPDATE products SET quantity = quantity - ? WHERE id = ? AND quantity >= ?";
        try (Connection c = DataSourceProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setString(2, id);
            ps.setInt(3, qty);
            return ps.executeUpdate() == 1;
        }
    }

    public void setPrice(String id, double price) throws SQLException {
        final String sql = "UPDATE products SET price = ? WHERE id = ?";
        try (Connection c = DataSourceProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, price);
            ps.setString(2, id);
            ps.executeUpdate();
        }
    }

    public Page<Product> search(ProductFilter filter) throws SQLException {
        QueryBuilder qb = new QueryBuilder(filter);

        long total = countRows(qb);
        List<Product> content = fetchRows(qb, filter);
        return new Page<>(content, filter.getPage(), filter.getPageSize(), total);
    }

    private long countRows(QueryBuilder qb) throws SQLException {
        String sql = "SELECT COUNT(*) " + qb.fromAndWhere();
        try (Connection c = DataSourceProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            qb.bind(ps, 1);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private List<Product> fetchRows(QueryBuilder qb, ProductFilter f) throws SQLException {
        String sql = "SELECT p.id, p.name, p.price, p.quantity "
                + qb.fromAndWhere()
                + " ORDER BY p.name"
                + " LIMIT ? OFFSET ?";
        try (Connection c = DataSourceProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int idx = qb.bind(ps, 1);
            ps.setInt(idx++, f.getPageSize());
            ps.setInt(idx,   f.getOffset());
            List<Product> results = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(map(rs));
            }
            return results;
        }
    }

    private Product map(ResultSet rs) throws SQLException {
        return new Product(
                rs.getString("id"),
                rs.getString("name"),
                rs.getDouble("price"),
                rs.getInt("quantity")
        );
    }

    static class QueryBuilder {
        private final StringBuilder fromWhere = new StringBuilder();
        private final List<Object> params = new ArrayList<>();

        QueryBuilder(ProductFilter f) {
            boolean joinGroup = f.getGroupId() != null;

            fromWhere.append("FROM products p");
            if (joinGroup) {
                fromWhere.append(" JOIN product_group_members pgm ON pgm.product_id = p.id");
            }

            List<String> conditions = new ArrayList<>();

            if (f.getNameContains() != null) {
                conditions.add("p.name ILIKE ?");
                params.add("%" + f.getNameContains() + "%");
            }
            if (joinGroup) {
                conditions.add("pgm.group_id = ?");
                params.add(f.getGroupId());
            }
            if (f.getMinPrice() != null) {
                conditions.add("p.price >= ?");
                params.add(f.getMinPrice());
            }
            if (f.getMaxPrice() != null) {
                conditions.add("p.price <= ?");
                params.add(f.getMaxPrice());
            }
            if (f.getMinQuantity() != null) {
                conditions.add("p.quantity >= ?");
                params.add(f.getMinQuantity());
            }
            if (f.getMaxQuantity() != null) {
                conditions.add("p.quantity <= ?");
                params.add(f.getMaxQuantity());
            }

            if (!conditions.isEmpty()) {
                fromWhere.append(" WHERE ").append(String.join(" AND ", conditions));
            }
        }

        String fromAndWhere() {
            return fromWhere.toString();
        }

        int bind(PreparedStatement ps, int startIndex) throws SQLException {
            int i = startIndex;
            for (Object p : params) {
                if (p instanceof String s)  ps.setString(i++, s);
                else if (p instanceof Double d)  ps.setDouble(i++, d);
                else if (p instanceof Integer n) ps.setInt(i++, n);
                else ps.setObject(i++, p);
            }
            return i;
        }
    }
}