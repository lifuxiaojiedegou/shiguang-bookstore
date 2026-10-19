package com.example.onlineshop.model;

public record ShopUser (
        long id,
        String email,
        String name,
        String passwordHash,
        String role) {
}



