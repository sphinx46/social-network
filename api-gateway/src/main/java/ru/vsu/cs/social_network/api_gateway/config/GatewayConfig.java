package ru.vsu.cs.social_network.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class GatewayConfig {

    private final AuthenticationHeaderFilter authFilter;

    public GatewayConfig(AuthenticationHeaderFilter authFilter) {
        this.authFilter = authFilter;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // USER PROFILE AUTH ROUTE
                .route("user_profile_auth", r -> r.path("/api/profile/me/**")
                        .filters(f -> f
                                .filter(authFilter)
                                .circuitBreaker(c -> c.setName("userProfileCircuitBreaker")
                                        .setFallbackUri("forward:/fallbackRoute"))
                                .rewritePath("/api/profile/me/(?<segment>.*)", "/api/user-profile/me/${segment}"))
                        .uri("lb://user-profile-service"))

                // USER PROFILE PUBLIC ROUTE
                .route("user_profile_public", r -> r.path("/api/profile/**")
                        .filters(f -> f
                                .circuitBreaker(c -> c.setName("userProfileCircuitBreaker")
                                        .setFallbackUri("forward:/fallbackRoute"))
                                .rewritePath("/api/profile/(?<segment>.*)", "/api/user-profile/${segment}"))
                        .uri("lb://user-profile-service"))

                // KEYCLOAK ROUTE
                .route("keycloak_auth", r -> r.path("/auth/**")
                        .or()
                        .path("/realms/**")
                        .or()
                        .path("/admin/**")
                        .uri("http://keycloak:8080"))

                // FALLBACK
                .route("fallbackRoute", r -> r.path("/fallbackRoute")
                        .filters(f -> f.rewritePath("/fallbackRoute", "/"))
                        .uri("forward:/fallback"))

                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> fallbackHandler() {
        return RouterFunctions.route()
                .GET("/fallback", request -> ServerResponse
                        .status(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)
                        .bodyValue("[ 'Service Unavailable. Please try again later.' ]"))
                .build();
    }
}