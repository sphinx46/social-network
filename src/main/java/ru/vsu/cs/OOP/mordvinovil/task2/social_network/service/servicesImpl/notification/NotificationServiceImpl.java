package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.NotificationResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Notification;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.NotificationStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.NotificationRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.notification.NotificationService;
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

    /**
     * Получает уведомления пользователя с пагинацией
     *
     * @param currentUser текущий пользователь
     * @param pageRequest параметры пагинации
     * @return страница с уведомлениями пользователя
     */
    @Override
    public PageResponse<NotificationResponse> getUserNotifications(User currentUser, PageRequest pageRequest) {
        Page<Notification> notificationPage =
                notificationRepository.findByUserAction(currentUser, pageRequest.toPageable());
        return PageResponse.of(notificationPage.map
                (notification -> entityMapper.map(notification, NotificationResponse.class)));
    }

    /**
     * Получает конкретное уведомление по идентификатору
     *
     * @param id идентификатор уведомления
     * @param currentUser текущий пользователь
     * @return ответ с данными уведомления
     */
    @Override
    public NotificationResponse getUserNotificationById(Long id, User currentUser) {
        notificationValidator.validateNotificationAccess(id, currentUser);

        Notification notification = entityUtils.getNotification(id);
        return entityMapper.map(notification, NotificationResponse.class);
    }

    /**
     * Получает непрочитанные уведомления пользователя
     *
     * @param currentUser текущий пользователь
     * @param pageRequest параметры пагинации
     * @return страница с непрочитанными уведомлениями
     */
    @Override
    public PageResponse<NotificationResponse> getUnreadNotifications(User currentUser, PageRequest pageRequest) {
        Page<Notification> notificationPage = notificationRepository
                .findByUserActionAndStatus(currentUser, NotificationStatus.UNREAD, pageRequest.toPageable());
        return PageResponse.of(notificationPage.map
                (notification -> entityMapper.map(notification, NotificationResponse.class)));
    }

    /**
     * Получает количество непрочитанных уведомлений пользователя
     *
     * @param currentUser текущий пользователь
     * @return количество непрочитанных уведомлений
     */
    @Override
    public Long getUnreadNotificationsCount(User currentUser) {
        return notificationRepository.countByUserActionAndStatus(currentUser, NotificationStatus.UNREAD);
    }

    /**
     * Помечает уведомление как прочитанное
     *
     * @param id идентификатор уведомления
     * @param currentUser текущий пользователь
     * @return ответ с обновленным уведомлением
     */
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

    /**
     * Помечает все уведомления пользователя как прочитанные
     *
     * @param currentUser текущий пользователь
     */
    @Transactional
    @Override
    public void markAllAsRead(User currentUser) {
        notificationRepository.markAllAsRead(currentUser.getId(), NotificationStatus.READ);
        log.debug("All notifications marked as read for user: {}", currentUser.getUsername());
    }

    /**
     * Удаляет уведомление (помечает как удаленное)
     *
     * @param notificationId идентификатор уведомления
     * @param currentUser текущий пользователь
     */
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

    /**
     * Очищает удаленные уведомления пользователя
     *
     * @param currentUser текущий пользователь
     */
    @Transactional
    @Override
    public void clearDeletedNotifications(User currentUser) {
        notificationRepository.deleteAllDeletedByUser(currentUser);
        log.debug("Deleted notifications cleared for user: {}", currentUser.getUsername());
    }
}