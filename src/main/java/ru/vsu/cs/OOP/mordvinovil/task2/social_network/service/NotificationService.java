package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

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
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.NotificationNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.UserNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.NotificationRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.UserRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.servicesImpl.NotificationValidatorImpl;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EntityMapper entityMapper;
    private final WebSocketNotificationService webSocketNotificationService;
    private final NotificationValidatorImpl notificationValidator;

    @EventListener
    @Async("notificationTaskExecutor")
    public void handleNotificationEvent(GenericNotificationEvent event) {
        try {
            User targetUser = userRepository.findById(event.getTargetUserId())
                    .orElseThrow(() -> new RuntimeException(ResponseMessageConstants.NOT_FOUND));

            Notification notification = createNotificationFromEvent(event, targetUser);
            notificationRepository.save(notification);

            webSocketNotificationService.sendNotification(targetUser.getId(), notification);

            log.debug("Notification created: {} for user: {}", event.getNotifitcationType(), targetUser.getUsername());

        } catch (Exception e) {
            log.error("Error processing notification event: {} for user: {}",
                    event.getNotifitcationType(), event.getTargetUserId(), e);
        }
    }

    public List<NotificationResponse> getUserNotifications(User user, User currentUser) {
        notificationValidator.validateUserNotificationsAccess(user, currentUser);

        List<Notification> notifications = notificationRepository
                .findByUserActionOrderByCreatedAtDesc(user);
        return entityMapper.mapList(notifications, NotificationResponse.class);
    }

    public NotificationResponse getUserNotificationById(Long id, User currentUser) {
        notificationValidator.validateNotificationAccess(id, currentUser);

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(ResponseMessageConstants.NOT_FOUND));
        return entityMapper.map(notification, NotificationResponse.class);
    }

    public List<NotificationResponse> getUnreadNotifications(User user, User currentUser) {
        notificationValidator.validateUserNotificationsAccess(user, currentUser);

        List<Notification> notifications = notificationRepository
                .findByUserActionAndStatusOrderByCreatedAtDesc(user, NotificationStatus.UNREAD);
        return entityMapper.mapList(notifications, NotificationResponse.class);
    }

    public Long getUnreadNotificationsCount(User user, User currentUser) {
        notificationValidator.validateUserNotificationsAccess(user, currentUser);

        return notificationRepository.countByUserActionAndStatus(user, NotificationStatus.UNREAD);
    }

    @Transactional
    public NotificationResponse markAsRead(Long id, User currentUser) {
        notificationValidator.validateNotificationOwnership(id, currentUser);

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(ResponseMessageConstants.NOT_FOUND));

        notification.setStatus(NotificationStatus.READ);
        notification.setUpdatedAt(LocalDateTime.now());

        Notification updated = notificationRepository.save(notification);

        return entityMapper.map(updated, NotificationResponse.class);
    }

    @Transactional
    public void markAllAsRead(User user, User currentUser) {
        notificationValidator.validateUserNotificationsAccess(user, currentUser);

        notificationRepository.markAllAsRead(user.getId(), NotificationStatus.READ);
        log.debug("All notifications marked as read for user: {}", user.getUsername());
    }

    @Transactional
    public void deleteNotification(Long notificationId, Long actionUserId, User currentUser) {
        User user = userRepository.findById(actionUserId)
                .orElseThrow(() -> new UserNotFoundException(ResponseMessageConstants.NOT_FOUND));
        Notification notification = notificationRepository.findByIdAndUserActionId(notificationId, actionUserId)
                .orElseThrow(() -> new NotificationNotFoundException(ResponseMessageConstants.NOT_FOUND));

        notificationValidator.validateUserNotificationsAccess(user, currentUser);

        notification.setStatus(NotificationStatus.DELETED);
        notification.setUpdatedAt(LocalDateTime.now());
        notificationRepository.save(notification);

        log.debug("Notification deleted: {} for user: {}", notificationId, user.getUsername());
    }

    @Transactional
    public void clearDeletedNotifications(User user, User currentUser) {
        notificationValidator.validateUserNotificationsAccess(user, currentUser);

        notificationRepository.deleteAllDeletedByUser(user);
        log.debug("Deleted notifications cleared for user: {}", user.getUsername());
    }

    private Notification createNotificationFromEvent(GenericNotificationEvent event, User targetUser) {
        return Notification.builder()
                .userAction(targetUser)
                .type(event.getNotifitcationType())
                .status(NotificationStatus.UNREAD)
                .additionalData(event.getAdditionalData())
                .createdAt(event.getTimeCreated())
                .updatedAt(event.getTimeCreated())
                .build();
    }
}