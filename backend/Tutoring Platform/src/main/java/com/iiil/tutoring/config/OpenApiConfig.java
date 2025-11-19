package com.iiil.tutoring.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for the Tutoring Platform API
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tutoring Platform API")
                        .version("1.0.0")
                        .description("""
                                # Tutoring Platform API Documentation
                                
                                This API provides comprehensive functionality for a tutoring platform including:
                                
                                ## Core Features
                                - **User Management**: Registration, authentication, and profile management for students, tutors, and administrators
                                - **Session Management**: Session creation, scheduling, and lifecycle management
                                - **Subject & Competence Management**: Managing subjects and tutor competencies
                                - **Communication**: Real-time messaging between students and tutors
                                - **Payment Processing**: Secure payment handling and transaction management
                                - **Evaluation System**: Rating and feedback system for sessions
                                - **Notification System**: Real-time notifications for platform events
                                
                                ## Authentication
                                This API uses JWT (JSON Web Token) for authentication. To access protected endpoints:
                                1. Register or login to obtain a JWT token
                                2. Include the token in the Authorization header: `Bearer <your-jwt-token>`
                                
                                ## User Roles
                                - **STUDENT**: Can request sessions, communicate with tutors, make payments
                                - **TUTOR**: Can accept sessions, manage availability, receive payments
                                - **ADMIN**: Full platform management capabilities
                                
                                ## Rate Limiting
                                API requests are rate-limited to ensure fair usage and platform stability.
                                """)
                        .contact(new Contact()
                                .name("Tutoring Platform Support")
                                .email("support@tutoringplatform.com")
                                .url("https://tutoringplatform.com/support"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.tutoringplatform.com")
                                .description("Production Server")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token obtained from /api/auth/login or /api/auth/register/* endpoints")));
    }
}
