package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.eventhandler.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Notification;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.notification.GenericNotificationEvent;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.NotificationRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.UserRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.notification.WebSocketNotificationService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.NotificationFactory;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventHandlerImpl implements NotificationEventHandler {
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final WebSocketNotificationService webSocketNotificationService;
    private final NotificationFactory factory;

    @EventListener
    @Async("notificationTaskExecutor")
    @Override
    public void handleNotificationEvent(GenericNotificationEvent event) {
        try {
            User targetUser = userRepository.findById(event.getTargetUserId())
                    .orElseThrow(() -> new RuntimeException(ResponseMessageConstants.FAILURE_USER_NOT_FOUND));

            Notification notification = factory.createNotificationFromEvent(event, targetUser);
            notificationRepository.save(notification);

            webSocketNotificationService.sendNotification(targetUser.getId(), notification);

            log.debug("Notification created: {} for user: {}", event.getNotificationType(), targetUser.getUsername());

        } catch (Exception e) {
            log.error("Error processing notification event: {} for user: {}",
                    event.getNotificationType(), event.getTargetUserId(), e);
        }
    }
}