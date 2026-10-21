package com.example.onlineshop.model;

import java.util.List;

public record StatsReport(
        SalesSummary summary,
        List<DailySales> daily,
        List<ProductSales> topProducts,
        List<CategorySales> categories) {
}
