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

    public void publishLikedPost(Object source, Object target, Long postId, Long likerId, Long likeId) {
        publishEvent(eventFactory.createLikedPostEvent(source, target, postId, likerId, likeId));
    }

    public void publishLikeDeleted(Object source, Object target, Long postId, Long likeId) {
        publishEvent(eventFactory.createLikeDeletedEvent(source, target, postId, likeId));
    }

    public void publishCommentCreated(Object source, Object target, Long postId, Long commenterId, Long commentId) {
        publishEvent(eventFactory.createCommentCreatedEvent(source, target, postId, commenterId, commentId));
    }

    public void publishCommentEdit(Object source, Object target, Long postId, Long commentId) {
        publishEvent(eventFactory.createCommentEditEvent(source, target, postId, commentId));
    }

    public void publishCommentDeleted(Object source, Object target, Long postId, Long commentId) {
        publishEvent(eventFactory.createCommentDeletedEvent(source, target, postId, commentId));
    }

    public void publishPostEdit(Object source, Object target, Long postId) {
        publishEvent(eventFactory.createPostEditEvent(source, target, postId));
    }

    public void publishPostCreate(Object source, Object target, Long postId) {
        publishEvent(eventFactory.createPostEvent(source, target, postId));
    }

    private void publishEvent(GenericCacheEvent event) {
        try {
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            log.error("Failed to publish event: {}", event.getAdditionalData(), e);
        }
    }
}
