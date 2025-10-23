package ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.NotifitcationType;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.GenericNotificationEvent;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.utils.NotificationEventUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractNotificationEventFactory implements NotificationEventFactory {
    protected final NotificationEventUtils utils;

    /**
     * Базовый метод для создания события с общими данными
     */
    protected GenericNotificationEvent createEvent(Object source, Long targetUserId,
                                                   NotifitcationType type, Map<String, Object> baseData) {
        Map<String, Object> finalData = new HashMap<>(baseData);
        return new GenericNotificationEvent(source, targetUserId, type, finalData);
    }

    /**
     * Создает событие с данными пользователя
     */
    protected GenericNotificationEvent createEventWithUserData(Object source, Long targetUserId,
                                                               NotifitcationType type, Long actorId,
                                                               String actorKey, Map<String, Object> additionalData) {
        Map<String, Object> data = new HashMap<>();

        data.put(actorKey, actorId);
        data.put(actorKey + "Username", utils.getUsernameSafe(actorId));

        if (additionalData != null) {
            data.putAll(additionalData);
        }

        return createEvent(source, targetUserId, type, data);
    }

    /**
     * Создает событие с двумя пользователями (например, отправитель и получатель)
     */
    protected GenericNotificationEvent createEventWithTwoUsers(Object source, Long targetUserId,
                                                               NotifitcationType type, Long firstUserId,
                                                               String firstUserKey, Long secondUserId,
                                                               String secondUserKey, Map<String, Object> additionalData) {
        Map<String, Object> data = new HashMap<>();

        data.put(firstUserKey, firstUserId);
        data.put(firstUserKey + "Username", utils.getUsernameSafe(firstUserId));

        data.put(secondUserKey, secondUserId);
        data.put(secondUserKey + "Username", utils.getUsernameSafe(secondUserId));

        if (additionalData != null) {
            data.putAll(additionalData);
        }

        return createEvent(source, targetUserId, type, data);
    }
}