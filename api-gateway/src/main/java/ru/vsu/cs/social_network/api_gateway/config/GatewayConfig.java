package ru.vsu.cs.social_network.api_gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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
                .onErrorResume(e -> Mono.empty())
                .flatMap(principal -> {
                    OidcUser oidcUser = null;
                    if (principal instanceof OAuth2AuthenticationToken) {
                        OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) principal;
                        if (oauth2Token.getPrincipal() instanceof OidcUser) {
                            oidcUser = (OidcUser) oauth2Token.getPrincipal();
                        }
                    } else if (principal instanceof OidcUser) {
                        oidcUser = (OidcUser) principal;
                    }
                    
                    if (oidcUser != null) {
                        String userId = oidcUser.getSubject();
                        if (userId != null && !userId.isEmpty()) {
                            return Mono.just(userId);
                        }
                    }
                    return Mono.empty();
                })
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
