package com.example.ecommerce_performance_tuning.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class OrderItemRepository {

    private final JdbcTemplate baseline;
    private final JdbcTemplate optimized;

    public OrderItemRepository(
            @Qualifier("baselineJdbc") JdbcTemplate baseline,
            @Qualifier("optimizedJdbc") JdbcTemplate optimized) {
        this.baseline = baseline;
        this.optimized = optimized;
    }

    public void batchInsert(JdbcTemplate jdbc, List<Object[]> rows) {
        jdbc.batchUpdate(
            "INSERT INTO order_items (id, order_id, product_id, quantity, price, total_price) VALUES (?,?,?,?,?,?)",
            rows
        );
    }

    // Query tìm items theo order_id (demo covering index)
    public List<Map<String, Object>> findByOrderId(JdbcTemplate jdbc, long orderId) {
        return jdbc.queryForList("SELECT * FROM order_items WHERE order_id = ?", orderId);
    }

    public void deleteAll(JdbcTemplate jdbc) {
        jdbc.update("TRUNCATE TABLE order_items CASCADE");
    }

    public JdbcTemplate getBaseline() { return baseline; }
    public JdbcTemplate getOptimized() { return optimized; }
}
