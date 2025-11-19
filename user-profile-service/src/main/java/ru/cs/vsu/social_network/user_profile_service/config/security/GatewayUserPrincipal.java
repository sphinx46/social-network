package ru.cs.vsu.social_network.user_profile_service.config.security;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * DTO с контекстом пользователя, прошедшего проверку подписи на уровне API Gateway.
 */
@Value
@Builder
public class GatewayUserPrincipal {
    UUID userId;
    String username;
    String email;
    String firstName;
    String lastName;
}

