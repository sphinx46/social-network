package ru.vsu.cs.social_network.api_gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
public class UserContextFilter extends AbstractGatewayFilterFactory<UserContextFilter.Config> {

    public UserContextFilter() {
        super(Config.class);
    }

    /**
     * Применяет фильтр для извлечения информации о пользователе из OIDC и добавления её в заголовки запроса.
     *
     * @param config конфигурация фильтра
     * @return Gateway фильтр
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!config.isEnabled()) {
                return chain.filter(exchange);
            }

            String path = exchange.getRequest().getPath().value();
            log.debug("ШЛЮЗ_КОНТЕКСТ_НАЧАЛО: извлечение контекста пользователя для пути: {}", path);

            return ReactiveSecurityContextHolder.getContext()
                    .cast(SecurityContext.class)
                    .map(SecurityContext::getAuthentication)
                    .filter(authentication -> authentication != null && authentication.getPrincipal() instanceof OidcUser)
                    .map(authentication -> (OidcUser) authentication.getPrincipal())
                    .map(oidcUser -> {
                        try {
                            String userId = oidcUser.getSubject();
                            if (userId == null || userId.isEmpty()) {
                                log.warn("ШЛЮЗ_КОНТЕКСТ_ОШИБКА: отсутствует subject (user ID) в OIDC user");
                                throw new IllegalStateException("OIDC subject (user ID) is missing");
                            }

                            UUID.fromString(userId);

                            String username = oidcUser.getPreferredUsername();
                            String email = oidcUser.getEmail();
                            String firstName = oidcUser.getGivenName();
                            String lastName = oidcUser.getFamilyName();

                            log.debug("ШЛЮЗ_КОНТЕКСТ_ДАННЫЕ: userId={}, username={}", userId, username);

                            ServerHttpRequest request = exchange.getRequest().mutate()
                                    .header("X-User-Id", userId)
                                    .header("X-Username", username != null ? username : "")
                                    .header("X-Email", email != null ? email : "")
                                    .header("X-First-Name", firstName != null ? firstName : "")
                                    .header("X-Last-Name", lastName != null ? lastName : "")
                                    .build();

                            log.debug("ШЛЮЗ_КОНТЕКСТ_УСПЕХ: заголовки пользователя добавлены для userId: {}", userId);
                            return exchange.mutate().request(request).build();
                        } catch (IllegalArgumentException e) {
                            log.error("ШЛЮЗ_КОНТЕКСТ_ОШИБКА: неверный формат user ID в OIDC user: {}", e.getMessage());
                            throw new IllegalStateException("Invalid user ID format in OIDC user", e);
                        } catch (Exception e) {
                            log.error("ШЛЮЗ_КОНТЕКСТ_ОШИБКА: ошибка извлечения контекста пользователя: {}", e.getMessage());
                            throw new IllegalStateException("Error extracting user context from OIDC user", e);
                        }
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        log.warn("ШЛЮЗ_КОНТЕКСТ_ОШИБКА: OIDC user не найден в SecurityContext для запроса: {}", path);
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return Mono.error(new IllegalStateException("OIDC user not found in SecurityContext"));
                    }))
                    .flatMap(chain::filter)
                    .onErrorResume(IllegalStateException.class, e -> {
                        log.error("ШЛЮЗ_КОНТЕКСТ_ОШИБКА_АУТЕНТИФИКАЦИЯ: {}", e.getMessage());
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        };
    }

    public static class Config {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
