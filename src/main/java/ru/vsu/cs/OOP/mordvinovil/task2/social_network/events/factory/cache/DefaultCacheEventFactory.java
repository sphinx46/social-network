package ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.factory.cache;

import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.cache.GenericCacheEvent;

import java.util.Map;

@Component
public class DefaultCacheEventFactory extends AbstractCacheEventFactory {

    @Override
    public GenericCacheEvent createLikedPostEvent(Object source, Object target, Long postId, Long likerId, Long likeId) {
        Map<String, Object> data = Map.of("postId", postId,
                                        "likerId", likerId,
                                        "likeId", likeId);
        return createEvent(source, target, data);
    }

    @Override
    public GenericCacheEvent createLikeDeletedEvent(Object source, Object target, Long postId, Long likeId) {
        Map<String, Object> data = Map.of("postId", postId,
                "likeId", likeId);
        return createEvent(source, target, data);
    }

    @Override
    public GenericCacheEvent createCommentCreatedEvent(Object source, Object target, Long postId, Long commenterId, Long commentId) {
        Map<String, Object> data = Map.of("postId", postId,
                "commenterId", commenterId,
                "commentId", commentId);
        return createEvent(source, target, data);
    }

    @Override
    public GenericCacheEvent createCommentEditEvent(Object source, Object target, Long postId, Long commentId) {
        Map<String, Object> data = Map.of("postId", postId,
                "commentId", commentId);
        return createEvent(source, target, data);
    }

    @Override
    public GenericCacheEvent createCommentDeletedEvent(Object source, Object target, Long postId, Long commentId) {
        Map<String, Object> data = Map.of("postId", postId,
                "commentId", commentId);
        return createEvent(source, target, data);
    }

    @Override
    public GenericCacheEvent createPostEditEvent(Object source, Object target, Long postId) {
        Map<String, Object> data = Map.of("postId", postId);
        return createEvent(source, target, data);
    }
}
