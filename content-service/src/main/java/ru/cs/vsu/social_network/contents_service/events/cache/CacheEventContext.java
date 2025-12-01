package ru.cs.vsu.social_network.contents_service.events.cache;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * Контекст события инвалидации кэша.
 * Содержит всю необходимую информацию для публикации и обработки события.
 */
@Getter
@Builder
public class CacheEventContext {
    private final Object source;
    private final Object target;
    private final UUID postId;
    private final UUID commentId;
    private final UUID likeId;
    private final UUID userId;
    private final CacheEventType eventType;
}