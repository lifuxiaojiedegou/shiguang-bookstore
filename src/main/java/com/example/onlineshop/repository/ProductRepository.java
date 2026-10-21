package com.example.onlineshop.repository;

// AI-assisted: Claude Code, 2026-10; reviewed and modified by 方喆.

import com.example.onlineshop.model.Product;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {

    private static final RowMapper<Product> PRODUCT_ROW_MAPPER = (rs, rowNum) -> new Product(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("category"),
            rs.getString("description"),
            rs.getInt("price_cents"),
            rs.getInt("stock"),
            rs.getBoolean("active"));

    private final JdbcTemplate jdbc;

    public ProductRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Product> search(String keyword, String category) {
        String sql = """
                SELECT id, name, category, description, price_cents, stock, active
                FROM product
                WHERE active = 1
                  AND (? = '' OR name LIKE ? OR description LIKE ?)
                  AND (? = '' OR category = ?)
                ORDER BY id
                """;
        String pattern = "%" + keyword + "%";
        return jdbc.query(sql, PRODUCT_ROW_MAPPER,
                keyword, pattern, pattern, category, category);
    }

    public List<String> findCategories() {
        return jdbc.queryForList(
                "SELECT DISTINCT category FROM product WHERE active = 1 ORDER BY category",
                String.class);
    }

    public Optional<Product> findById(long id) {
        String sql = """
            SELECT id, name, category, description, price_cents, stock, active
            FROM product
            WHERE id = ?
            """;
        List<Product> rows = jdbc.query(sql, PRODUCT_ROW_MAPPER, id);
        return rows.stream().findFirst();
    }

    public boolean decreaseStock(long productId, int quantity) {
        return jdbc.update(
                "UPDATE product SET stock = stock - ? "
                        + "WHERE id = ? AND active = 1 AND stock >= ?",
                quantity, productId, quantity) == 1;
    }

    public void restoreStock(long productId, int quantity) {
        jdbc.update("UPDATE product SET stock = stock + ? WHERE id = ?",
                quantity, productId);
    }

    public List<Product> findAll() {
        return jdbc.query("""
            SELECT id, name, category, description, price_cents, stock, active
            FROM product
            ORDER BY id
            """, PRODUCT_ROW_MAPPER);
    }

    public void insert(String name, String category, String description,
                       int priceCents, int stock, boolean active) {
        jdbc.update("""
            INSERT INTO product (name, category, description, price_cents, stock, active)
            VALUES (?, ?, ?, ?, ?, ?)
            """, name, category, description, priceCents, stock, active);
    }

    public int update(long id, String name, String category, String description,
                      int priceCents, int stock, boolean active) {
        return jdbc.update("""
            UPDATE product
            SET name = ?, category = ?, description = ?, price_cents = ?, stock = ?, active = ?
            WHERE id = ?
            """, name, category, description, priceCents, stock, active, id);
    }

    public int setActive(long id, boolean active) {
        return jdbc.update("UPDATE product SET active = ? WHERE id = ?", active, id);
    }

    public boolean hasOrderItems(long productId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM order_item WHERE product_id = ?",
                Integer.class, productId);
        return count != null && count > 0;
    }

    public void deleteCartItems(long productId) {
        jdbc.update("DELETE FROM cart_item WHERE product_id = ?", productId);
    }

    public int delete(long productId) {
        return jdbc.update("DELETE FROM product WHERE id = ?", productId);
    }
}