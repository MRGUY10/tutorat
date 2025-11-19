package com.iiil.tutoring.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * System health check and diagnostics controller
 */
@RestController
@RequestMapping("/api/v1/system")
@CrossOrigin(origins = "*")
@Tag(name = "System Health", description = "System health checks and diagnostics")
public class SystemController {

    @Autowired
    private DatabaseClient databaseClient;

    /**
     * Application health check
     */
    @GetMapping("/health")
    @Operation(
            summary = "Application health check",
            description = "Check the overall health status of the application"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Application is healthy",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Health Status",
                            value = """
                                    {
                                      "status": "UP",
                                      "timestamp": "2025-09-17T10:30:00",
                                      "application": "Tutoring Platform",
                                      "version": "1.0.0-SNAPSHOT"
                                    }
                                    """
                    )
            )
    )
    public Mono<Map<String, Object>> health() {
        return Mono.fromCallable(() -> {
            return Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now(),
                "application", "Tutoring Platform",
                "version", "1.0.0-SNAPSHOT"
            );
        });
    }

    /**
     * Database connectivity check
     */
    @GetMapping("/health/db")
    @Operation(
            summary = "Database health check", 
            description = "Verify database connectivity and response time"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Database is accessible",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Database Health",
                            value = """
                                    {
                                      "status": "UP",
                                      "database": "PostgreSQL",
                                      "connection_test": 1,
                                      "db_time": "2025-09-17T10:30:00"
                                    }
                                    """
                    )
            )
    )
    public Mono<Map<String, Object>> dbHealth() {
        return databaseClient.sql("SELECT 1 as connection_test, NOW() as db_time")
                .fetch()
                .first()
                .map(result -> Map.<String, Object>of(
                    "database", "UP",
                    "connection_test", result.get("connection_test"),
                    "database_time", result.get("db_time"),
                    "timestamp", LocalDateTime.now()
                ))
                .onErrorReturn(Map.of(
                    "database", "DOWN",
                    "error", "Connection failed",
                    "timestamp", LocalDateTime.now()
                ));
    }

    /**
     * Database schema validation
     */
    @GetMapping("/health/schema")
    public Mono<Map<String, Object>> schemaHealth() {
        return databaseClient.sql("""
                SELECT 
                    (SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'users') as users_table,
                    (SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'etudiants') as etudiants_table,
                    (SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'tuteurs') as tuteurs_table,
                    (SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'sessions') as sessions_table
                """)
                .fetch()
                .first()
                .map(result -> Map.<String, Object>of(
                    "schema", "VALIDATED",
                    "tables_found", result,
                    "timestamp", LocalDateTime.now()
                ))
                .onErrorReturn(Map.of(
                    "schema", "ERROR",
                    "error", "Schema validation failed",
                    "timestamp", LocalDateTime.now()
                ));
    }

    /**
     * Simple ping endpoint
     */
    @GetMapping("/ping")
    public Mono<String> ping() {
        return Mono.just("pong");
    }
}