package ru.cs.vsu.social_network.upload_service.config.security;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
@SuppressWarnings("VisibilityModifier")
public class GatewayUserPrincipal {
    UUID userId;
    String username;
    String email;
    String firstName;
    String lastName;
}

