package com.example.ecommerce_performance_tuning.controller;

import com.example.ecommerce_performance_tuning.dto.ApiResponse;
import com.example.ecommerce_performance_tuning.service.BenchmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/benchmark")
@Tag(name = "Benchmark", description = "JDBC Batch Processing vs Single Insert | useOptimized=false→NonePerformanceTest, true→PerformanceTest")
public class BenchmarkController {

    private final BenchmarkService benchmarkService;

    public BenchmarkController(BenchmarkService benchmarkService) {
        this.benchmarkService = benchmarkService;
    }

    @Operation(summary = "Single Insert – chèn từng row một (chậm, mỗi row = 1 roundtrip)",
               description = "Truncate → insert rowCount rows từng cái một bằng vòng lặp JDBC")
    @PostMapping("/single-insert")
    public ResponseEntity<ApiResponse<Map<String, Object>>> singleInsert(
            @RequestParam(defaultValue = "false") boolean useOptimized,
            @RequestParam(defaultValue = "1000") int rowCount) {

        Map<String, Object> result = benchmarkService.runSingleInsert(useOptimized, rowCount);
        return ResponseEntity.ok(ApiResponse.ok("Single insert completed", result));
    }

    @Operation(summary = "Batch Insert – gom toàn bộ rows vào 1 batch (nhanh)",
               description = "Truncate → insert rowCount rows bằng PreparedStatement.executeBatch() duy nhất")
    @PostMapping("/batch-insert")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchInsert(
            @RequestParam(defaultValue = "false") boolean useOptimized,
            @RequestParam(defaultValue = "1000") int rowCount) {

        Map<String, Object> result = benchmarkService.runBatchInsert(useOptimized, rowCount);
        return ResponseEntity.ok(ApiResponse.ok("Batch insert completed", result));
    }

    @Operation(summary = "Clear – TRUNCATE tất cả bảng test (dùng sau khi test xong)",
               description = "Truncate order_items → orders → users CASCADE trên database được chọn")
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Map<String, Object>>> clearData(
            @RequestParam(defaultValue = "false") boolean useOptimized) {

        Map<String, Object> result = benchmarkService.clearData(useOptimized);
        return ResponseEntity.ok(ApiResponse.ok("Data cleared", result));
    }
}
