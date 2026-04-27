package com.example.ecommerce_performance_tuning.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ecommercePerformanceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-commerce Performance Tuning API")
                        .description("API documentation for large-scale database performance tuning project")
                        .version("1.0.0"));
    }
}