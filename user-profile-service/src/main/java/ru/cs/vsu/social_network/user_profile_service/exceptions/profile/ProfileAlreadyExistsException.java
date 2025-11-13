package ru.cs.vsu.social_network.user_profile_service.exceptions.profile;

public class ProfileAlreadyExistsException extends RuntimeException {
    public ProfileAlreadyExistsException(String message) {
        super(message);
    }
}
