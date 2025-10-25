package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.NotificationResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.GenericNotificationEvent;

import java.util.List;

public interface NotificationService {
    void handleNotificationEvent(GenericNotificationEvent event);
    List<NotificationResponse> getUserNotifications(User currentUser);
    NotificationResponse getUserNotificationById(Long id, User currentUser);
    List<NotificationResponse> getUnreadNotifications(User currentUser);
    Long getUnreadNotificationsCount(User currentUser);
    NotificationResponse markAsRead(Long id, User currentUser);
    void markAllAsRead(User currentUser);
    void deleteNotification(Long notificationId, User currentUser);
    void clearDeletedNotifications(User currentUser);
}
