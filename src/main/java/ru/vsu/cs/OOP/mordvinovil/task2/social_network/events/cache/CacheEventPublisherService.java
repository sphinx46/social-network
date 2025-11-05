package ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.factory.cache.DefaultCacheEventFactory;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheEventPublisherService {
    private final ApplicationEventPublisher eventPublisher;
    private final DefaultCacheEventFactory eventFactory;

    /**
     * Публикует событие лайка на пост
     *
     * @param source источник события
     * @param target целевой объект
     * @param postId идентификатор поста
     * @param likerId идентификатор пользователя, поставившего лайк
     * @param likeId идентификатор лайка
     */
    public void publishLikedPost(Object source, Object target, Long postId, Long likerId, Long likeId) {
        publishEvent(eventFactory.createLikedPostEvent(source, target, postId, likerId, likeId));
    }

    /**
     * Публикует событие лайка на комментарий
     *
     * @param source источник события
     * @param target целевой объект
     * @param commentId идентификатор комментария
     * @param likerId идентификатор пользователя, поставившего лайк
     * @param likeId идентификатор лайка
     */
    public void publishLikedComment(Object source, Object target, Long commentId, Long likerId, Long likeId) {
        publishEvent(eventFactory.createLikedPostEvent(source, target, commentId, likerId, likeId));
    }

    /**
     * Публикует событие удаления лайка
     *
     * @param source источник события
     * @param target целевой объект
     * @param postId идентификатор поста
     * @param likeId идентификатор лайка
     */
    public void publishLikeDeleted(Object source, Object target, Long postId, Long likeId) {
        publishEvent(eventFactory.createLikeDeletedEvent(source, target, postId, likeId));
    }

    /**
     * Публикует событие создания комментария
     *
     * @param source источник события
     * @param target целевой объект
     * @param postId идентификатор поста
     * @param commenterId идентификатор пользователя, оставившего комментарий
     * @param commentId идентификатор комментария
     */
    public void publishCommentCreated(Object source, Object target, Long postId, Long commenterId, Long commentId) {
        publishEvent(eventFactory.createCommentCreatedEvent(source, target, postId, commenterId, commentId));
    }

    /**
     * Публикует событие редактирования комментария
     *
     * @param source источник события
     * @param target целевой объект
     * @param postId идентификатор поста
     * @param commentId идентификатор комментария
     */
    public void publishCommentEdit(Object source, Object target, Long postId, Long commentId) {
        publishEvent(eventFactory.createCommentEditEvent(source, target, postId, commentId));
    }

    /**
     * Публикует событие удаления комментария
     *
     * @param source источник события
     * @param target целевой объект
     * @param postId идентификатор поста
     * @param commentId идентификатор комментария
     */
    public void publishCommentDeleted(Object source, Object target, Long postId, Long commentId) {
        publishEvent(eventFactory.createCommentDeletedEvent(source, target, postId, commentId));
    }

    /**
     * Публикует событие редактирования поста
     *
     * @param source источник события
     * @param target целевой объект
     * @param postId идентификатор поста
     */
    public void publishPostEdit(Object source, Object target, Long postId) {
        publishEvent(eventFactory.createPostEditEvent(source, target, postId));
    }

    /**
     * Публикует событие создания поста
     *
     * @param source источник события
     * @param target целевой объект
     * @param postId идентификатор поста
     */
    public void publishPostCreate(Object source, Object target, Long postId) {
        publishEvent(eventFactory.createPostEvent(source, target, postId));
    }

    /**
     * Публикует событие кеша
     *
     * @param event событие кеша
     */
    private void publishEvent(GenericCacheEvent event) {
        try {
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            log.error("Failed to publish event: {}", event.getAdditionalData(), e);
        }
    }
}