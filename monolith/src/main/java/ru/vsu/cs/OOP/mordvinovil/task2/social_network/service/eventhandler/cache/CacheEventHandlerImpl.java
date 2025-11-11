package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.eventhandler.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.cache.GenericCacheEvent;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging.CentralLogger;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.feed.NewsFeedCacheService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheEventHandlerImpl implements CacheEventHandler {
    private final NewsFeedCacheService cacheService;
    private final CentralLogger centralLogger;

    /**
     * Обрабатывает событие кеша
     *
     * @param event событие кеша
     */
    @Async
    @EventListener
    public void handleCacheEvent(GenericCacheEvent event) {
        Map<String, Object> context = new HashMap<>();
        context.put("data", event.getAdditionalData());
        context.put("class", event.getClass());

        try {
            Long postId = (Long) event.getData("postId");
            if (postId != null) {
                cacheService.evictCachePostForFriends(postId);
                centralLogger.logInfo("ИЗМЕНЕНИЕ_КЕША_УСПЕХ",
                        "Кеш успешно обновлён", context);
            }
        } catch (Exception e) {
            centralLogger.logError("ИЗМЕНЕНИЕ_КЕША_ОШИБКА",
                    "Ошибка при обновлении кеша", context, e);
        }
    }
}