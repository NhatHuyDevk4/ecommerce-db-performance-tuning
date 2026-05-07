package com.example.ecommerce_performance_tuning.benchmark;

import com.example.ecommerce_performance_tuning.repository.OrderItemRepository;
import com.example.ecommerce_performance_tuning.repository.OrderRepository;
import com.example.ecommerce_performance_tuning.repository.UserRepository;
import com.example.ecommerce_performance_tuning.util.TimerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class InsertBenchmark {

    private static final Logger log = LoggerFactory.getLogger(InsertBenchmark.class);

    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;

    public InsertBenchmark(UserRepository userRepository,
                           OrderItemRepository orderItemRepository,
                           OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * Xóa đúng thứ tự để tránh vi phạm foreign key:
     * order_items → orders → users
     */
    private void cleanAll(JdbcTemplate jdbc) {
        orderItemRepository.deleteAll(jdbc);
        orderRepository.deleteAll(jdbc);
        userRepository.deleteAll(jdbc);
    }

    /**
     * Single Insert: chèn từng row một bằng vòng lặp JDBC.
     * synchronized để ngăn concurrent requests gây duplicate key.
     */
    public synchronized Map<String, Object> runSingle(boolean useOptimized, int rowCount) {
        JdbcTemplate jdbc = useOptimized
                ? userRepository.getOptimized()
                : userRepository.getBaseline();
        String dbName = useOptimized ? "PerformanceTest" : "NonePerformanceTest";
        long idBase = System.currentTimeMillis();
        log.info("[SingleInsert] DB={}, rowCount={}, idBase={}", dbName, rowCount, idBase);


        long singleMs = TimerUtil.measure(() -> {
            for (int i = 0; i < rowCount; i++) {
                userRepository.insertSingle(
                    jdbc,
                    idBase + i,
                    "User_" + i,
                    "user" + (idBase + i) + "@test.com",
                    "09" + String.format("%08d", i % 100_000_000),
                    "Address " + i
                );
            }
        });
        log.info("[SingleInsert] {}rows = {}ms", rowCount, singleMs);

        return Map.of(
            "database",       dbName,
            "rowCount",       rowCount,
            "singleInsertMs", singleMs,
            "note",           "Each row = 1 separate JDBC roundtrip. Total: " + rowCount + " transactions."
        );
    }

    /**
     * Batch Insert: gom toàn bộ rows vào 1 PreparedStatement.executeBatch().
     * synchronized để ngăn concurrent requests gây duplicate key.
     */
    public synchronized Map<String, Object> runBatch(boolean useOptimized, int rowCount) {
        JdbcTemplate jdbc = useOptimized
                ? userRepository.getOptimized()
                : userRepository.getBaseline();
        String dbName = useOptimized ? "PerformanceTest" : "NonePerformanceTest";
        long idBase = System.currentTimeMillis();
        log.info("[BatchInsert] DB={}, rowCount={}, idBase={}", dbName, rowCount, idBase);

        List<Object[]> rows = new ArrayList<>();
        for (int i = 0; i < rowCount; i++) {
            rows.add(new Object[]{
                idBase + i,
                "User_" + i,
                "user" + (idBase + i) + "@test.com",
                "09" + String.format("%08d", i % 100_000_000),
                "Address " + i,
                Timestamp.from(Instant.now())
            });
        }
        long batchMs = TimerUtil.measure(() -> userRepository.batchInsert(jdbc, rows));
        log.info("[BatchInsert] {}rows = {}ms", rowCount, batchMs);

        return Map.of(
            "database",      dbName,
            "rowCount",      rowCount,
            "batchInsertMs", batchMs,
            "note",          "All " + rowCount + " rows sent in 1 PreparedStatement.executeBatch() call."
        );
    }

    /**
     * Clear: TRUNCATE tất cả bảng test theo đúng thứ tự FK.
     */
    public synchronized Map<String, Object> clear(boolean useOptimized) {
        JdbcTemplate jdbc = useOptimized
                ? userRepository.getOptimized()
                : userRepository.getBaseline();
        String dbName = useOptimized ? "PerformanceTest" : "NonePerformanceTest";
        log.info("[Clear] Truncating all benchmark tables on {}", dbName);
        cleanAll(jdbc);
        return Map.of(
            "database", dbName,
            "status",   "CLEARED",
            "message",  "TRUNCATE: order_items → orders → users (CASCADE)"
        );
    }
}
