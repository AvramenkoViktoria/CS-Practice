package com.naukma.model;

import java.util.HashMap;
import java.util.Map;

public class Warehouse {

    private final Map<String, Product> products = new HashMap<>();
    private final Map<String, ProductGroup> groups = new HashMap<>();

    public void addProduct(String id, String name, double price) {
        products.putIfAbsent(id, new Product(id, name, price, 0));
    }

    public Product getProduct(String id) {
        return products.get(id);
    }

    public int getStockQuantity(String productId) {
        Product p = products.get(productId);
        return p != null ? p.getQuantity() : 0;
    }

    public void addStock(String productId, int quantity) {
        Product p = products.get(productId);
        if (p != null) p.addStock(quantity);
    }

    public boolean deductStock(String productId, int quantity) {
        Product p = products.get(productId);
        return p != null && p.deductStock(quantity);
    }

    public void setPrice(String productId, double price) {
        Product p = products.get(productId);
        if (p != null) p.setPrice(price);
    }

    public void addGroup(String groupId, String groupName) {
        groups.putIfAbsent(groupId, new ProductGroup(groupId, groupName));
    }

    public void addProductToGroup(String groupId, String productId) {
        ProductGroup group = groups.get(groupId);
        if (group != null && productId != null) {
            group.addProduct(productId);
        }
    }
}
