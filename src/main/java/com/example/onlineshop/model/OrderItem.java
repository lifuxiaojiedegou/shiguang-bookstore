package com.example.onlineshop.model;

public record OrderItem(
        long productId,
        String name,
        int unitPriceCents,
        int quantity) {

    public int subtotalCents() {
        return unitPriceCents * quantity;
    }
}
