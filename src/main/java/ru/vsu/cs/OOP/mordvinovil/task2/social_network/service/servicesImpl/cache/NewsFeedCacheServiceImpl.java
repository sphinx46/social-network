package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
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
    private final PostRepository postRepository;
    private final RelationshipRepository relationshipRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "newsFeed::user:";

    public void evictNewsFeedCacheForUser(Long userId) {
        try {
            String pattern = CACHE_KEY_PREFIX + userId + ":*";
            Set<String> keys = redisTemplate.keys(pattern);

            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Удалено {} кешей ленты новостей для пользователя id={}", keys.size(), userId);
            } else {
                log.debug("Кеши ленты новостей не найдены для пользователя id={}", userId);
            }
        } catch (Exception e) {
            log.error("Ошибка при очистке кеша для пользователя id={}: {}", userId, e.getMessage(), e);
        }
    }

    public void evictNewsFeedCacheForUsers(Iterable<Long> userIds) {
        int totalDeleted = 0;
        for (Long userId : userIds) {
            try {
                String pattern = CACHE_KEY_PREFIX + userId + ":*";
                Set<String> keys = redisTemplate.keys(pattern);

                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                    totalDeleted += keys.size();
                    log.debug("Удалено {} кешей для пользователя id={}", keys.size(), userId);
                }
            } catch (Exception e) {
                log.error("Ошибка при очистке кеша для пользователя id={}: {}", userId, e.getMessage());
            }
        }
        log.debug("Всего удалено {} кешей ленты новостей для {} пользователей", totalDeleted, ((Set<Long>) userIds).size());
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

            log.debug("Кеш ленты новостей инвалидирован для поста id={}. Затронуто {} пользователей",
                    postId, friendsOwnerPost.size() + 1);

        } catch (Exception e) {
            log.error("Ошибка при инвалидации кеша для поста {}: {}", postId, e.getMessage(), e);
        }
    }
}