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
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging.CentralLogger;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationServiceImpl implements WebSocketNotificationService {
    private final SimpMessagingTemplate messagingTemplate;
    private final EntityMapper entityMapper;
    private final CentralLogger centralLogger;

    /**
     * Отправляет уведомление конкретному пользователю через WebSocket
     *
     * @param targetUserId идентификатор пользователя-получателя
     * @param notification сущность уведомления для отправки
     */
    @Override
    public void sendNotification(Long targetUserId, Notification notification) {
        Map<String, Object> context = new HashMap<>();
        context.put("targetUserId", targetUserId);
        context.put("notificationId", notification.getId());
        context.put("notificationType", notification.getType());

        centralLogger.logInfo("WEB_SOCKET_УВЕДОМЛЕНИЕ_ОТПРАВКА",
                "Отправка WebSocket уведомления", context);

        try {
            NotificationResponse response = entityMapper.map(notification, NotificationResponse.class);
            WebSocketMessage<NotificationResponse> message =
                    WebSocketMessage.success("NOTIFICATION", response);

            messagingTemplate.convertAndSendToUser(targetUserId.toString(),
                    "/queue/notifications", message);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("messageType", message.getType());

            centralLogger.logInfo("WEB_SOCKET_УВЕДОМЛЕНИЕ_ОТПРАВЛЕНО",
                    "WebSocket уведомление успешно отправлено", successContext);
        } catch (Exception e) {
            centralLogger.logError("WEB_SOCKET_УВЕДОМЛЕНИЕ_ОШИБКА_ОТПРАВКИ",
                    "Ошибка при отправке WebSocket уведомления", context, e);
        }
    }
}