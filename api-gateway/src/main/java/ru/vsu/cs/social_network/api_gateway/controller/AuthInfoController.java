package ru.vsu.cs.social_network.api_gateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class AuthInfoController {

    /**
     * Возвращает access token текущего аутентифицированного пользователя из сессии.
     *
     * @param client авторизованный OAuth2 клиент из сессии
     * @return Mono с access token
     */
    @Operation(summary = "Получение access токена")
    @GetMapping("/access-token")
    public Mono<OAuth2AccessToken> getAccessToken(
            @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient client) {
        log.info("ШЛЮЗ_ТОКЕН_ACCESS_НАЧАЛО: запрос access token");
        OAuth2AccessToken token = client.getAccessToken();
        log.info("ШЛЮЗ_ТОКЕН_ACCESS_УСПЕХ: access token получен");
        return Mono.just(token);
    }

    /**
     * Возвращает ID token текущего аутентифицированного пользователя из сессии.
     *
     * @param oidcUser OIDC пользователь из principal
     * @return Mono с ID token
     */
    @Operation(summary = "Получение id токена")
    @GetMapping("/id-token")
    public Mono<org.springframework.security.oauth2.core.oidc.OidcIdToken> getIdToken(
            @AuthenticationPrincipal OidcUser oidcUser) {
        log.info("ШЛЮЗ_ТОКЕН_ID_НАЧАЛО: запрос ID token для пользователя {}", oidcUser.getPreferredUsername());
        org.springframework.security.oauth2.core.oidc.OidcIdToken token = oidcUser.getIdToken();
        log.info("ШЛЮЗ_ТОКЕН_ID_УСПЕХ: ID token получен");
        return Mono.just(token);
    }
}
