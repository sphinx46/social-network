package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.messaging;

public interface MessageCacheService {
    void evictConversationCache(Long userId1, Long userId2);
    void evictConversationCacheForUser(Long userId);
}
