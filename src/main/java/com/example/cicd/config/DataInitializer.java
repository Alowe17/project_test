package com.example.cicd.config;

import com.example.cicd.catalog.CatalogService;
import com.example.cicd.catalog.Product;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seed(CatalogService catalogService) {
        return args -> {
            if (catalogService.search(null, null, null, null).isEmpty()) {
                catalogService.save(new Product("Laptop Pro 15", "Electronics", new BigDecimal("1299.99"), 20));
                catalogService.save(new Product("Wireless Mouse", "Electronics", new BigDecimal("29.99"), 100));
                catalogService.save(new Product("USB-C Hub", "Electronics", new BigDecimal("49.99"), 50));
                catalogService.save(new Product("Running Shoes", "Sports", new BigDecimal("89.99"), 40));
                catalogService.save(new Product("Yoga Mat", "Sports", new BigDecimal("24.99"), 80));
            }
        };
    }
}