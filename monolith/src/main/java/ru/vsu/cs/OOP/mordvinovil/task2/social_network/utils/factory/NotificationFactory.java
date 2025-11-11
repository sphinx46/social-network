package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory;

import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Notification;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.NotificationStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.notification.GenericNotificationEvent;

@Component
public class NotificationFactory {
    public Notification createNotificationFromEvent(GenericNotificationEvent event, User targetUser) {
        return Notification.builder()
                .userAction(targetUser)
                .type(event.getNotificationType())
                .status(NotificationStatus.UNREAD)
                .additionalData(event.getAdditionalData())
                .updatedAt(event.getTimeCreated())
                .build();
    }
}
