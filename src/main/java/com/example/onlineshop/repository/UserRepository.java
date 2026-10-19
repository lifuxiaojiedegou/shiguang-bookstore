package com.example.onlineshop.repository;

import com.example.onlineshop.model.ShopUser;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private static final RowMapper<ShopUser> USER_ROW_MAPPER = (rs, rowNum) -> new ShopUser(
            rs.getLong("id"),
            rs.getString("email"),
            rs.getString("name"),
            rs.getString("password_hash"),
            rs.getString("role")
    );

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<ShopUser> findByEmail(String email) {
        List<ShopUser> users = jdbc.query(
                "SELECT id, email, name, password_hash, role FROM shop_user WHERE email = ?",
                USER_ROW_MAPPER, email);
        return users.stream().findFirst();
    }

    public boolean existsByEmail(String email) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM shop_user WHERE email = ?", Integer.class, email);
        return count != null && count > 0;
    }

    public void insert(String email, String name, String passwordHash, String role) {
        jdbc.update("""
                INSERT INTO shop_user (email, name, password_hash, role)
                VALUES (?, ?, ?, ?)
                """, email, name, passwordHash, role);
    }

    public void lock(long userId) {
        jdbc.queryForObject("SELECT id FROM shop_user WHERE id = ? FOR UPDATE", Long.class, userId);
    }

    public Optional<ShopUser> findById(long id) {
        List<ShopUser> rows = jdbc.query(
                "SELECT id, email, name, password_hash, role FROM shop_user WHERE id = ?",
                USER_ROW_MAPPER, id);
        return rows.stream().findFirst();
    }
}
