package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.NotificationResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.websocket.WebSocketMessage;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Notification;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {
    private final SimpMessagingTemplate messagingTemplate;
    private final EntityMapper entityMapper;

    public void sendNotification(Long targetUserId, Notification notification) {
        try {
            NotificationResponse response = entityMapper.map(notification, NotificationResponse.class);
            WebSocketMessage<NotificationResponse> message =
                    WebSocketMessage.success("NOTIFICATION", response);

            messagingTemplate.convertAndSendToUser(targetUserId.toString(),
                    "/queue/notifications", message);
            log.debug("WebSocket notification sent to user {}: {}", targetUserId, notification.getType());
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification to user {}: {}",
                    targetUserId, e.getMessage());
        }
    }
}
