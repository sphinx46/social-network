package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Notification;

public interface WebSocketNotificationService {
    void sendNotification(Long targetUserId, Notification notification);
}
