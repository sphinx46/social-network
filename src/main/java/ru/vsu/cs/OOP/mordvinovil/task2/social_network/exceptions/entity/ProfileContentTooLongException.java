package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity;

public class ProfileContentTooLongException extends RuntimeException {
    public ProfileContentTooLongException(String message) {
        super(message);
    }
}
