package ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.notification;

public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException(String message) {
        super(message);
    }
}
