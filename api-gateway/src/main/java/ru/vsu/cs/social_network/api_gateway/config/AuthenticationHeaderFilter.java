package ru.vsu.cs.social_network.api_gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class AuthenticationHeaderFilter implements GatewayFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .cast(Jwt.class)
                .flatMap(jwt -> {
                    try {
                        String userId = jwt.getSubject();
                        String username = jwt.getClaim("preferred_username");

                        // Добавляем проверку на null и пустую строку
                        if (userId == null || userId.trim().isEmpty()) {
                            throw new IllegalArgumentException("User ID is missing in token");
                        }

                        // Пробуем распарсить UUID, но не сохраняем результат
                        UUID.fromString(userId);

                        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                .header("X-User-Id", userId)
                                .header("X-Username", username != null ? username : "")
                                .build();

                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    } catch (Exception e) {
                        // Добавляем логирование для отладки
                        System.err.println("Authentication error: " + e.getMessage());
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }
                })
                .switchIfEmpty(Mono.defer(() -> {
                    System.err.println("No authentication context found");
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }));
    }
}