package com.example.onlineshop.model;

import java.time.LocalDate;

public record DailySales(LocalDate day, long orders, long revenueCents) {
}
