package com.iiil.tutoring.config;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Additional SpringDoc configuration for WebFlux
 */
@Configuration
public class SpringDocConfig {

    @Bean
    public OpenApiCustomizer springDocCustomizer() {
        return openApi -> {
            // Ensure paths are properly set for WebFlux
            openApi.getPaths().forEach((path, pathItem) -> {
                // Additional customization if needed
            });
        };
    }
}