package com.example.onlineshop.repository;

// AI-assisted: Claude Code, 2026-10; reviewed and modified by 方喆.

import com.example.onlineshop.model.Order;
import com.example.onlineshop.model.OrderItem;
import com.example.onlineshop.model.OrderStatus;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class OrderRepository {

    private static final RowMapper<Order> ORDER_ROW_MAPPER = (rs, rowNum) -> {
        Timestamp paid = rs.getTimestamp("paid_at");
        return new Order(
                rs.getLong("id"),
                rs.getLong("user_id"),
                OrderStatus.valueOf(rs.getString("status")),
                rs.getLong("total_cents"),
                rs.getString("address"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                paid == null ? null : paid.toLocalDateTime(),
                rs.getString("tracking_no"),
                List.of());
    };

    private static final RowMapper<OrderItem> ORDER_ITEM_ROW_MAPPER = (rs, rowNum) -> new OrderItem(
            rs.getLong("product_id"),
            rs.getString("name"),
            rs.getInt("unit_price_cents"),
            rs.getInt("quantity"));

    private final JdbcTemplate jdbc;

    public OrderRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long insertOrder(long userId, String address, long totalCents) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO orders (user_id, status, total_cents, address)
                    VALUES (?, 'UNPAID', ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, userId);
            statement.setLong(2, totalCents);
            statement.setString(3, address);
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void insertItem(long orderId, long productId, String name, int unitPriceCents, int quantity) {
        jdbc.update("""
                INSERT INTO order_item (order_id, product_id, name, unit_price_cents, quantity)
                VALUES (?, ?, ?, ?, ?)
                """, orderId, productId, name, unitPriceCents, quantity);
    }

    public List<Order> findByUser(long userId) {
        List<Order> rows = jdbc.query("""
                SELECT id, user_id, status, total_cents, address, created_at, paid_at, tracking_no
                FROM orders
                WHERE user_id = ?
                ORDER BY id DESC
                """, ORDER_ROW_MAPPER, userId);
        return rows.stream().map(this::withItems).toList();
    }

    private Order withItems(Order order) {
        return new Order(order.id(), order.userId(), order.status(),
                order.totalCents(), order.address(), order.createdAt(),
                order.paidAt(), order.trackingNo(), findItems(order.id()));
    }

    public int markPaid(long orderId, long userId) {
        return jdbc.update("""
            UPDATE orders SET status = 'PAID', paid_at = CURRENT_TIMESTAMP
            WHERE id = ? AND user_id = ? AND status = 'UNPAID'
            """, orderId, userId);
    }

    public int markCancelled(long orderId, long userId) {
        return jdbc.update("""
            UPDATE orders SET status = 'CANCELLED'
            WHERE id = ? AND user_id = ? AND status = 'UNPAID'
            """, orderId, userId);
    }

    public int markCompleted(long orderId, long userId) {
        return jdbc.update("""
            UPDATE orders SET status = 'COMPLETED'
            WHERE id = ? AND user_id = ? AND status = 'SHIPPED'
            """, orderId, userId);
    }

    public List<OrderItem> findItems(long orderId) {
        return jdbc.query(
                "SELECT product_id, name, unit_price_cents, quantity "
                        + "FROM order_item WHERE order_id = ? ORDER BY id",
                ORDER_ITEM_ROW_MAPPER, orderId);
    }

    public List<Order> findAll() {
        List<Order> rows = jdbc.query("""
            SELECT id, user_id, status, total_cents, address, created_at, paid_at, tracking_no
            FROM orders
            ORDER BY id DESC
            """, ORDER_ROW_MAPPER);
        return rows.stream().map(this::withItems).toList();
    }

    public int markShipped(long orderId, String trackingNo) {
        return jdbc.update("""
            UPDATE orders SET status = 'SHIPPED', tracking_no = ?
            WHERE id = ? AND status = 'PAID'
            """, trackingNo, orderId);
    }

    public Optional<Order> findById(long orderId) {
        List<Order> rows = jdbc.query("""
            SELECT id, user_id, status, total_cents, address, created_at, paid_at, tracking_no
            FROM orders
            WHERE id = ?
            """, ORDER_ROW_MAPPER, orderId);
        return rows.stream().findFirst().map(this::withItems);
    }
}
