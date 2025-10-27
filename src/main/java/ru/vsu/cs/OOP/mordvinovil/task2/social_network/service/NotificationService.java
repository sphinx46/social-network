package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.NotificationResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;

public interface NotificationService {
    NotificationResponse getUserNotificationById(Long id, User currentUser);
    Long getUnreadNotificationsCount(User currentUser);
    NotificationResponse markAsRead(Long id, User currentUser);
    void markAllAsRead(User currentUser);
    void deleteNotification(Long notificationId, User currentUser);
    void clearDeletedNotifications(User currentUser);

    PageResponse<NotificationResponse> getUserNotifications(User currentUser, PageRequest pageRequest);
    PageResponse<NotificationResponse> getUnreadNotifications(User currentUser, PageRequest pageRequest);
}
