package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.NotificationResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Notification;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.NotificationStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.NotificationRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.NotificationService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.NotificationValidator;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final EntityMapper entityMapper;
    private final NotificationValidator notificationValidator;
    private final EntityUtils entityUtils;

    @Override
    public PageResponse<NotificationResponse> getUserNotifications(User currentUser, PageRequest pageRequest) {
        Page<Notification> notificationPage =
                notificationRepository.findByUserActionOrderByCreatedAtDesc(currentUser, pageRequest);
        return PageResponse.of(notificationPage.map
                (notification -> entityMapper.map(notification, NotificationResponse.class)));
    }

    @Override
    public NotificationResponse getUserNotificationById(Long id, User currentUser) {
        notificationValidator.validateNotificationAccess(id, currentUser);

        Notification notification = entityUtils.getNotification(id);
        return entityMapper.map(notification, NotificationResponse.class);
    }

    @Override
    public PageResponse<NotificationResponse> getUnreadNotifications(User currentUser, PageRequest pageRequest) {
        Page<Notification> notificationPage = notificationRepository
                .findByUserActionAndStatusOrderByCreatedAtDesc(currentUser, NotificationStatus.UNREAD, pageRequest);
        return PageResponse.of(notificationPage.map
                (notification -> entityMapper.map(notification, NotificationResponse.class)));
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
}