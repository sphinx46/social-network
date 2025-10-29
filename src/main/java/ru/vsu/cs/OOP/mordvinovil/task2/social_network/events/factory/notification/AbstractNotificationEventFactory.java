package ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.factory.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.NotificationType;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.notification.GenericNotificationEvent;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.utils.UserInfoService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractNotificationEventFactory implements NotificationEventFactory {
    protected final UserInfoService userInfoService;

    /**
     * Базовый метод для создания события с общими данными
     */
    protected GenericNotificationEvent createEvent(Object source, Long targetUserId,
                                                   NotificationType type, Map<String, Object> baseData) {
        Map<String, Object> finalData = new HashMap<>(baseData);
        return new GenericNotificationEvent(source, targetUserId, type, finalData);
    }

    /**
     * Создает событие с данными пользователя
     */
    protected GenericNotificationEvent createEventWithUserData(Object source, Long targetUserId,
                                                               NotificationType type, Long actorId,
                                                               String actorKey, Map<String, Object> additionalData) {
        Map<String, Object> data = new HashMap<>();

        data.put(actorKey, actorId);
        data.put(actorKey + "Username", userInfoService.getUsernameSafe(actorId));

        if (additionalData != null) {
            data.putAll(additionalData);
        }

        return createEvent(source, targetUserId, type, data);
    }

    /**
     * Создает событие с двумя пользователями (например, отправитель и получатель)
     */
    protected GenericNotificationEvent createEventWithTwoUsers(Object source, Long targetUserId,
                                                               NotificationType type, Long firstUserId,
                                                               String firstUserKey, Long secondUserId,
                                                               String secondUserKey, Map<String, Object> additionalData) {
        Map<String, Object> data = new HashMap<>();

        data.put(firstUserKey, firstUserId);
        data.put(firstUserKey + "Username", userInfoService.getUsernameSafe(firstUserId));

        data.put(secondUserKey, secondUserId);
        data.put(secondUserKey + "Username", userInfoService.getUsernameSafe(secondUserId));

        if (additionalData != null) {
            data.putAll(additionalData);
        }

        return createEvent(source, targetUserId, type, data);
    }
}
