package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.notification.NotificationResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.websocket.WebSocketMessage;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Notification;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.notification.WebSocketNotificationService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationServiceImpl implements WebSocketNotificationService {
    private final SimpMessagingTemplate messagingTemplate;
    private final EntityMapper entityMapper;

    /**
     * Отправляет уведомление конкретному пользователю через WebSocket
     *
     * @param targetUserId идентификатор пользователя-получателя
     * @param notification сущность уведомления для отправки
     */
    @Override
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