package com.example.onlineshop.repository;

import com.example.onlineshop.model.CartItem;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class CartRepository {

    private static final RowMapper<CartItem> CART_ITEM_ROW_MAPPER = (rs, rowNum)
    -> new CartItem(
            rs.getLong("product_id"),
            rs.getString("name"),
            rs.getString("category"),
            rs.getInt("price_cents"),
            rs.getInt("stock"),
            rs.getBoolean("active"),
            rs.getInt("quantity"));

    private final JdbcTemplate jdbc;

    public CartRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<CartItem> findByUser(long userId) {
        return jdbc.query("""
                SELECT p.id AS product_id, p.name, p.category, p.price_cents,
                                       p.stock, p.active, c.quantity
                                FROM cart_item c
                                JOIN product p ON c.product_id = p.id
                                WHERE c.user_id = ?
                                ORDER BY c.product_id
                                """, CART_ITEM_ROW_MAPPER, userId);
    }

    public Optional<Integer> findQuantity(long userId, long productId) {
        List<Integer> rows = jdbc.queryForList(
                "SELECT quantity FROM cart_item WHERE user_id = ? AND product_id " +
                        "= ?", Integer.class, userId, productId);
        return rows.stream().findFirst();
    }

    public void save(long userId, long productId, int quantity) {
        int updated = jdbc.update(
                "UPDATE cart_item SET quantity = ? WHERE user_id = ? AND product_id = ?",
                quantity, userId, productId);
        if (updated == 0) {
            jdbc.update(
                    "INSERT INTO cart_item (user_id, product_id, quantity) VALUES (?, ?, ?)",
                    userId, productId, quantity);
        }
    }

    public void delete(long userId, long productId) {
        jdbc.update("DELETE FROM cart_item WHERE user_id = ? AND product_id = ?",
                userId, productId);
    }

    public void deleteByUser(long userId) {
        jdbc.update("DELETE FROM cart_item WHERE user_id = ?", userId);
    }
}

