package com.example.onlineshop.model;

public record SalesSummary(
        long revenueCents,
        long paidOrders,
        long averageOrderCents,
        long lowStockCount) {
}
