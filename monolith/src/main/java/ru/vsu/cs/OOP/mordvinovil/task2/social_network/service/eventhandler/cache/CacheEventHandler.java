package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.eventhandler.cache;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.cache.GenericCacheEvent;

public interface CacheEventHandler {
    void handleCacheEvent(GenericCacheEvent event);
}





