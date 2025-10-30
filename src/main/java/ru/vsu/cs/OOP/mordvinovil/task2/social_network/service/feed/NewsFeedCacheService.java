package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.feed;

public interface NewsFeedCacheService {
    void evictNewsFeedCacheForUser(Long userId);
    void evictNewsFeedCacheForUsers(Iterable<Long> userIds);
    void evictCachePostForFriends(Long postId);
}
