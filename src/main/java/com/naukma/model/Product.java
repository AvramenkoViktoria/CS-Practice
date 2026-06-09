package com.naukma.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Product {
    private final String id;
    private String name;
    private double price;
    private int quantity;

    public void addStock(int qty) {
        this.quantity += qty;
    }

    public boolean deductStock(int qty) {
        if (this.quantity < qty) return false;
        this.quantity -= qty;
        return true;
    }

    @Override
    public String toString() {
        return "Product{id='" + id + "', name='" + name + "', price=" + price + ", qty=" + quantity + '}';
    }
}