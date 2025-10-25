package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.NotificationResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Notification;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.NotificationStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.GenericNotificationEvent;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.NotificationRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.UserRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.NotificationService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.NotificationValidator;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EntityMapper entityMapper;
    private final WebSocketNotificationServiceImpl webSocketNotificationServiceImpl;
    private final NotificationValidator notificationValidator;
    private final EntityUtils entityUtils;

    @EventListener
    @Async("notificationTaskExecutor")
    @Override
    public void handleNotificationEvent(GenericNotificationEvent event) {
        try {
            User targetUser = userRepository.findById(event.getTargetUserId())
                    .orElseThrow(() -> new RuntimeException(ResponseMessageConstants.NOT_FOUND));

            Notification notification = createNotificationFromEvent(event, targetUser);
            notificationRepository.save(notification);

            webSocketNotificationServiceImpl.sendNotification(targetUser.getId(), notification);

            log.debug("Notification created: {} for user: {}", event.getNotificationType(), targetUser.getUsername());

        } catch (Exception e) {
            log.error("Error processing notification event: {} for user: {}",
                    event.getNotificationType(), event.getTargetUserId(), e);
        }
    }

    @Override
    public List<NotificationResponse> getUserNotifications(User currentUser) {
        List<Notification> notifications = notificationRepository
                .findByUserActionOrderByCreatedAtDesc(currentUser);
        return entityMapper.mapList(notifications, NotificationResponse.class);
    }

    @Override
    public NotificationResponse getUserNotificationById(Long id, User currentUser) {
        notificationValidator.validateNotificationAccess(id, currentUser);

        Notification notification = entityUtils.getNotification(id);
        return entityMapper.map(notification, NotificationResponse.class);
    }

    @Override
    public List<NotificationResponse> getUnreadNotifications(User currentUser) {
        List<Notification> notifications = notificationRepository
                .findByUserActionAndStatusOrderByCreatedAtDesc(currentUser, NotificationStatus.UNREAD);
        return entityMapper.mapList(notifications, NotificationResponse.class);
    }

    @Override
    public Long getUnreadNotificationsCount(User currentUser) {
        return notificationRepository.countByUserActionAndStatus(currentUser, NotificationStatus.UNREAD);
    }

    @Transactional
    @Override
    public NotificationResponse markAsRead(Long id, User currentUser) {
        notificationValidator.validateNotificationAccess(id, currentUser);

        Notification notification = entityUtils.getNotification(id);

        notification.setStatus(NotificationStatus.READ);
        notification.setUpdatedAt(LocalDateTime.now());

        Notification updated = notificationRepository.save(notification);

        return entityMapper.map(updated, NotificationResponse.class);
    }

    @Transactional
    @Override
    public void markAllAsRead(User currentUser) {
        notificationRepository.markAllAsRead(currentUser.getId(), NotificationStatus.READ);
        log.debug("All notifications marked as read for user: {}", currentUser.getUsername());
    }

    @Transactional
    @Override
    public void deleteNotification(Long notificationId, User currentUser) {
        Notification notification = entityUtils.getNotification(notificationId);
        User user = notification.getUserAction();

        notificationValidator.validateUserNotificationsAccess(user, currentUser);

        notification.setStatus(NotificationStatus.DELETED);
        notification.setUpdatedAt(LocalDateTime.now());
        notificationRepository.save(notification);

        log.debug("Notification deleted: {} for user: {}", notificationId, user.getUsername());
    }

    @Transactional
    @Override
    public void clearDeletedNotifications(User currentUser) {
        notificationRepository.deleteAllDeletedByUser(currentUser);
        log.debug("Deleted notifications cleared for user: {}", currentUser.getUsername());
    }

    private Notification createNotificationFromEvent(GenericNotificationEvent event, User targetUser) {
        return Notification.builder()
                .userAction(targetUser)
                .type(event.getNotificationType())
                .status(NotificationStatus.UNREAD)
                .additionalData(event.getAdditionalData())
                .createdAt(event.getTimeCreated())
                .updatedAt(event.getTimeCreated())
                .build();
    }
}