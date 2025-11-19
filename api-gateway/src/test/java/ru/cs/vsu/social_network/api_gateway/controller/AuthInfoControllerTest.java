package ru.cs.vsu.social_network.api_gateway.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Instant;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOAuth2Client;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOidcLogin;

/**
 * Интеграционные тесты для AuthInfoController.
 * Тестирует получение токенов через контроллер.
 */
@WebFluxTest(AuthInfoController.class)
@ActiveProfiles("test")
class AuthInfoControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testGetAccessToken() {
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        String username = "testuser";
        String tokenValue = "test-access-token";

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                tokenValue,
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );

        webTestClient
                .mutateWith(mockOidcLogin()
                        .idToken(token -> token
                                .subject(userId)
                                .claim("preferred_username", username)))
                .mutateWith(mockOAuth2Client("keycloak")
                        .accessToken(accessToken))
                .get()
                .uri("/access-token")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.tokenValue").exists();
    }

    @Test
    void testGetIdToken() {
        String userId = "123e4567-e89b-12d3-a456-426614174000";
        String username = "testuser";

        webTestClient
                .mutateWith(mockOidcLogin()
                        .idToken(token -> token
                                .subject(userId)
                                .claim("preferred_username", username)))
                .get()
                .uri("/id-token")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.subject").isEqualTo(userId);
    }
}

