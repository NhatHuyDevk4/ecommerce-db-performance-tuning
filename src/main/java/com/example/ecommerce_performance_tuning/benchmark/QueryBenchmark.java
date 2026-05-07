package com.example.ecommerce_performance_tuning.benchmark;

import com.example.ecommerce_performance_tuning.repository.OrderRepository;
import com.example.ecommerce_performance_tuning.repository.ProductRepository;
import com.example.ecommerce_performance_tuning.util.TimerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class QueryBenchmark {

    private static final Logger log = LoggerFactory.getLogger(QueryBenchmark.class);

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public QueryBenchmark(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * So sánh:
     * 1. Query tìm products theo category (Full Scan vs Index Scan)
     * 2. Aggregation tổng doanh thu theo status
     * @param category category cần tìm (ví dụ: "Electronics")
     */
    public Map<String, Object> run(String category) {
        Map<String, Object> result = new LinkedHashMap<>();

        // --- Test 1: findByCategory ---
        // DB không có index (NonePerformanceTest)
        JdbcTemplate baselineJdbc = productRepository.getBaseline();
        TimerUtil.TimedResult<List<Map<String, Object>>> baselineQuery =
            TimerUtil.measure(() -> productRepository.findByCategory(baselineJdbc, category));

        // DB có index (PerformanceTest)
        JdbcTemplate optimizedJdbc = productRepository.getOptimized();
        TimerUtil.TimedResult<List<Map<String, Object>>> optimizedQuery =
            TimerUtil.measure(() -> productRepository.findByCategory(optimizedJdbc, category));

        log.info("[QueryBenchmark] findByCategory='{}': baseline={}ms, optimized={}ms",
            category, baselineQuery.elapsedMs(), optimizedQuery.elapsedMs());

        result.put("test", "findByCategory");
        result.put("category", category);
        result.put("rowsFound", baselineQuery.result().size());
        result.put("noIndexMs", baselineQuery.elapsedMs());
        result.put("withIndexMs", optimizedQuery.elapsedMs());
        result.put("speedupMs", baselineQuery.elapsedMs() - optimizedQuery.elapsedMs());

        // --- Test 2: Aggregation ---
        long baselineAggMs = TimerUtil.measure(() -> orderRepository.aggregateByStatus(baselineJdbc)).elapsedMs();
        long optimizedAggMs = TimerUtil.measure(() -> orderRepository.aggregateByStatus(optimizedJdbc)).elapsedMs();

        log.info("[QueryBenchmark] aggregateByStatus: baseline={}ms, optimized={}ms",
            baselineAggMs, optimizedAggMs);

        result.put("aggregation_noIndexMs", baselineAggMs);
        result.put("aggregation_withIndexMs", optimizedAggMs);
        result.put("aggregation_speedupMs", baselineAggMs - optimizedAggMs);

        return result;
    }
}
