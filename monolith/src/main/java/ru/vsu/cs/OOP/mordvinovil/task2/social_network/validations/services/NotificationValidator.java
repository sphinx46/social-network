package ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;

public interface NotificationValidator {
    void validateNotificationAccess(Long notificationId, User currentUser);
    void validateUserNotificationsAccess(User targetUser, User currentUser);
}