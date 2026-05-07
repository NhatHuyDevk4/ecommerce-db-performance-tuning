package com.example.ecommerce_performance_tuning.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    // --- NonePerformanceTest (baseline, primary datasource) ---
    @Value("${spring.datasource.url}")
    private String baselineUrl;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    // --- PerformanceTest (optimized datasource) ---
    @Value("${app.datasource.optimized.url}")
    private String optimizedUrl;

    @Primary
    @Bean(name = "baselineDataSource")
    public DataSource baselineDataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName(driverClassName);
        ds.setUrl(baselineUrl);
        ds.setUsername(username);
        ds.setPassword(password);
        return ds;
    }

    @Bean(name = "optimizedDataSource")
    public DataSource optimizedDataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName(driverClassName);
        ds.setUrl(optimizedUrl);
        ds.setUsername(username);
        ds.setPassword(password);
        return ds;
    }

    @Primary
    @Bean(name = "baselineJdbc")
    public JdbcTemplate baselineJdbc(DataSource baselineDataSource) {
        return new JdbcTemplate(baselineDataSource);
    }

    @Bean(name = "optimizedJdbc")
    public JdbcTemplate optimizedJdbc(DataSource optimizedDataSource) {
        return new JdbcTemplate(optimizedDataSource);
    }
}
