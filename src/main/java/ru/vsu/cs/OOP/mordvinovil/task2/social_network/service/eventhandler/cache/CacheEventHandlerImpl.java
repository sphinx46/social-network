package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.eventhandler.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.cache.GenericCacheEvent;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.NewsFeedCacheService;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheEventHandlerImpl implements CacheEventHandler {
    private final NewsFeedCacheService cacheService;

    @Async
    @EventListener
    public void handleCacheEvent(GenericCacheEvent event) {
        try {
            Long postId = (Long) event.getData("postId");
            if (postId != null) {
                cacheService.evictCachePostForFriends(postId);
                log.debug("Обработано событие для поста id={}, тип события: {}",
                        postId, event.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.error("Ошибка обработки события: {}", event.getAdditionalData(), e);
        }
    }
}
