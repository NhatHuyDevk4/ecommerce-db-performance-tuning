package com.example.ecommerce_performance_tuning.service;

import com.example.ecommerce_performance_tuning.benchmark.InsertBenchmark;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class BenchmarkService {

    private final InsertBenchmark insertBenchmark;

    public BenchmarkService(InsertBenchmark insertBenchmark) {
        this.insertBenchmark = insertBenchmark;
    }

    public Map<String, Object> runSingleInsert(boolean useOptimized, int rowCount) {
        return insertBenchmark.runSingle(useOptimized, rowCount);
    }

    public Map<String, Object> runBatchInsert(boolean useOptimized, int rowCount) {
        return insertBenchmark.runBatch(useOptimized, rowCount);
    }

    public Map<String, Object> clearData(boolean useOptimized) {
        return insertBenchmark.clear(useOptimized);
    }
}
