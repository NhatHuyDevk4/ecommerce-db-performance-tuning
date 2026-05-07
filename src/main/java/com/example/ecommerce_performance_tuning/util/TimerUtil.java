package com.example.ecommerce_performance_tuning.util;

import java.util.function.Supplier;

public class TimerUtil {

    /**
     * Đo thời gian thực thi của một block code, trả về kết quả kèm thời gian (ms).
     */
    public static <T> TimedResult<T> measure(Supplier<T> action) {
        long start = System.currentTimeMillis();
        T result = action.get();
        long elapsed = System.currentTimeMillis() - start;
        return new TimedResult<>(result, elapsed);
    }

    /**
     * Đo thời gian thực thi không có giá trị trả về.
     */
    public static long measure(Runnable action) {
        long start = System.currentTimeMillis();
        action.run();
        return System.currentTimeMillis() - start;
    }

    public record TimedResult<T>(T result, long elapsedMs) {}
}
