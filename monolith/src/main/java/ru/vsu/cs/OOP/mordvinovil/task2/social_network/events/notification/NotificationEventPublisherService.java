package ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.NotificationType;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.factory.notification.DefaultNotificationEventFactory;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventPublisherService {
    private final ApplicationEventPublisher eventPublisher;
    private final DefaultNotificationEventFactory eventFactory;

    /**
     * Публикует событие запроса на дружбу
     *
     * @param source источник события
     * @param targetUserId идентификатор целевого пользователя
     * @param requesterId идентификатор пользователя, отправившего запрос
     */
    public void publishFriendRequest(Object source, Long targetUserId, Long requesterId) {
        publishEvent(eventFactory.createFriendRequestEvent(source, targetUserId, requesterId));
    }

    /**
     * Публикует событие получения сообщения
     *
     * @param source источник события
     * @param targetUserId идентификатор целевого пользователя
     * @param senderId идентификатор отправителя сообщения
     * @param messagePreview превью сообщения
     */
    public void publishMessageReceived(Object source, Long targetUserId, Long senderId, String messagePreview) {
        publishEvent(eventFactory.createMessageReceivedEvent(source, targetUserId, senderId, messagePreview));
    }

    /**
     * Публикует событие удаления сообщения
     *
     * @param source источник события
     * @param targetUserId идентификатор целевого пользователя
     * @param deleterId идентификатор пользователя, удалившего сообщение
     */
    public void publishMessageDeleted(Object source, Long targetUserId, Long deleterId) {
        publishEvent(eventFactory.createMessageDeletedEvent(source, targetUserId, deleterId));
    }

    /**
     * Публикует событие лайка на пост
     *
     * @param source источник события
     * @param targetUserId идентификатор целевого пользователя
     * @param postId идентификатор поста
     * @param likerId идентификатор пользователя, поставившего лайк
     */
    public void publishPostLiked(Object source, Long targetUserId, Long postId, Long likerId) {
        publishEvent(eventFactory.createPostLikedEvent(source, targetUserId, postId, likerId));
    }

    /**
     * Публикует событие лайка на комментарий
     *
     * @param source источник события
     * @param targetUserId идентификатор целевого пользователя
     * @param postId идентификатор поста
     * @param likerId идентификатор пользователя, поставившего лайк
     */
    public void publishCommentLiked(Object source, Long targetUserId, Long postId, Long likerId) {
        publishEvent(eventFactory.createCommentLikedEvent(source, targetUserId, postId, likerId));
    }

    /**
     * Публикует событие добавления комментария
     *
     * @param source источник события
     * @param targetUserId идентификатор целевого пользователя
     * @param postId идентификатор поста
     * @param commenterId идентификатор пользователя, оставившего комментарий
     */
    public void publishCommentAdded(Object source, Long targetUserId, Long postId, Long commenterId) {
        publishEvent(eventFactory.createCommentAddedEvent(source, targetUserId, postId, commenterId));
    }

    /**
     * Публикует событие принятия запроса на дружбу
     *
     * @param source источник события
     * @param targetUserId идентификатор целевого пользователя
     * @param acceptorId идентификатор пользователя, принявшего запрос
     */
    public void publishFriendRequestAccepted(Object source, Long targetUserId, Long acceptorId) {
        publishEvent(eventFactory.createFriendRequestAcceptedEvent(source, targetUserId, acceptorId));
    }

    /**
     * Публикует кастомное событие уведомления
     *
     * @param source источник события
     * @param targetUserId идентификатор целевого пользователя
     * @param type тип уведомления
     * @param data дополнительные данные
     */
    public void publishCustomEvent(Object source, Long targetUserId, NotificationType type, Map<String, Object> data) {
        publishEvent(eventFactory.createCustomEvent(source, targetUserId, type, data));
    }

    /**
     * Публикует событие уведомления
     *
     * @param event событие уведомления
     */
    private void publishEvent(GenericNotificationEvent event) {
        try {
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            log.error("Failed to publish event: {}", event.getNotificationType(), e);
        }
    }
}