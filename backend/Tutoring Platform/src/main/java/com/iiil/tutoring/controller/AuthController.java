package com.iiil.tutoring.controller;

import com.iiil.tutoring.dto.auth.*;
import com.iiil.tutoring.dto.tutor.TutorRegistrationRequest;
import com.iiil.tutoring.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication controller handling login, registration, and token management
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Authentication", description = "Authentication and user registration endpoints")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Login endpoint
     */
    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticate a user with email and password. Returns JWT tokens for accessing protected endpoints.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    name = "Login Example",
                                    value = """
                                            {
                                              "email": "student@example.com",
                                              "motDePasse": "password123"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "Successful Login",
                                    value = """
                                            {
                                              "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                              "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                              "expiresIn": 3600,
                                              "email": "student@example.com",
                                              "role": "STUDENT",
                                              "userId": 123
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials or account disabled",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Invalid Credentials",
                                    value = """
                                            {
                                              "error": "Invalid credentials",
                                              "message": "Email or password is incorrect"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request format",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Validation Error",
                                    value = """
                                            {
                                              "error": "Validation failed",
                                              "details": ["Email is required", "Password must be at least 6 characters"]
                                            }
                                            """
                            )
                    )
            )
    })
    public Mono<ResponseEntity<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest)
                .map(authResponse -> ResponseEntity.ok(authResponse))
                .onErrorReturn(throwable -> throwable instanceof org.springframework.security.core.userdetails.UsernameNotFoundException ||
                                          throwable instanceof org.springframework.security.authentication.BadCredentialsException,
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED).build())
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    /**
     * Student registration endpoint
     */
    @PostMapping("/register/student")
    @Operation(
            summary = "Register a new student",
            description = "Create a new student account with academic information. Returns JWT tokens for immediate access.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Student registration information",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StudentRegisterRequest.class),
                            examples = @ExampleObject(
                                    name = "Student Registration",
                                    value = """
                                            {
                                              "nom": "Dupont",
                                              "prenom": "Jean",
                                              "email": "jean.dupont@student.com",
                                              "motDePasse": "securePassword123",
                                              "telephone": "0123456789",
                                              "filiere": "Informatique",
                                              "annee": 2,
                                              "niveau": "INTERMEDIAIRE"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Student account created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email already exists",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Email Conflict",
                                    value = """
                                            {
                                              "error": "Email already exists",
                                              "message": "An account with this email already exists"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid registration data",
                    content = @Content(mediaType = "application/json")
            )
    })
    public Mono<ResponseEntity<AuthResponse>> registerStudent(@Valid @RequestBody StudentRegisterRequest request) {
        return authService.registerStudent(request)
                .map(authResponse -> ResponseEntity.status(HttpStatus.CREATED).body(authResponse))
                .onErrorReturn(throwable -> throwable instanceof IllegalArgumentException,
                        ResponseEntity.status(HttpStatus.CONFLICT).build())
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }



    /**
     * Admin registration endpoint (Super Admin only)
     */
    @PostMapping("/register/admin")
    @Operation(
            summary = "Register a new admin user",
            description = "Create a new admin account with management permissions. This endpoint should be restricted to super admin users.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Admin registration information",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.iiil.tutoring.dto.admin.CreateAdminRequest.class),
                            examples = @ExampleObject(
                                    name = "Admin Registration",
                                    value = """
                                            {
                                              "nom": "Administrator",
                                              "prenom": "System",
                                              "email": "admin@tutoring.com",
                                              "motDePasse": "secureAdminPassword123",
                                              "telephone": "0123456789",
                                              "permissions": "READ_WRITE_ALL",
                                              "departement": "IT Management"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Admin account created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email already exists",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Email Conflict",
                                    value = """
                                            {
                                              "error": "Email already exists",
                                              "message": "An account with this email already exists"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid registration data",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Insufficient permissions",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Access Denied",
                                    value = """
                                            {
                                              "error": "Access denied",
                                              "message": "Only super admin users can create admin accounts"
                                            }
                                            """
                            )
                    )
            )
    })
    public Mono<ResponseEntity<AuthResponse>> registerAdmin(@Valid @RequestBody com.iiil.tutoring.dto.admin.CreateAdminRequest request) {
        return authService.registerAdmin(request)
                .map(authResponse -> ResponseEntity.status(HttpStatus.CREATED).body(authResponse))
                .onErrorReturn(throwable -> throwable instanceof IllegalArgumentException,
                        ResponseEntity.status(HttpStatus.CONFLICT).build())
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    /**
     * Tutor registration endpoint
     */
    @PostMapping("/register/tutor")
    @Operation(
            summary = "Tutor registration",
            description = "Register a new tutor account with profile information and teaching specialties. Use GET /api/matieres to get available subject IDs for specialiteIds field.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Tutor registration details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TutorRegistrationRequest.class),
                            examples = @ExampleObject(
                                    name = "Tutor Registration",
                                    value = """
                                            {
                                              "nom": "Martin",
                                              "prenom": "Jean",
                                              "email": "jean.martin@tutor.com",
                                              "motDePasse": "TutorPass123!",
                                              "telephone": "0612345678",
                                              "specialiteIds": [1, 3, 5],
                                              "tarifHoraire": 25.00,
                                              "experience": "10 ans d'expérience en enseignement",
                                              "diplomes": "Master en Mathématiques, CAPES",
                                              "description": "Professeur de mathématiques expérimenté spécialisé en algèbre et analyse.",
                                              "ville": "Paris",
                                              "pays": "France",
                                              "coursEnLigne": true,
                                              "coursPresentiel": true
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Tutor registration successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "Successful Registration",
                                    value = """
                                            {
                                              "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                              "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                              "expiresIn": 3600,
                                              "email": "jean.martin@tutor.com",
                                              "role": "TUTOR",
                                              "userId": 456
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Tutor already exists with this email",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Email Conflict",
                                    value = """
                                            {
                                              "error": "Conflict",
                                              "message": "Un compte avec cet email existe déjà"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid registration data",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Validation Error",
                                    value = """
                                            {
                                              "error": "Validation Failed",
                                              "message": "Les données fournies ne sont pas valides"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Server Error",
                                    value = """
                                            {
                                              "error": "Internal Server Error",
                                              "message": "Une erreur interne s'est produite"
                                            }
                                            """
                            )
                    )
            )
    })
    public Mono<ResponseEntity<AuthResponse>> registerTutor(@Valid @RequestBody TutorRegistrationRequest request) {
        return authService.registerTutor(request)
                .map(authResponse -> ResponseEntity.status(HttpStatus.CREATED).body(authResponse))
                .onErrorReturn(throwable -> throwable instanceof IllegalArgumentException,
                        ResponseEntity.status(HttpStatus.CONFLICT).build())
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    /**
     * Token refresh endpoint
     */
    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh JWT token",
            description = "Generate a new access token using a valid refresh token. Use this when your access token expires.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token request",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RefreshTokenRequest.class),
                            examples = @ExampleObject(
                                    name = "Token Refresh",
                                    value = """
                                            {
                                              "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Invalid Refresh Token",
                                    value = """
                                            {
                                              "error": "Invalid refresh token",
                                              "message": "The refresh token is invalid or expired"
                                            }
                                            """
                            )
                    )
            )
    })
    public Mono<ResponseEntity<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request)
                .map(authResponse -> ResponseEntity.ok(authResponse))
                .onErrorReturn(throwable -> throwable instanceof org.springframework.security.authentication.BadCredentialsException ||
                                          throwable instanceof org.springframework.security.core.userdetails.UsernameNotFoundException,
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED).build())
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    /**
     * Logout endpoint (client-side token invalidation)
     */
    @PostMapping("/logout")
    @Operation(
            summary = "User logout",
            description = "Logout endpoint for client-side token invalidation. With JWT tokens, logout is primarily handled client-side by removing the token.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Logout successful",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "Logout Success",
                                            value = """
                                                    {
                                                      "message": "Logout successful"
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public Mono<ResponseEntity<Void>> logout() {
        // With JWT, logout is mainly handled client-side by removing the token
        // In a production environment, you might want to add token blacklisting
        return Mono.just(ResponseEntity.ok().build());
    }

    /**
     * Check if email is available
     */
    @GetMapping("/check-email")
    @Operation(
            summary = "Check email availability",
            description = "Check if an email address is available for registration. Returns true if the email can be used for a new account.",
            parameters = @Parameter(
                    name = "email",
                    description = "Email address to check",
                    required = true,
                    example = "user@example.com",
                    schema = @Schema(type = "string", format = "email")
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Email availability check completed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "boolean"),
                            examples = {
                                    @ExampleObject(
                                            name = "Email Available",
                                            value = "true",
                                            description = "Email is available for registration"
                                    ),
                                    @ExampleObject(
                                            name = "Email Taken",
                                            value = "false",
                                            description = "Email is already registered"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid email format",
                    content = @Content(mediaType = "application/json")
            )
    })
    public Mono<ResponseEntity<Boolean>> checkEmailAvailability(
            @RequestParam String email) {
        return authService.isEmailAvailable(email)
                .map(available -> ResponseEntity.ok(available))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    /**
     * Handle validation errors
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(WebExchangeBindException ex) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("timestamp", java.time.LocalDateTime.now().toString());
        errors.put("status", HttpStatus.BAD_REQUEST.value());
        errors.put("error", "Validation Failed");
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });
        errors.put("errors", fieldErrors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", java.time.LocalDateTime.now().toString());
        error.put("status", HttpStatus.CONFLICT.value());
        error.put("error", "Conflict");
        error.put("message", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Handle general exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", java.time.LocalDateTime.now().toString());
        error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.put("error", "Internal Server Error");
        error.put("message", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}