package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.eventhandler.notification;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.notification.GenericNotificationEvent;

public interface NotificationEventHandler {
    void handleNotificationEvent(GenericNotificationEvent event);
}