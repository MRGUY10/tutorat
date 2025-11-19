package com.iiil.tutoring.config;

import com.iiil.tutoring.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Database initialization component to set up default roles
 */
@Component
public class DatabaseInitializer implements ApplicationRunner {

    @Autowired
    private RoleService roleService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Initialize default roles when the application starts
        roleService.initializeDefaultRoles()
                .doOnSuccess(v -> System.out.println("✅ Default roles initialized successfully"))
                .doOnError(error -> System.err.println("❌ Failed to initialize default roles: " + error.getMessage()))
                .subscribe();
    }
}