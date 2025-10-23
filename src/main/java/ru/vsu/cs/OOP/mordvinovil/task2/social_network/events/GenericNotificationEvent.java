package ru.vsu.cs.OOP.mordvinovil.task2.social_network.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.NotifitcationType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
public class GenericNotificationEvent extends ApplicationEvent {
    private final Long targetUserId;
    private final NotifitcationType notifitcationType;
    private final Map<String, Object> additionalData;
    private final LocalDateTime timeCreated;

    public GenericNotificationEvent(Object source, Long targetUserId, NotifitcationType notifitcationType,
                                    Map<String, Object> additionalData) {
        super(source);
        this.targetUserId = targetUserId;
        this.notifitcationType = notifitcationType;
        this.additionalData = additionalData != null ? new HashMap<>(additionalData) : new HashMap<>();
        this.timeCreated = LocalDateTime.now();
    }

    public void addData(String key, Object value) {
        this.additionalData.put(key, value);
    }

    public Object getData(String key) {
        return this.additionalData.get(key);
    }
}
