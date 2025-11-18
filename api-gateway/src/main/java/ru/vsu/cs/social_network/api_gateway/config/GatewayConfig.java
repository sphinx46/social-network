package ru.vsu.cs.social_network.api_gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Configuration
public class GatewayConfig {

    /**
     * Создает резолвер ключа для rate limiting на основе user ID из OIDC или IP адреса.
     * Если пользователь аутентифицирован, используется его user ID из OIDC токена.
     * В противном случае используется IP адрес запроса.
     *
     * @return резолвер ключа для rate limiting
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> exchange.getPrincipal()
                .cast(OidcUser.class)
                .map(OidcUser::getSubject)
                .cast(String.class)
                .switchIfEmpty(Mono.defer(() -> {
                    String ip = Optional.ofNullable(
                                    exchange.getRequest().getHeaders().getFirst("X-Forwarded-For"))
                            .orElseGet(() ->
                                    Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                                            .map(addr -> addr.getAddress().getHostAddress())
                                            .orElse("unknown"));
                    return Mono.just(ip);
                }));
    }
}
