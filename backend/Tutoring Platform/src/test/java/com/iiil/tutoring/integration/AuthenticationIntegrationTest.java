package com.iiil.tutoring.integration;

import com.iiil.tutoring.dto.auth.AuthResponse;
import com.iiil.tutoring.dto.auth.LoginRequest;
import com.iiil.tutoring.dto.auth.StudentRegisterRequest;

import com.iiil.tutoring.enums.NiveauAcademique;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for authentication endpoints
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class AuthenticationIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void testStudentRegistration() {
        StudentRegisterRequest request = new StudentRegisterRequest();
        request.setNom("Jean");
        request.setPrenom("Dupont");
        request.setEmail("jean.dupont@test.com");
        request.setMotDePasse("password123");
        request.setTelephone("0123456789");
        request.setFiliere("Informatique");
        request.setAnnee(2);
        request.setNiveau(NiveauAcademique.INTERMEDIAIRE);

        webTestClient.post()
                .uri("/api/auth/register/student")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AuthResponse.class)
                .value(response -> {
                    assert response.getToken() != null;
                    assert response.getRefreshToken() != null;
                    assert response.getEmail().equals("jean.dupont@test.com");
                    assert response.getRole().equals("STUDENT");
                });
    }



    @Test
    public void testLogin() {
        // First register a student
        StudentRegisterRequest registerRequest = new StudentRegisterRequest();
        registerRequest.setNom("Test");
        registerRequest.setPrenom("User");
        registerRequest.setEmail("test.user@test.com");
        registerRequest.setMotDePasse("password123");
        registerRequest.setTelephone("0123456789");
        registerRequest.setFiliere("Informatique");
        registerRequest.setAnnee(1);
        registerRequest.setNiveau(NiveauAcademique.DEBUTANT);

        webTestClient.post()
                .uri("/api/auth/register/student")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(registerRequest)
                .exchange()
                .expectStatus().isCreated();

        // Then try to login
        LoginRequest loginRequest = new LoginRequest("test.user@test.com", "password123");

        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .value(response -> {
                    assert response.getToken() != null;
                    assert response.getRefreshToken() != null;
                    assert response.getEmail().equals("test.user@test.com");
                    assert response.getRole().equals("STUDENT");
                });
    }

    @Test
    public void testInvalidLogin() {
        LoginRequest loginRequest = new LoginRequest("invalid@test.com", "wrongpassword");

        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    public void testDuplicateEmailRegistration() {
        StudentRegisterRequest request = new StudentRegisterRequest();
        request.setNom("Duplicate");
        request.setPrenom("User");
        request.setEmail("duplicate@test.com");
        request.setMotDePasse("password123");
        request.setTelephone("0123456789");
        request.setFiliere("Informatique");
        request.setAnnee(1);
        request.setNiveau(NiveauAcademique.DEBUTANT);

        // First registration should succeed
        webTestClient.post()
                .uri("/api/auth/register/student")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();

        // Second registration with same email should fail
        webTestClient.post()
                .uri("/api/auth/register/student")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(409); // Conflict
    }

    @Test
    public void testEmailAvailabilityCheck() {
        // Check available email
        webTestClient.get()
                .uri("/api/auth/check-email?email=available@test.com")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(true);

        // Register user with email
        StudentRegisterRequest request = new StudentRegisterRequest();
        request.setNom("Taken");
        request.setPrenom("Email");
        request.setEmail("taken@test.com");
        request.setMotDePasse("password123");
        request.setTelephone("0123456789");
        request.setFiliere("Informatique");
        request.setAnnee(1);
        request.setNiveau(NiveauAcademique.DEBUTANT);

        webTestClient.post()
                .uri("/api/auth/register/student")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();

        // Check taken email
        webTestClient.get()
                .uri("/api/auth/check-email?email=taken@test.com")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(false);
    }
}