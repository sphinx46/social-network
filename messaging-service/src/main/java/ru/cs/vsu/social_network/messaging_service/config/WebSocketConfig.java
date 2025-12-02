package ru.cs.vsu.social_network.messaging_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Конфигурация WebSocket для мессенджера.
 * Настраивает STOMP брокер сообщений и конечные точки WebSocket.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${messaging.websocket.allowed-origins:http://localhost:*,http://127.0.0.1:*}")
    private String[] allowedOrigins;

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
        List<String> originList = Arrays.asList(allowedOrigins);
        String[] originPatterns = originList.toArray(new String[0]);

        registry.addEndpoint(WS_ENDPOINT)
                .setAllowedOriginPatterns(originPatterns)
                .withSockJS();

        registry.addEndpoint(WS_ENDPOINT)
                .setAllowedOriginPatterns(originPatterns);
    }
}