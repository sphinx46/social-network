package ru.cs.vsu.social_network.user_profile_service.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String extractUserName(String token);
    boolean isTokenValid(String token, UserDetails userDetails);
}
