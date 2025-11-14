package ru.vsu.cs.social_network.api_gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class GatewayConfig {

    /**
     * Создает резолвер ключа для rate limiting на основе user ID из OIDC или IP адреса.
     *
     * @return резолвер ключа для rate limiting
     */
    @Bean
    public KeyResolver userKeyResolver() {
        log.debug("ШЛЮЗ_КОНФИГ_РЕЗОЛВЕР: создание KeyResolver для rate limiting");
        return exchange -> {
            return exchange.getPrincipal()
                    .cast(OidcUser.class)
                    .map(oidcUser -> {
                        String userId = oidcUser.getSubject();
                        log.debug("ШЛЮЗ_КОНФИГ_РЕЗОЛВЕР_ПОЛЬЗОВАТЕЛЬ: использование userId для rate limiting: {}", userId);
                        return userId;
                    })
                    .cast(String.class)
                    .switchIfEmpty(
                            Mono.defer(() -> {
                                String ip = exchange.getRequest().getRemoteAddress() != null
                                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                                        : "unknown";
                                log.debug("ШЛЮЗ_КОНФИГ_РЕЗОЛВЕР_IP: использование IP адреса для rate limiting: {}", ip);
                                return Mono.just(ip);
                            })
                    );
        };
    }
}
