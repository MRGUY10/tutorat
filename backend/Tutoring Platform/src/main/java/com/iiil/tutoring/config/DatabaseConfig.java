package com.iiil.tutoring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import java.util.List;

/**
 * R2DBC Database Configuration
 */
@Configuration
@EnableR2dbcRepositories(basePackages = "com.iiil.tutoring.repository")
@EnableR2dbcAuditing
public class DatabaseConfig extends AbstractR2dbcConfiguration {

    @Override
    public @org.springframework.lang.NonNull io.r2dbc.spi.ConnectionFactory connectionFactory() {
        // This will be handled by Spring Boot auto-configuration
        throw new UnsupportedOperationException("ConnectionFactory is configured by Spring Boot");
    }

    /**
     * Custom R2DBC converters for enums
     */
    @Override
    protected @org.springframework.lang.NonNull List<Object> getCustomConverters() {
        return R2dbcEnumConverters.getConverters();
    }
}