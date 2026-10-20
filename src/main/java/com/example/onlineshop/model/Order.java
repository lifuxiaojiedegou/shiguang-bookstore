package com.example.onlineshop.model;

import java.time.LocalDateTime;
import java.util.List;

public record Order(
        long id,
        long userId,
        OrderStatus status,
        long totalCents,
        String address,
        LocalDateTime createdAt,
        LocalDateTime paidAt,
        String trackingNo,
        List<OrderItem> items) {
}
