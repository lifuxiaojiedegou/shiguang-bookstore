package com.example.onlineshop.model;

public record Product (
    long id,
    String name,
    String category,
    String description,
    int priceCents,
    int stock,
    boolean active) {
}

