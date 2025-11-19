package com.iiil.tutoring.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

/**
 * Configuration for testing database connectivity
 */
@Configuration
public class DatabaseTestConfig {

    @Autowired
    private DatabaseClient databaseClient;

    /**
     * Test database connection on startup
     */
    @Bean
    public ApplicationRunner testDatabaseConnection() {
        return args -> {
            System.out.println("Testing database connection...");
            
            databaseClient.sql("SELECT 1 as test")
                .fetch()
                .first()
                .doOnSuccess(result -> {
                    System.out.println("✅ Database connection successful: " + result);
                })
                .doOnError(error -> {
                    System.err.println("❌ Database connection failed: " + error.getMessage());
                })
                .onErrorResume(error -> {
                    System.err.println("Database connection error details: " + error.getClass().getSimpleName());
                    return Mono.empty();
                })
                .subscribe();
        };
    }
}