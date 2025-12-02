package ru.cs.vsu.social_network.messaging_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Конфигурация WebSocket для мессенджера.
 * Настраивает STOMP брокер сообщений и конечные точки WebSocket.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final String[] ALLOWED_ORIGINS = {"http://localhost:3000", "http://localhost:8080"};
    private static final String WS_ENDPOINT = "/ws-messaging";
    private static final String APP_DESTINATION_PREFIX = "/app";
    private static final String USER_DESTINATION_PREFIX = "/user";
    private static final String BROKER_TOPIC_PREFIX = "/topic";
    private static final String BROKER_QUEUE_PREFIX = "/queue";

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker(BROKER_TOPIC_PREFIX, BROKER_QUEUE_PREFIX);
        config.setApplicationDestinationPrefixes(APP_DESTINATION_PREFIX);
        config.setUserDestinationPrefix(USER_DESTINATION_PREFIX);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(WS_ENDPOINT)
                .setAllowedOriginPatterns("*")
                .withSockJS();

        registry.addEndpoint(WS_ENDPOINT)
                .setAllowedOriginPatterns("*");
    }
}