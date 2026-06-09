package com.naukma.service;

import com.naukma.dao.ProductDAO;
import com.naukma.model.Page;
import com.naukma.model.Product;
import com.naukma.model.ProductFilter;

import java.sql.SQLException;
import java.util.Optional;

public class ProductService {

    private final ProductDAO dao;

    public ProductService(ProductDAO dao) {
        this.dao = dao;
    }

    public void create(Product product) throws SQLException {
        dao.insert(product);
    }

    public Optional<Product> findById(String id) throws SQLException {
        return dao.findById(id);
    }

    public void update(Product product) throws SQLException {
        dao.update(product);
    }

    public void delete(String id) throws SQLException {
        dao.delete(id);
    }

    public void addStock(String id, int qty) throws SQLException {
        dao.addStock(id, qty);
    }

    public boolean deductStock(String id, int qty) throws SQLException {
        return dao.deductStock(id, qty);
    }

    public void setPrice(String id, double price) throws SQLException {
        dao.setPrice(id, price);
    }

    public Page<Product> search(ProductFilter filter) throws SQLException {
        return dao.search(filter);
    }
}