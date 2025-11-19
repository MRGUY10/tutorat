package com.iiil.tutoring.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger API groups configuration for better organization
 */
@Configuration
public class SwaggerGroupConfig {

    @Bean
    public GroupedOpenApi authenticationApi() {
        return GroupedOpenApi.builder()
                .group("01-authentication")
                .displayName("Authentication & Registration")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userManagementApi() {
        return GroupedOpenApi.builder()
                .group("02-user-management")
                .displayName("User Management")
                .pathsToMatch("/api/v1/users/**", "/api/user/**")
                .build();
    }

    @Bean
    public GroupedOpenApi sessionManagementApi() {
        return GroupedOpenApi.builder()
                .group("03-session-management")
                .displayName("Session Management")
                .pathsToMatch("/api/v1/sessions/**", "/api/sessions/**")
                .build();
    }

    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("99-system")
                .displayName("System & Health")
                .pathsToMatch("/api/system/**", "/actuator/**")
                .build();
    }

    @Bean
    public GroupedOpenApi allApis() {
        return GroupedOpenApi.builder()
                .group("00-all-apis")
                .displayName("All APIs")
                .pathsToMatch("/api/**")
                .build();
    }
}