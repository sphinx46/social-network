package ru.cs.vsu.social_network.user_profile_service.exceptions.profile;

public class ProfileBioTooLongException extends RuntimeException {
    public ProfileBioTooLongException(String message) {
        super(message);
    }
}
