package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.common.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.notification.NotificationResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.common.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Notification;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.NotificationStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.NotificationRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.notification.NotificationService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.NotificationValidator;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging.CentralLogger;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final EntityMapper entityMapper;
    private final NotificationValidator notificationValidator;
    private final EntityUtils entityUtils;
    private final CentralLogger centralLogger;

    /**
     * Получает уведомления пользователя с пагинацией
     *
     * @param currentUser текущий пользователь
     * @param pageRequest параметры пагинации
     * @return страница с уведомлениями пользователя
     */
    @Override
    public PageResponse<NotificationResponse> getUserNotifications(User currentUser, PageRequest pageRequest) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", currentUser.getId());
        context.put("page", pageRequest.getPageNumber());
        context.put("size", pageRequest.getSize());

        centralLogger.logInfo("УВЕДОМЛЕНИЯ_ПОЛЬЗОВАТЕЛЯ_ПОЛУЧЕНИЕ",
                "Получение уведомлений пользователя", context);

        try {
            Page<Notification> notificationPage =
                    notificationRepository.findByUserAction(currentUser, pageRequest.toPageable());

            Map<String, Object> resultContext = new HashMap<>(context);
            resultContext.put("totalNotifications", notificationPage.getTotalElements());
            resultContext.put("currentPageNotifications", notificationPage.getContent().size());

            centralLogger.logInfo("УВЕДОМЛЕНИЯ_ПОЛЬЗОВАТЕЛЯ_ПОЛУЧЕНЫ",
                    "Уведомления пользователя успешно получены", resultContext);

            return PageResponse.of(notificationPage.map
                    (notification -> entityMapper.map(notification, NotificationResponse.class)));
        } catch (Exception e) {
            centralLogger.logError("УВЕДОМЛЕНИЯ_ПОЛЬЗОВАТЕЛЯ_ОШИБКА_ПОЛУЧЕНИЯ",
                    "Ошибка при получении уведомлений пользователя", context, e);
            throw e;
        }
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
        Map<String, Object> context = new HashMap<>();
        context.put("notificationId", id);
        context.put("userId", currentUser.getId());

        centralLogger.logInfo("УВЕДОМЛЕНИЕ_ПО_ID_ПОЛУЧЕНИЕ",
                "Получение уведомления по идентификатору", context);

        try {
            notificationValidator.validateNotificationAccess(id, currentUser);

            Notification notification = entityUtils.getNotification(id);

            centralLogger.logInfo("УВЕДОМЛЕНИЕ_ПО_ID_ПОЛУЧЕНО",
                    "Уведомление по идентификатору успешно получено", context);

            return entityMapper.map(notification, NotificationResponse.class);
        } catch (Exception e) {
            centralLogger.logError("УВЕДОМЛЕНИЕ_ПО_ID_ОШИБКА_ПОЛУЧЕНИЯ",
                    "Ошибка при получении уведомления по идентификатору", context, e);
            throw e;
        }
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
        Map<String, Object> context = new HashMap<>();
        context.put("userId", currentUser.getId());
        context.put("page", pageRequest.getPageNumber());
        context.put("size", pageRequest.getSize());
        context.put("status", NotificationStatus.UNREAD);

        centralLogger.logInfo("НЕПРОЧИТАННЫЕ_УВЕДОМЛЕНИЯ_ПОЛУЧЕНИЕ",
                "Получение непрочитанных уведомлений", context);

        try {
            Page<Notification> notificationPage = notificationRepository
                    .findByUserActionAndStatus(currentUser, NotificationStatus.UNREAD, pageRequest.toPageable());

            Map<String, Object> resultContext = new HashMap<>(context);
            resultContext.put("totalUnreadNotifications", notificationPage.getTotalElements());
            resultContext.put("currentPageUnreadNotifications", notificationPage.getContent().size());

            centralLogger.logInfo("НЕПРОЧИТАННЫЕ_УВЕДОМЛЕНИЯ_ПОЛУЧЕНЫ",
                    "Непрочитанные уведомления успешно получены", resultContext);

            return PageResponse.of(notificationPage.map
                    (notification -> entityMapper.map(notification, NotificationResponse.class)));
        } catch (Exception e) {
            centralLogger.logError("НЕПРОЧИТАННЫЕ_УВЕДОМЛЕНИЯ_ОШИБКА_ПОЛУЧЕНИЯ",
                    "Ошибка при получении непрочитанных уведомлений", context, e);
            throw e;
        }
    }

    /**
     * Получает количество непрочитанных уведомлений пользователя
     *
     * @param currentUser текущий пользователь
     * @return количество непрочитанных уведомлений
     */
    @Override
    public Long getUnreadNotificationsCount(User currentUser) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", currentUser.getId());

        centralLogger.logInfo("КОЛИЧЕСТВО_НЕПРОЧИТАННЫХ_УВЕДОМЛЕНИЙ_ПОЛУЧЕНИЕ",
                "Получение количества непрочитанных уведомлений", context);

        try {
            Long count = notificationRepository.countByUserActionAndStatus(currentUser, NotificationStatus.UNREAD);

            Map<String, Object> resultContext = new HashMap<>(context);
            resultContext.put("unreadCount", count);

            centralLogger.logInfo("КОЛИЧЕСТВО_НЕПРОЧИТАННЫХ_УВЕДОМЛЕНИЙ_ПОЛУЧЕНО",
                    "Количество непрочитанных уведомлений успешно получено", resultContext);

            return count;
        } catch (Exception e) {
            centralLogger.logError("КОЛИЧЕСТВО_НЕПРОЧИТАННЫХ_УВЕДОМЛЕНИЙ_ОШИБКА_ПОЛУЧЕНИЯ",
                    "Ошибка при получении количества непрочитанных уведомлений", context, e);
            throw e;
        }
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
        Map<String, Object> context = new HashMap<>();
        context.put("notificationId", id);
        context.put("userId", currentUser.getId());

        centralLogger.logInfo("УВЕДОМЛЕНИЕ_ПОМЕТКА_ПРОЧИТАНО",
                "Пометка уведомления как прочитанного", context);

        try {
            notificationValidator.validateNotificationAccess(id, currentUser);

            Notification notification = entityUtils.getNotification(id);

            notification.setStatus(NotificationStatus.READ);
            notification.setUpdatedAt(LocalDateTime.now());

            Notification updated = notificationRepository.save(notification);

            centralLogger.logInfo("УВЕДОМЛЕНИЕ_ПОМЕЧЕНО_ПРОЧИТАНО",
                    "Уведомление помечено как прочитанное", context);

            return entityMapper.map(updated, NotificationResponse.class);
        } catch (Exception e) {
            centralLogger.logError("УВЕДОМЛЕНИЕ_ОШИБКА_ПОМЕТКИ_ПРОЧИТАНО",
                    "Ошибка при пометке уведомления как прочитанного", context, e);
            throw e;
        }
    }

    /**
     * Помечает все уведомления пользователя как прочитанные
     *
     * @param currentUser текущий пользователь
     */
    @Transactional
    @Override
    public void markAllAsRead(User currentUser) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", currentUser.getId());

        centralLogger.logInfo("ВСЕ_УВЕДОМЛЕНИЯ_ПОМЕТКА_ПРОЧИТАНЫ",
                "Пометка всех уведомлений как прочитанных", context);

        try {
            notificationRepository.markAllAsRead(currentUser.getId(), NotificationStatus.READ);

            centralLogger.logInfo("ВСЕ_УВЕДОМЛЕНИЯ_ПОМЕЧЕНЫ_ПРОЧИТАНЫ",
                    "Все уведомления помечены как прочитанные", context);
        } catch (Exception e) {
            centralLogger.logError("ВСЕ_УВЕДОМЛЕНИЯ_ОШИБКА_ПОМЕТКИ_ПРОЧИТАНЫ",
                    "Ошибка при пометке всех уведомлений как прочитанных", context, e);
            throw e;
        }
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
        Map<String, Object> context = new HashMap<>();
        context.put("notificationId", notificationId);
        context.put("userId", currentUser.getId());

        centralLogger.logInfo("УВЕДОМЛЕНИЕ_УДАЛЕНИЕ",
                "Удаление уведомления", context);

        try {
            Notification notification = entityUtils.getNotification(notificationId);
            User user = notification.getUserAction();

            notificationValidator.validateUserNotificationsAccess(user, currentUser);

            notification.setStatus(NotificationStatus.DELETED);
            notification.setUpdatedAt(LocalDateTime.now());
            notificationRepository.save(notification);

            centralLogger.logInfo("УВЕДОМЛЕНИЕ_УДАЛЕНО",
                    "Уведомление успешно удалено", context);
        } catch (Exception e) {
            centralLogger.logError("УВЕДОМЛЕНИЕ_ОШИБКА_УДАЛЕНИЯ",
                    "Ошибка при удалении уведомления", context, e);
            throw e;
        }
    }

    /**
     * Очищает удаленные уведомления пользователя
     *
     * @param currentUser текущий пользователь
     */
    @Transactional
    @Override
    public void clearDeletedNotifications(User currentUser) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", currentUser.getId());

        centralLogger.logInfo("УДАЛЕННЫЕ_УВЕДОМЛЕНИЯ_ОЧИСТКА",
                "Очистка удаленных уведомлений", context);

        try {
            notificationRepository.deleteAllDeletedByUser(currentUser);

            centralLogger.logInfo("УДАЛЕННЫЕ_УВЕДОМЛЕНИЯ_ОЧИЩЕНЫ",
                    "Удаленные уведомления успешно очищены", context);
        } catch (Exception e) {
            centralLogger.logError("УДАЛЕННЫЕ_УВЕДОМЛЕНИЯ_ОШИБКА_ОЧИСТКИ",
                    "Ошибка при очистке удаленных уведомлений", context, e);
            throw e;
        }
    }
}