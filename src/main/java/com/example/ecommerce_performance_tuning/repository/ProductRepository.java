package com.example.ecommerce_performance_tuning.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Repository
public class ProductRepository {

    private final JdbcTemplate baseline;
    private final JdbcTemplate optimized;

    public ProductRepository(
            @Qualifier("baselineJdbc") JdbcTemplate baseline,
            @Qualifier("optimizedJdbc") JdbcTemplate optimized) {
        this.baseline = baseline;
        this.optimized = optimized;
    }

    public void insertSingle(JdbcTemplate jdbc, long id, String name, String category,
                              BigDecimal price, int stockQuantity) {
        jdbc.update(
            "INSERT INTO products (id, name, category, price, stock_quantity, created_at) VALUES (?,?,?,?,?,?)",
            id, name, category, price, stockQuantity, Timestamp.from(Instant.now())
        );
    }

    public void batchInsert(JdbcTemplate jdbc, List<Object[]> rows) {
        jdbc.batchUpdate(
            "INSERT INTO products (id, name, category, price, stock_quantity, created_at) VALUES (?,?,?,?,?,?)",
            rows
        );
    }

    // Query: tìm sản phẩm theo category (demo Full Scan vs Index Scan)
    public List<Map<String, Object>> findByCategory(JdbcTemplate jdbc, String category) {
        return jdbc.queryForList("SELECT * FROM products WHERE category = ?", category);
    }

    public void deleteAll(JdbcTemplate jdbc) {
        jdbc.update("TRUNCATE TABLE products CASCADE");
    }

    public JdbcTemplate getBaseline() { return baseline; }
    public JdbcTemplate getOptimized() { return optimized; }
}
