package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.eventhandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Notification;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.NotificationStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.GenericNotificationEvent;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.NotificationRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.UserRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.WebSocketNotificationService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventHandlerImpl implements NotificationEventHandler {
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final WebSocketNotificationService webSocketNotificationService;

    @EventListener
    @Async("notificationTaskExecutor")
    @Override
    public void handleNotificationEvent(GenericNotificationEvent event) {
        try {
            User targetUser = userRepository.findById(event.getTargetUserId())
                    .orElseThrow(() -> new RuntimeException(ResponseMessageConstants.FAILURE_USER_NOT_FOUND));

            Notification notification = createNotificationFromEvent(event, targetUser);
            notificationRepository.save(notification);

            webSocketNotificationService.sendNotification(targetUser.getId(), notification);

            log.debug("Notification created: {} for user: {}", event.getNotificationType(), targetUser.getUsername());

        } catch (Exception e) {
            log.error("Error processing notification event: {} for user: {}",
                    event.getNotificationType(), event.getTargetUserId(), e);
        }
    }

    private Notification createNotificationFromEvent(GenericNotificationEvent event, User targetUser) {
        return Notification.builder()
                .userAction(targetUser)
                .type(event.getNotificationType())
                .status(NotificationStatus.UNREAD)
                .additionalData(event.getAdditionalData())
                .updatedAt(event.getTimeCreated())
                .build();
    }
}