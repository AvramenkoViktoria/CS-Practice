package com.naukma.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Product {
    private String id;
    private String name;
    private double price;
    private int quantity;

    public void addStock(int amount) {
        this.quantity += amount;
    }

    public boolean deductStock(int amount) {
        if (quantity >= amount) {
            quantity -= amount;
            return true;
        }
        return false;
    }
}