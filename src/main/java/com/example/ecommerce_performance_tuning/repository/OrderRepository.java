package com.example.ecommerce_performance_tuning.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class OrderRepository {

    private final JdbcTemplate baseline;
    private final JdbcTemplate optimized;

    public OrderRepository(
            @Qualifier("baselineJdbc") JdbcTemplate baseline,
            @Qualifier("optimizedJdbc") JdbcTemplate optimized) {
        this.baseline = baseline;
        this.optimized = optimized;
    }

    public void batchInsert(JdbcTemplate jdbc, List<Object[]> rows) {
        jdbc.batchUpdate(
            "INSERT INTO orders (id, user_id, status, total_amount, created_at, updated_at) VALUES (?,?,?,?,?,?)",
            rows
        );
    }

    // Deep pagination: OFFSET-LIMIT (chậm với offset lớn)
    public List<Map<String, Object>> findByOffsetPagination(JdbcTemplate jdbc, int offset, int limit) {
        return jdbc.queryForList(
            "SELECT * FROM orders ORDER BY id LIMIT ? OFFSET ?",
            limit, offset
        );
    }

    // Keyset pagination: dùng WHERE id > lastId (nhanh hơn)
    public List<Map<String, Object>> findByKeysetPagination(JdbcTemplate jdbc, long lastId, int limit) {
        return jdbc.queryForList(
            "SELECT * FROM orders WHERE id > ? ORDER BY id LIMIT ?",
            lastId, limit
        );
    }

    // Query: tìm orders theo user_id (demo index trên user_id)
    public List<Map<String, Object>> findByUserId(JdbcTemplate jdbc, long userId) {
        return jdbc.queryForList("SELECT * FROM orders WHERE user_id = ?", userId);
    }

    // Query: aggregation - tổng doanh thu theo trạng thái
    public List<Map<String, Object>> aggregateByStatus(JdbcTemplate jdbc) {
        return jdbc.queryForList(
            "SELECT status, COUNT(*) as order_count, SUM(total_amount) as total_revenue " +
            "FROM orders GROUP BY status"
        );
    }

    public void deleteAll(JdbcTemplate jdbc) {
        jdbc.update("TRUNCATE TABLE orders CASCADE");
    }

    public JdbcTemplate getBaseline() { return baseline; }
    public JdbcTemplate getOptimized() { return optimized; }
}
