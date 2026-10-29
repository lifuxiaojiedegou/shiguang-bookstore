package com.example.onlineshop.repository;

// AI-assisted: Claude Code, 2026-10; reviewed and modified by 方喆.

import com.example.onlineshop.model.ActivityLog;
import com.example.onlineshop.model.KeywordCount;
import com.example.onlineshop.model.LogAction;
import com.example.onlineshop.model.ProductFunnel;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class LogRepository {

    private static final RowMapper<ActivityLog> LOG_ROW_MAPPER = (rs, rowNum) -> new ActivityLog(
            rs.getLong("id"),
            rs.getObject("user_id", Long.class),
            rs.getString("email"),
            rs.getObject("product_id", Long.class),
            rs.getString("product_name"),
            LogAction.valueOf(rs.getString("action")),
            rs.getString("detail"),
            rs.getString("ip"),
            rs.getTimestamp("created_at").toLocalDateTime());

    private final JdbcTemplate jdbc;

    public LogRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(Long userId, Long productId, String productName,
                       String action, String detail, String ip) {
        jdbc.update("""
                INSERT INTO activity_log (user_id, product_id, product_name, action, detail, ip)
                VALUES (?, ?, ?, ?, ?, ?)
                """, userId, productId, productName, action, detail, ip);
    }

    public List<ActivityLog> findRecent(String action, int limit) {
        return jdbc.query("""
                SELECT l.id, l.user_id, u.email, l.product_id, l.product_name,
                       l.action, l.detail, l.ip, l.created_at
                FROM activity_log l
                LEFT JOIN shop_user u ON u.id = l.user_id
                WHERE (? = '' OR l.action = ?)
                ORDER BY l.id DESC
                LIMIT ?
                """, LOG_ROW_MAPPER, action, action, limit);
    }

    public Map<LogAction, Long> countToday() {
        Map<LogAction, Long> counts = new EnumMap<>(LogAction.class);
        for (LogAction action : LogAction.values()) {
            counts.put(action, 0L);
        }
        jdbc.query("""
                SELECT action, COUNT(*) AS total
                FROM activity_log
                WHERE created_at >= CURDATE()
                GROUP BY action
                """, rs -> {
            counts.put(LogAction.valueOf(rs.getString("action")), rs.getLong("total"));
        });
        return counts;
    }

    public List<ProductFunnel> productFunnel(int limit) {
        return jdbc.query("""
                SELECT product_id,
                       MAX(product_name) AS name,
                       SUM(action = 'VIEW') AS views,
                       SUM(action = 'ADD_CART') AS carts,
                       SUM(action = 'ORDER') AS orders
                FROM activity_log
                WHERE product_id IS NOT NULL
                GROUP BY product_id
                ORDER BY views DESC, orders DESC
                LIMIT ?
                """, (rs, rowNum) -> new ProductFunnel(
                rs.getLong("product_id"),
                rs.getString("name"),
                rs.getLong("views"),
                rs.getLong("carts"),
                rs.getLong("orders")), limit);
    }

    public List<KeywordCount> topKeywords(int limit) {
        return jdbc.query("""
                SELECT detail, COUNT(*) AS total
                FROM activity_log
                WHERE action = 'SEARCH'
                GROUP BY detail
                ORDER BY total DESC
                LIMIT ?
                """, (rs, rowNum) -> new KeywordCount(
                rs.getString("detail"),
                rs.getLong("total")), limit);
    }
}
