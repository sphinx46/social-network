package ru.vsu.cs.social_network.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final AuthenticationHeaderFilter authFilter;

    public GatewayConfig(AuthenticationHeaderFilter authFilter) {
        this.authFilter = authFilter;
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-profile-service-public", r -> r
                        .path("/api/profile/*")
                        .filters(f -> f.rewritePath("/api/profile/(?<segment>.*)", "/api/user-profile/profile/${segment}"))
                        .uri("lb://user-profile-service"))
                .route("user-profile-service-auth", r -> r
                        .path("/api/profile/me/**")
                        .filters(f -> f.filter(authFilter)
                                .rewritePath("/api/profile/me(?<segment>.*)", "/api/user-profile/profile${segment}"))
                        .uri("lb://user-profile-service"))
                .route("keycloak", r -> r
                        .path("/auth/**", "/realms/**", "/admin/**")
                        .uri("http://keycloak:8080"))
                .build();
    }
}