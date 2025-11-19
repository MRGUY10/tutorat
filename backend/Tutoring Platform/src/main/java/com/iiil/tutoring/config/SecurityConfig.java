package com.iiil.tutoring.config;

import com.iiil.tutoring.security.JwtAuthenticationFilter;
import com.iiil.tutoring.security.ReactiveUserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for JWT-based authentication
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Autowired
    private ReactiveUserDetailsServiceImpl userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints
                        .pathMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/auth/check-email").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/system/**").permitAll()
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        
                        // Swagger/OpenAPI endpoints
                        .pathMatchers("/swagger-ui.html").permitAll()
                        .pathMatchers("/swagger-ui/**").permitAll()
                        .pathMatchers("/v3/api-docs/**").permitAll()
                        .pathMatchers("/webjars/**").permitAll()
                        .pathMatchers("/v3/api-docs/**", "/**", "/webjars/**").permitAll()

                        // Admin endpoints
                        .pathMatchers("/api/admin/**").hasRole("ADMIN")
                        
                        // Student endpoints
                        .pathMatchers("/api/student/**").hasAnyRole("STUDENT", "ADMIN")
                        
                        // Tutor endpoints
                        .pathMatchers("/api/tutor/**").hasAnyRole("TUTOR", "ADMIN")
                        
                        // General user endpoints
                        .pathMatchers("/api/user/**").hasAnyRole("STUDENT", "TUTOR", "ADMIN")
                        
                        // Session management endpoints
                        .pathMatchers(HttpMethod.GET, "/api/sessions/**").hasAnyRole("STUDENT", "TUTOR", "ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/sessions/**").hasAnyRole("STUDENT", "TUTOR", "ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/sessions/**").hasAnyRole("STUDENT", "TUTOR", "ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/sessions/**").hasAnyRole("ADMIN", "TUTOR")
                        
                        // All other endpoints require authentication
                        .anyExchange().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .authenticationManager(reactiveAuthenticationManager())
                .build();
    }

    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager() {
        UserDetailsRepositoryReactiveAuthenticationManager authManager = 
                new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        authManager.setPasswordEncoder(passwordEncoder);
        return authManager;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}