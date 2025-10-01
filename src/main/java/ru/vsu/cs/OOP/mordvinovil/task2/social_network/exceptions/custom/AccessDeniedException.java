package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.custom;

public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}