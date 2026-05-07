package com.example.ecommerce_performance_tuning.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
public class UserRepository {

    private final JdbcTemplate baseline;
    private final JdbcTemplate optimized;

    public UserRepository(
            @Qualifier("baselineJdbc") JdbcTemplate baseline,
            @Qualifier("optimizedJdbc") JdbcTemplate optimized) {
        this.baseline = baseline;
        this.optimized = optimized;
    }

    // ---- Single insert (từng dòng một) ----
    public void insertSingle(JdbcTemplate jdbc, long id, String fullName, String email, String phone, String address) {
        jdbc.update(
            "INSERT INTO users (id, full_name, email, phone, address, created_at) VALUES (?,?,?,?,?,?)",
            id, fullName, email, phone, address, Timestamp.from(Instant.now())
        );
    }

    // ---- Batch insert (dùng batchUpdate) ----
    public void batchInsert(JdbcTemplate jdbc, List<Object[]> rows) {
        jdbc.batchUpdate(
            "INSERT INTO users (id, full_name, email, phone, address, created_at) VALUES (?,?,?,?,?,?)",
            rows
        );
    }

    public void deleteAll(JdbcTemplate jdbc) {
        jdbc.update("TRUNCATE TABLE users CASCADE");
    }

    public JdbcTemplate getBaseline() { return baseline; }
    public JdbcTemplate getOptimized() { return optimized; }
}
