package com.naukma.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class ProductGroup {
    private final String id;
    private String name;
    private final List<String> productIds = new ArrayList<>();

    public ProductGroup(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public List<String> getProductIds() { return Collections.unmodifiableList(productIds); }

    public void addProduct(String productId) {
        if (!productIds.contains(productId)) productIds.add(productId);
    }
}