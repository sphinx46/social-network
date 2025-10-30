package ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.factory.cache;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.cache.GenericCacheEvent;

public interface CacheEventFactory {
    GenericCacheEvent createLikedPostEvent(Object source, Object target, Long postId, Long likerId, Long likeId);
    GenericCacheEvent createLikedCommentEvent(Object source, Object target, Long commentId, Long likerId, Long likeId);
    GenericCacheEvent createLikeDeletedEvent(Object source, Object target, Long postId, Long likeId);
    GenericCacheEvent createCommentCreatedEvent(Object source, Object target, Long postId, Long commenterId, Long commentId);
    GenericCacheEvent createCommentEditEvent(Object source, Object target, Long postId, Long commentId);
    GenericCacheEvent createCommentDeletedEvent(Object source, Object target, Long postId, Long commentId);
    GenericCacheEvent createPostEditEvent(Object source, Object target, Long postId);
    GenericCacheEvent createPostEvent(Object source, Object target, Long postId);
}
