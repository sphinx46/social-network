package ru.cs.vsu.social_network.api_gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit тесты для UserContextFilter.
 * Тестирует базовую логику конфигурации фильтра.
 */
class UserContextFilterTest {

    private UserContextFilter filter;
    private GatewayFilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new UserContextFilter();
        filterChain = mock(GatewayFilterChain.class);
        when(filterChain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void testFilterDisabledDoesNotProcess() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/profile/me").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        UserContextFilter.Config config = new UserContextFilter.Config();
        config.setEnabled(false);
        var gatewayFilter = filter.apply(config);

        StepVerifier.create(gatewayFilter.filter(exchange, filterChain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void testConfigEnabled() {
        UserContextFilter.Config config = new UserContextFilter.Config();

        assertThat(config.isEnabled()).isTrue();

        config.setEnabled(false);
        assertThat(config.isEnabled()).isFalse();
    }
}

