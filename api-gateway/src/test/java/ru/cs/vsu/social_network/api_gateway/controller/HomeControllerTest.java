package ru.cs.vsu.social_network.api_gateway.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOidcLogin;

/**
 * Интеграционные тесты для HomeController.
 * Тестирует работу контроллера с мок-аутентификацией.
 */
@WebFluxTest(HomeController.class)
@ActiveProfiles("test")
class HomeControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testHomeWithoutAuthentication() {
        webTestClient
                .get()
                .uri("/")
                .exchange()
                .expectStatus().is3xxRedirection();
    }

    @Test
    void testHomeWithAuthentication() {
        String username = "testuser";
        String userId = "123e4567-e89b-12d3-a456-426614174000";

        webTestClient
                .mutateWith(mockOidcLogin()
                        .idToken(token -> token
                                .subject(userId)
                                .claim("preferred_username", username)))
                .get()
                .uri("/")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assert body.contains("Authentication successful");
                    assert body.contains(username);
                });
    }
}

