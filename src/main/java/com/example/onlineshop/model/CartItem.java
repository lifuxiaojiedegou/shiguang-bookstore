package com.example.onlineshop.model;

public record CartItem(
        long productId,
        String name,
        String category,
        int priceCents,
        int stock,
        boolean active,
        int quantity) {

    public int subtotalCents() {
        return quantity * priceCents;
    }
}
