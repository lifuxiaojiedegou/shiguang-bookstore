package com.example.onlineshop.repository;

import com.example.onlineshop.model.MailMessage;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class MailRepository {
    private static final RowMapper<MailMessage> MAIL_ROW_MAPPER = (rs, rowNum) -> new MailMessage(
            rs.getLong("id"),
            rs.getLong("order_id"),
            rs.getString("recipient"),
            rs.getString("subject"),
            rs.getString("body"),
            rs.getString("status"),
            rs.getString("error"));

    private final JdbcTemplate jdbc;

    public MailRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long insert(long orderId, String recipient, String subject, String body) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO mail_outbox (order_id, recipient, subject, body)
                    VALUES (?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, orderId);
            statement.setString(2, recipient);
            statement.setString(3, subject);
            statement.setString(4, body);
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public Optional<MailMessage> findById(long id) {
        List<MailMessage> rows = jdbc.query(
                "SELECT id, order_id, recipient, subject, body, status, error "
                        + "FROM mail_outbox WHERE id = ?",
                MAIL_ROW_MAPPER, id);
        return rows.stream().findFirst();
    }

    public Optional<MailMessage> findLatestByOrder(long orderId) {
        List<MailMessage> rows = jdbc.query(
                "SELECT id, order_id, recipient, subject, body, status, error "
                        + "FROM mail_outbox WHERE order_id = ? ORDER BY id DESC LIMIT 1",
                MAIL_ROW_MAPPER, orderId);
        return rows.stream().findFirst();
    }

    public boolean claim(long id) {
        return jdbc.update("""
                UPDATE mail_outbox SET status = 'SENDING', error = NULL
                WHERE id = ? AND status IN ('PENDING', 'FAILED', 'SAVED')
                """, id) == 1;
    }

    public void markSent(long id) {
        jdbc.update("UPDATE mail_outbox SET status = 'SENT', sent_at = CURRENT_TIMESTAMP WHERE id = ?", id);
    }

    public void markSaved(long id) {
        jdbc.update("UPDATE mail_outbox SET status = 'SAVED', sent_at = CURRENT_TIMESTAMP WHERE id = ?", id);
    }

    public void markFailed(long id, String error) {
        jdbc.update("UPDATE mail_outbox SET status = 'FAILED', error = ? WHERE id = ?", error, id);
    }
}
