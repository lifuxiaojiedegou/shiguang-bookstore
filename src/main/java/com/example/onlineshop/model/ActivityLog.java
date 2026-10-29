package com.example.onlineshop.model;

import java.time.LocalDateTime;

public record ActivityLog(
        long id,
        Long userId,
        String userEmail,
        Long productId,
        String productName,
        LogAction action,
        String detail,
        String ip,
        LocalDateTime createdAt) {
}
