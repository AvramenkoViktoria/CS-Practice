package com.naukma.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductGroup {
    private String id;
    private String name;
    private Set<String> productIds = new HashSet<>();

    public ProductGroup(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addProduct(String productId) {
        productIds.add(productId);
    }
}