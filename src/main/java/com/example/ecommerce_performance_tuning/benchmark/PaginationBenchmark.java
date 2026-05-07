package com.example.ecommerce_performance_tuning.benchmark;

import com.example.ecommerce_performance_tuning.repository.OrderRepository;
import com.example.ecommerce_performance_tuning.util.TimerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class PaginationBenchmark {

    private static final Logger log = LoggerFactory.getLogger(PaginationBenchmark.class);

    private final OrderRepository orderRepository;

    public PaginationBenchmark(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * So sánh OFFSET-LIMIT (deep pagination) vs Keyset Pagination.
     *
     * @param pageSize  số rows mỗi trang
     * @param offset    OFFSET để test (nên dùng offset lớn như 1_000_000 để thấy rõ sự chậm)
     * @param lastId    ID cuối của trang trước cho Keyset (cùng vị trí tương đương offset)
     * @param useOptimized true = PerformanceTest (có index), false = NonePerformanceTest
     */
    public Map<String, Object> run(int pageSize, int offset, long lastId, boolean useOptimized) {
        JdbcTemplate jdbc = useOptimized
                ? orderRepository.getOptimized()
                : orderRepository.getBaseline();

        String dbName = useOptimized ? "PerformanceTest" : "NonePerformanceTest";

        // --- OFFSET Pagination ---
        long offsetMs = TimerUtil.measure(
            () -> orderRepository.findByOffsetPagination(jdbc, offset, pageSize)
        ).elapsedMs();
        log.info("[PaginationBenchmark] OFFSET={} LIMIT={} on {}: {}ms", offset, pageSize, dbName, offsetMs);

        // --- Keyset Pagination ---
        long keysetMs = TimerUtil.measure(
            () -> orderRepository.findByKeysetPagination(jdbc, lastId, pageSize)
        ).elapsedMs();
        log.info("[PaginationBenchmark] Keyset lastId={} LIMIT={} on {}: {}ms", lastId, pageSize, dbName, keysetMs);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("database", dbName);
        result.put("pageSize", pageSize);
        result.put("offsetUsed", offset);
        result.put("lastIdUsed", lastId);
        result.put("offsetPaginationMs", offsetMs);
        result.put("keysetPaginationMs", keysetMs);
        result.put("speedupMs", offsetMs - keysetMs);
        result.put("note", "Offset lớn (ví dụ 1,000,000) sẽ thấy chênh lệch rõ ràng nhất");
        return result;
    }
}
