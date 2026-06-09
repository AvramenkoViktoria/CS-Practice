package com.naukma.model;

import com.naukma.dao.GroupDAO;
import com.naukma.dao.ProductDAO;
import com.naukma.service.GroupService;
import com.naukma.service.ProductService;
import lombok.AllArgsConstructor;

import java.sql.SQLException;
import java.util.Optional;

@AllArgsConstructor
public class Warehouse {

    private final ProductService productService;
    private final GroupService   groupService;

    public static Warehouse createDefault() {
        return new Warehouse(
                new ProductService(new ProductDAO()),
                new GroupService(new GroupDAO())
        );
    }

    public void addProduct(String id, String name, double price) throws SQLException {
        requireNonBlank(id,   "Product id");
        requireNonBlank(name, "Product name");
        requireNonNegative(price, "Price");

        if (productService.findById(id).isPresent()) return;
        productService.create(new Product(id, name, price, 0));
    }

    public Optional<Product> getProduct(String id) throws SQLException {
        requireNonBlank(id, "Product id");
        return productService.findById(id);
    }

    public void updateProduct(String id, String name, double price) throws SQLException {
        requireNonBlank(id,   "Product id");
        requireNonBlank(name, "Product name");
        requireNonNegative(price, "Price");

        Product p = requireExists(id);
        p.setName(name);
        p.setPrice(price);
        productService.update(p);
    }

    public void deleteProduct(String id) throws SQLException {
        requireNonBlank(id, "Product id");
        productService.delete(id);
    }

    public int getStockQuantity(String productId) throws SQLException {
        requireNonBlank(productId, "Product id");
        return productService.findById(productId)
                .map(Product::getQuantity)
                .orElse(0);
    }

    public void addStock(String productId, int quantity) throws SQLException {
        requireNonBlank(productId, "Product id");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity to add must be > 0");
        requireExists(productId);
        productService.addStock(productId, quantity);
    }

    public boolean deductStock(String productId, int quantity) throws SQLException {
        requireNonBlank(productId, "Product id");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity to deduct must be > 0");
        requireExists(productId);
        return productService.deductStock(productId, quantity);
    }

    public void setPrice(String productId, double price) throws SQLException {
        requireNonBlank(productId, "Product id");
        requireNonNegative(price, "Price");
        requireExists(productId);
        productService.setPrice(productId, price);
    }

    public Page<Product> searchProducts(ProductFilter filter) throws SQLException {
        if (filter == null) throw new IllegalArgumentException("Filter must not be null");
        if (filter.getMinPrice() != null && filter.getMinPrice() < 0)
            throw new IllegalArgumentException("minPrice must be >= 0");
        if (filter.getMaxPrice() != null && filter.getMaxPrice() < 0)
            throw new IllegalArgumentException("maxPrice must be >= 0");
        if (filter.getMinPrice() != null && filter.getMaxPrice() != null
                && filter.getMinPrice() > filter.getMaxPrice())
            throw new IllegalArgumentException("minPrice must be <= maxPrice");
        if (filter.getMinQuantity() != null && filter.getMinQuantity() < 0)
            throw new IllegalArgumentException("minQuantity must be >= 0");
        return productService.search(filter);
    }

    public void addGroup(String groupId, String groupName) throws SQLException {
        requireNonBlank(groupId,   "Group id");
        requireNonBlank(groupName, "Group name");
        if (groupService.findById(groupId).isPresent()) return;
        groupService.create(new ProductGroup(groupId, groupName));
    }

    public void updateGroup(String groupId, String groupName) throws SQLException {
        requireNonBlank(groupId,   "Group id");
        requireNonBlank(groupName, "Group name");
        ProductGroup g = groupService.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));
        g.setName(groupName);
        groupService.update(g);
    }

    public void deleteGroup(String groupId) throws SQLException {
        requireNonBlank(groupId, "Group id");
        groupService.delete(groupId);
    }

    public void addProductToGroup(String groupId, String productId) throws SQLException {
        requireNonBlank(groupId,   "Group id");
        requireNonBlank(productId, "Product id");
        if (groupService.findById(groupId).isEmpty())
            throw new IllegalArgumentException("Group not found: " + groupId);
        if (productService.findById(productId).isEmpty())
            throw new IllegalArgumentException("Product not found: " + productId);
        groupService.addMember(groupId, productId);
    }

    public void removeProductFromGroup(String groupId, String productId) throws SQLException {
        requireNonBlank(groupId,   "Group id");
        requireNonBlank(productId, "Product id");
        groupService.removeMember(groupId, productId);
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException(fieldName + " must not be blank");
    }

    private static void requireNonNegative(double value, String fieldName) {
        if (value < 0)
            throw new IllegalArgumentException(fieldName + " must not be negative");
    }

    private Product requireExists(String productId) throws SQLException {
        return productService.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
    }
}