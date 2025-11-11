package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.feed.NewsFeedService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.feed.CachingNewsFeedServiceImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.feed.NewsFeedServiceImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.CacheMode;


@Component
@RequiredArgsConstructor
public class NewsFeedServiceFactory {
    private final CachingNewsFeedServiceImpl cachingService;
    private final NewsFeedServiceImpl notCachingService;

    public NewsFeedService getService(CacheMode cacheMode) {
        return switch (cacheMode) {
            case CACHE -> cachingService;
            case NONE_CACHE -> notCachingService;
        };
    }
}