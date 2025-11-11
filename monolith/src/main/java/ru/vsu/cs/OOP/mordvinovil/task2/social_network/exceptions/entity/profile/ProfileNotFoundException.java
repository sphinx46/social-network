package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.profile;

public class ProfileNotFoundException extends RuntimeException {
    public ProfileNotFoundException(String message) {
        super(message);
    }
}