package com.example.onlineshop.repository;

// AI-assisted: Claude Code, 2026-10; reviewed and modified by 方喆.

import com.example.onlineshop.model.CategorySales;
import com.example.onlineshop.model.DailySales;
import com.example.onlineshop.model.ProductSales;
import com.example.onlineshop.model.SalesSummary;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class StatsRepository {

    private final JdbcTemplate jdbc;

    public StatsRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public SalesSummary summary() {
        Map<String, Object> row = jdbc.queryForMap("""
                SELECT COUNT(*) AS paid_orders,
                       COALESCE(SUM(total_cents), 0) AS revenue_cents
                FROM orders
                WHERE status IN ('PAID', 'SHIPPED', 'COMPLETED')
                """);
        long orders = ((Number) row.get("paid_orders")).longValue();
        long revenue = ((Number) row.get("revenue_cents")).longValue();
        long average = orders == 0 ? 0 : revenue / orders;
        return new SalesSummary(revenue, orders, average, lowStockCount());
    }

    private long lowStockCount() {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM product WHERE active = 1 AND stock < 10",
                Long.class);
        return count == null ? 0 : count;
    }

    public List<DailySales> daily(LocalDate start) {
        return jdbc.query("""
                SELECT DATE(paid_at) AS day,
                       COUNT(*) AS orders,
                       SUM(total_cents) AS revenue_cents
                FROM orders
                WHERE status IN ('PAID', 'SHIPPED', 'COMPLETED')
                  AND paid_at >= ?
                GROUP BY DATE(paid_at)
                ORDER BY day
                """, (rs, rowNum) -> new DailySales(
                rs.getDate("day").toLocalDate(),
                rs.getLong("orders"),
                rs.getLong("revenue_cents")), java.sql.Date.valueOf(start));
    }

    public List<ProductSales> topProducts(int limit) {
        return jdbc.query("""
                SELECT i.name,
                       SUM(i.quantity) AS quantity,
                       SUM(i.unit_price_cents * i.quantity) AS revenue_cents
                FROM order_item i
                JOIN orders o ON o.id = i.order_id
                WHERE o.status IN ('PAID', 'SHIPPED', 'COMPLETED')
                GROUP BY i.product_id, i.name
                ORDER BY quantity DESC, revenue_cents DESC
                LIMIT ?
                """, (rs, rowNum) -> new ProductSales(
                rs.getString("name"),
                rs.getLong("quantity"),
                rs.getLong("revenue_cents")), limit);
    }

    public List<CategorySales> categories() {
        return jdbc.query("""
                SELECT p.category,
                       SUM(i.quantity) AS quantity,
                       SUM(i.unit_price_cents * i.quantity) AS revenue_cents
                FROM order_item i
                JOIN orders o ON o.id = i.order_id
                JOIN product p ON p.id = i.product_id
                WHERE o.status IN ('PAID', 'SHIPPED', 'COMPLETED')
                GROUP BY p.category
                ORDER BY revenue_cents DESC
                """, (rs, rowNum) -> new CategorySales(
                rs.getString("category"),
                rs.getLong("quantity"),
                rs.getLong("revenue_cents")));
    }
}