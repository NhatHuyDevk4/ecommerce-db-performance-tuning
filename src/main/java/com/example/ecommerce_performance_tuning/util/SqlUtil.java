package com.example.ecommerce_performance_tuning.util;

public class SqlUtil {

    /**
     * Tạo chuỗi placeholders cho batch insert: (?,?,?),(?,?,?),...
     */
    public static String buildBatchPlaceholders(int rows, int columns) {
        String singleRow = "(" + "?,".repeat(columns - 1) + "?" + ")";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            sb.append(singleRow);
            if (i < rows - 1) sb.append(",");
        }
        return sb.toString();
    }

    /**
     * Tạo IN clause: (?,?,?,...)
     */
    public static String buildInClause(int count) {
        return "(" + "?,".repeat(count - 1) + "?" + ")";
    }
}
