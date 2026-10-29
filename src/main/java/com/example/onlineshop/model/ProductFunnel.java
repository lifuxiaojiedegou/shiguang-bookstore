package com.example.onlineshop.model;

public record ProductFunnel(
        long productId,
        String name,
        long views,
        long carts,
        long orders) {
}
