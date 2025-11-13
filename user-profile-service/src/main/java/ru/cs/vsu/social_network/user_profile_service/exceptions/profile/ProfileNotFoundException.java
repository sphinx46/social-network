package ru.cs.vsu.social_network.user_profile_service.exceptions.profile;

public class ProfileNotFoundException extends RuntimeException {
    public ProfileNotFoundException(String message) {
        super(message);
    }
}