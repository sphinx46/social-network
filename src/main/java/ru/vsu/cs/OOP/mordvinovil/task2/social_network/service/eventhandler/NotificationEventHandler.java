package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.eventhandler;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.GenericNotificationEvent;

public interface NotificationEventHandler {
    void handleNotificationEvent(GenericNotificationEvent event);
}