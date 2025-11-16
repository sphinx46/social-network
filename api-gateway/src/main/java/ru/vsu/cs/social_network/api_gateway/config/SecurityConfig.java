package ru.vsu.cs.social_network.api_gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.DelegatingServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.SecurityContextServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.WebSessionServerLogoutHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession;

@Slf4j
@Configuration
@EnableRedisWebSession
public class SecurityConfig {

    @Value("${app.logout.redirect-uri:http://localhost:8082/}")
    private String logoutRedirectUri;

    /**
     * Настраивает Security Web Filter Chain для API Gateway.
     * Конфигурирует OAuth2 login, logout, CSRF защиту и правила авторизации.
     *
     * @param httpSecurity объект для настройки безопасности
     * @param authorizedClientRepository репозиторий для хранения авторизованных OAuth2 клиентов
     * @param authenticationSuccessHandler обработчик успешной аутентификации
     * @param logoutSuccessHandler обработчик успешного выхода
     * @param logoutHandler обработчик выхода
     * @param clientRegistrationRepository репозиторий регистраций OAuth2 клиентов
     * @return настроенная цепочка фильтров безопасности
     */
    @Bean
    SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity httpSecurity,
            ServerOAuth2AuthorizedClientRepository authorizedClientRepository,
            ServerAuthenticationSuccessHandler authenticationSuccessHandler,
            ServerLogoutSuccessHandler logoutSuccessHandler,
            ServerLogoutHandler logoutHandler,
            ReactiveClientRegistrationRepository clientRegistrationRepository) {
        log.info("ШЛЮЗ_БЕЗОПАСНОСТЬ_НАСТРОЙКА_НАЧАЛО: настройка Security Web Filter Chain");
        
        SecurityWebFilterChain chain = httpSecurity
                .authorizeExchange(
                        authorizeExchange ->
                                authorizeExchange.pathMatchers(
                                                "/actuator/health",
                                                "/actuator/info",
                                                "/auth/**",
                                                "/realms/**",
                                                "/eureka/**",
                                                "/oauth2/**",
                                                "/login/**",
                                                "/",
                                                "/api/profile/{keycloakUserId}")
                                        .permitAll()
                                        .pathMatchers("/api/profile/me", "/access-token/**", "/id-token")
                                        .authenticated()
                                        .anyExchange()
                                        .authenticated()
                )
                .oauth2Login(oauth2Login ->
                        oauth2Login
                                .authorizedClientRepository(authorizedClientRepository)
                                .authenticationSuccessHandler(authenticationSuccessHandler)
                                .authorizationRequestResolver(customAuthorizationRequestResolver(clientRegistrationRepository))
                )
                .logout(logout ->
                        logout.logoutSuccessHandler(logoutSuccessHandler)
                                .logoutHandler(logoutHandler)
                )
                .csrf(csrf -> csrf.disable())
                .build();
        
        log.info("ШЛЮЗ_БЕЗОПАСНОСТЬ_НАСТРОЙКА_УСПЕХ: Security Web Filter Chain настроен");
        return chain;
    }

    /**
     * Создает репозиторий для хранения авторизованных OAuth2 клиентов в сессии.
     *
     * @return репозиторий авторизованных клиентов
     */
    @Bean
    ServerOAuth2AuthorizedClientRepository authorizedClientRepository() {
        log.debug("ШЛЮЗ_БЕЗОПАСНОСТЬ_РЕПОЗИТОРИЙ: создание WebSessionServerOAuth2AuthorizedClientRepository");
        return new WebSessionServerOAuth2AuthorizedClientRepository();
    }

    /**
     * Создает обработчик успешной аутентификации, который перенаправляет на главную страницу.
     *
     * @return обработчик успешной аутентификации
     */
    @Bean
    ServerAuthenticationSuccessHandler authenticationSuccessHandler() {
        log.debug("ШЛЮЗ_БЕЗОПАСНОСТЬ_УСПЕХ: создание RedirectServerAuthenticationSuccessHandler");
        return new RedirectServerAuthenticationSuccessHandler("/");
    }

    /**
     * Создает обработчик успешного выхода, который также выходит из Keycloak SSO сессии.
     *
     * @param clientRegistrationRepository репозиторий регистраций OAuth2 клиентов
     * @return обработчик успешного выхода
     */
    @Bean
    ServerLogoutSuccessHandler logoutSuccessHandler(ReactiveClientRegistrationRepository clientRegistrationRepository) {
        log.debug("ШЛЮЗ_БЕЗОПАСНОСТЬ_ВЫХОД: создание OidcClientInitiatedServerLogoutSuccessHandler");
        OidcClientInitiatedServerLogoutSuccessHandler handler = 
                new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);
        handler.setPostLogoutRedirectUri(logoutRedirectUri);
        return handler;
    }

    /**
     * Создает обработчик выхода, который очищает SecurityContext и сессию.
     *
     * @return обработчик выхода
     */
    @Bean
    ServerLogoutHandler logoutHandler() {
        log.debug("ШЛЮЗ_БЕЗОПАСНОСТЬ_ОБРАБОТЧИК_ВЫХОДА: создание DelegatingServerLogoutHandler");
        return new DelegatingServerLogoutHandler(
                new SecurityContextServerLogoutHandler(),
                new WebSessionServerLogoutHandler()
        );
    }

    /**
     * Создает кастомный резолвер OAuth2 authorization request для передачи параметров (prompt, kc_idp_hint) в Keycloak.
     *
     * @param clientRegistrationRepository репозиторий регистраций OAuth2 клиентов
     * @return кастомный резолвер authorization request
     */
    @Bean
    ServerOAuth2AuthorizationRequestResolver customAuthorizationRequestResolver(
            ReactiveClientRegistrationRepository clientRegistrationRepository) {
        log.debug("ШЛЮЗ_БЕЗОПАСНОСТЬ_РЕЗОЛВЕР: создание CustomAuthorizationRequestResolver");
        DefaultServerOAuth2AuthorizationRequestResolver defaultResolver =
                new DefaultServerOAuth2AuthorizationRequestResolver(clientRegistrationRepository);
        return new CustomAuthorizationRequestResolver(defaultResolver);
    }
}
