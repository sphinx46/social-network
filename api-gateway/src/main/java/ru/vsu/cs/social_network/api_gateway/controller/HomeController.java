package ru.vsu.cs.social_network.api_gateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class HomeController {

    @Operation(summary = "Переход на домашнюю страницу")
    @GetMapping("/")
    public Mono<ResponseEntity<String>> home(@AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser != null) {
            log.info("ШЛЮЗ_ГЛАВНАЯ_АУТЕНТИФИЦИРОВАН: пользователь {} запросил главную страницу", oidcUser.getPreferredUsername());
            return Mono.just(ResponseEntity.ok()
                    .body("Authentication successful! Welcome, " + oidcUser.getPreferredUsername() + 
                          "\n\nYou can now access protected endpoints:\n" +
                          "- GET /api/profile/me\n" +
                          "- GET /access-token\n" +
                          "- GET /id-token\n\n" +
                          "To logout: /logout\n" +
                          "To login as different user (force login screen): /oauth2/authorization/keycloak?prompt=login"));
        } else {
            log.debug("ШЛЮЗ_ГЛАВНАЯ_НЕАУТЕНТИФИЦИРОВАН: неаутентифицированный запрос главной страницы");
            return Mono.just(ResponseEntity.ok()
                    .body("API Gateway is running. Please authenticate at:\n" +
                          "http://localhost:8082/oauth2/authorization/keycloak\n\n" +
                          "To force login screen (disable SSO): /oauth2/authorization/keycloak?prompt=login"));
        }
    }
}
