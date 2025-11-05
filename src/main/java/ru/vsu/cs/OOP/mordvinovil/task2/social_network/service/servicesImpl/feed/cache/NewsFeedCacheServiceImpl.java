package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.feed.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.FriendshipStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.PostRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.RelationshipRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.feed.NewsFeedCacheService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging.CentralLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsFeedCacheServiceImpl implements NewsFeedCacheService {
    private final PostRepository postRepository;
    private final RelationshipRepository relationshipRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CentralLogger centralLogger;

    private static final String CACHE_KEY_PREFIX = "newsFeed::user:";

    /**
     * Очищает кеш ленты новостей для конкретного пользователя
     *
     * @param userId идентификатор пользователя
     */
    public void evictNewsFeedCacheForUser(Long userId) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);

        centralLogger.logInfo("КЕШ_ЛЕНТА_ОЧИСТКА_ПОЛЬЗОВАТЕЛЬ",
                "Очистка кеша ленты новостей для пользователя", context);

        try {
            String pattern = CACHE_KEY_PREFIX + userId + ":*";
            Set<String> keys = redisTemplate.keys(pattern);

            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);

                Map<String, Object> successContext = new HashMap<>(context);
                successContext.put("deletedKeysCount", keys.size());

                centralLogger.logInfo("КЕШ_ЛЕНТА_ОЧИЩЕН_ПОЛЬЗОВАТЕЛЬ",
                        "Кеш ленты новостей очищен для пользователя", successContext);
            } else {
                centralLogger.logInfo("КЕШ_ЛЕНТА_НЕ_НАЙДЕН_ПОЛЬЗОВАТЕЛЬ",
                        "Кеш ленты новостей не найден для пользователя", context);
            }
        } catch (Exception e) {
            centralLogger.logError("КЕШ_ЛЕНТА_ОШИБКА_ОЧИСТКИ_ПОЛЬЗОВАТЕЛЬ",
                    "Ошибка при очистке кеша ленты новостей для пользователя", context, e);
        }
    }

    /**
     * Очищает кеш ленты новостей для нескольких пользователей
     *
     * @param userIds идентификаторы пользователей
     */
    public void evictNewsFeedCacheForUsers(Iterable<Long> userIds) {
        Map<String, Object> context = new HashMap<>();
        context.put("userIdsCount", ((Set<Long>) userIds).size());

        centralLogger.logInfo("КЕШ_ЛЕНТА_ОЧИСТКА_ПОЛЬЗОВАТЕЛИ",
                "Очистка кеша ленты новостей для нескольких пользователей", context);

        int totalDeleted = 0;
        for (Long userId : userIds) {
            try {
                String pattern = CACHE_KEY_PREFIX + userId + ":*";
                Set<String> keys = redisTemplate.keys(pattern);

                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                    totalDeleted += keys.size();

                    Map<String, Object> userContext = new HashMap<>();
                    userContext.put("userId", userId);
                    userContext.put("deletedKeysCount", keys.size());

                    centralLogger.logInfo("КЕШ_ЛЕНТА_ОЧИЩЕН_ПОЛЬЗОВАТЕЛЬ_ИЗ_СПИСКА",
                            "Кеш ленты новостей очищен для пользователя из списка", userContext);
                }
            } catch (Exception e) {
                Map<String, Object> errorContext = new HashMap<>();
                errorContext.put("userId", userId);

                centralLogger.logError("КЕШ_ЛЕНТА_ОШИБКА_ОЧИСТКИ_ПОЛЬЗОВАТЕЛЬ_ИЗ_СПИСКА",
                        "Ошибка при очистке кеша ленты новостей для пользователя из списка", errorContext, e);
            }
        }

        Map<String, Object> resultContext = new HashMap<>(context);
        resultContext.put("totalDeletedKeys", totalDeleted);

        centralLogger.logInfo("КЕШ_ЛЕНТА_ОЧИЩЕН_ВСЕ_ПОЛЬЗОВАТЕЛИ",
                "Кеш ленты новостей очищен для всех пользователей из списка", resultContext);
    }

    /**
     * Очищает кеш ленты новостей для друзей автора поста
     *
     * @param postId идентификатор поста
     */
    public void evictCachePostForFriends(Long postId) {
        Map<String, Object> context = new HashMap<>();
        context.put("postId", postId);

        centralLogger.logInfo("КЕШ_ЛЕНТА_ОЧИСТКА_ПОСТ_ДРУЗЬЯ",
                "Очистка кеша ленты новостей для друзей автора поста", context);

        try {
            Optional<Post> postOpt = postRepository.findById(postId);
            if (postOpt.isEmpty()) {
                centralLogger.logInfo("КЕШ_ЛЕНТА_ПОСТ_НЕ_НАЙДЕН",
                        "Пост не найден при очистке кеша ленты новостей", context);
                return;
            }

            Post post = postOpt.get();
            User ownerPost = post.getUser();
            Set<Long> friendsOwnerPost = relationshipRepository.findFriendIdsByUserId(
                    ownerPost.getId(), FriendshipStatus.ACCEPTED);

            evictNewsFeedCacheForUser(ownerPost.getId());
            evictNewsFeedCacheForUsers(friendsOwnerPost);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("postOwnerId", ownerPost.getId());
            successContext.put("friendsCount", friendsOwnerPost.size());
            successContext.put("totalAffectedUsers", friendsOwnerPost.size() + 1);

            centralLogger.logInfo("КЕШ_ЛЕНТА_ОЧИЩЕН_ПОСТ_ДРУЗЬЯ",
                    "Кеш ленты новостей инвалидирован для поста и друзей автора", successContext);

        } catch (Exception e) {
            centralLogger.logError("КЕШ_ЛЕНТА_ОШИБКА_ОЧИСТКИ_ПОСТ_ДРУЗЬЯ",
                    "Ошибка при инвалидации кеша ленты новостей для поста и друзей автора", context, e);
        }
    }
}