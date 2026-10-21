package com.example.onlineshop.model;

public record CategorySales(
        String category,
        long quantity,
        long revenueCents) {
}
