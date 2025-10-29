package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.FriendshipStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.PostRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.RelationshipRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.NewsFeedCacheService;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsFeedCacheServiceImpl implements NewsFeedCacheService {
    private final CacheManager cacheManager;
    private final PostRepository postRepository;
    private final RelationshipRepository relationshipRepository;

    private static final String NEWS_FEED_CACHE = "newsFeed";

    public void evictNewsFeedCacheForUser(Long userId) {
        try {
            var cache = cacheManager.getCache(NEWS_FEED_CACHE);
            if (cache != null) {
                String cacheKey = "user:" + userId;
                cache.evict(cacheKey);
                log.debug("Кеш ленты новостей очищен для пользователя id={}", userId);
            }
        } catch (Exception e) {
            log.error("Ошибка при очистке кеша для пользователя id={}: {}", userId, e.getMessage());
        }
    }

    public void evictNewsFeedCacheForUsers(Iterable<Long> userIds) {
        for (Long userId : userIds) {
            evictNewsFeedCacheForUser(userId);
        }
    }

    public void evictCachePostForFriends(Long postId) {
        try {
            Optional<Post> postOpt = postRepository.findById(postId);
            if (postOpt.isEmpty()) {
                log.warn("Пост с id={} не найден при очистке кеша", postId);
                return;
            }

            Post post = postOpt.get();
            User ownerPost = post.getUser();
            Set<Long> friendsOwnerPost = relationshipRepository.findFriendIdsByUserId(
                    ownerPost.getId(), FriendshipStatus.ACCEPTED);

            evictNewsFeedCacheForUser(ownerPost.getId());
            evictNewsFeedCacheForUsers(friendsOwnerPost);

        } catch (Exception e) {
            log.error("Ошибка при инвалидации кеша для поста {}: {}", postId, e.getMessage());
        }
    }
}