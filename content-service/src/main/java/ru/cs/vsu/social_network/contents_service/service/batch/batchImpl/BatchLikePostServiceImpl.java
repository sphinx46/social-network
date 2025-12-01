package ru.cs.vsu.social_network.contents_service.service.batch.batchImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cs.vsu.social_network.contents_service.dto.response.content.LikePostResponse;
import ru.cs.vsu.social_network.contents_service.entity.LikePost;
import ru.cs.vsu.social_network.contents_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.contents_service.provider.LikePostEntityProvider;
import ru.cs.vsu.social_network.contents_service.service.batch.BatchLikePostService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для пакетных операций с лайками.
 * Обеспечивает эффективное получение лайков для множества постов.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BatchLikePostServiceImpl implements BatchLikePostService {

    private final LikePostEntityProvider likePostEntityProvider;
    private final EntityMapper entityMapper;

    private static final int MAX_BATCH_SIZE = 1000;
    private static final int BATCH_QUERY_SIZE = 100;
    private static final int DEFAULT_LIKES_LIMIT = 50;

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<UUID, Long> getLikesCountsForPosts(final List<UUID> postIds) {
        log.debug("BATCH_LIKE_SERVICE_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_ЛАЙКОВ_НАЧАЛО: " +
                "для {} постов", postIds.size());

        if (postIds.isEmpty()) {
            log.debug("BATCH_LIKE_SERVICE_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_ЛАЙКОВ_ПУСТОЙ_СПИСОК");
            return Collections.emptyMap();
        }

        final List<UUID> batchPostIds = postIds.size() > MAX_BATCH_SIZE ?
                postIds.subList(0, MAX_BATCH_SIZE) : postIds;

        final Map<UUID, Long> result = new ConcurrentHashMap<>();

        for (int i = 0; i < batchPostIds.size(); i += BATCH_QUERY_SIZE) {
            List<UUID> subList = batchPostIds.subList(i, Math.min(i + BATCH_QUERY_SIZE, batchPostIds.size()));
            Map<UUID, Long> batchResult = likePostEntityProvider.getLikesCountsForPosts(subList);
            result.putAll(batchResult);
        }

        batchPostIds.forEach(postId -> result.putIfAbsent(postId, 0L));

        log.debug("BATCH_LIKE_SERVICE_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_ЛАЙКОВ_УСПЕХ: " +
                "получено количество лайков для {} постов", result.size());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<UUID, List<LikePostResponse>> getLikesForPosts(final List<UUID> postIds,
                                                              final int likesLimit) {
        log.debug("BATCH_LIKE_SERVICE_ПОЛУЧЕНИЕ_ЛАЙКОВ_ДЛЯ_ПОСТОВ_НАЧАЛО: " +
                "для {} постов с лимитом {}", postIds.size(), likesLimit);

        if (postIds.isEmpty()) {
            log.debug("BATCH_LIKE_SERVICE_ПОЛУЧЕНИЕ_ЛАЙКОВ_ДЛЯ_ПОСТОВ_ПУСТОЙ_СПИСОК");
            return Collections.emptyMap();
        }

        final List<UUID> batchPostIds = postIds.size() > MAX_BATCH_SIZE ?
                postIds.subList(0, MAX_BATCH_SIZE) : postIds;

        final int effectiveLimit = Math.max(1, Math.min(likesLimit, DEFAULT_LIKES_LIMIT));

        final List<LikePost> allLikes = likePostEntityProvider.getLikesWithPosts(batchPostIds, effectiveLimit);

        final Map<UUID, List<LikePostResponse>> result = new ConcurrentHashMap<>();
        batchPostIds.forEach(postId -> result.put(postId, new ArrayList<>()));

        Map<UUID, List<LikePost>> likesByPostId = allLikes.stream()
                .filter(like -> like.getPost() != null)
                .collect(Collectors.groupingBy(like -> like.getPost().getId()));

        likesByPostId.forEach((postId, likes) -> {
            List<LikePostResponse> responses = likes.stream()
                    .limit(effectiveLimit)
                    .map(like -> entityMapper.map(like, LikePostResponse.class))
                    .collect(Collectors.toList());
            result.put(postId, responses);
        });

        log.debug("BATCH_LIKE_SERVICE_ПОЛУЧЕНИЕ_ЛАЙКОВ_ДЛЯ_ПОСТОВ_УСПЕХ: " +
                "получены лайки для {} постов", result.size());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LikePostResponse> getLikesForPost(final UUID postId,
                                                  final int limit) {
        log.debug("BATCH_LIKE_SERVICE_ПОЛУЧЕНИЕ_ЛАЙКОВ_ДЛЯ_ПОСТА_НАЧАЛО: " +
                "для поста {} с лимитом {}", postId, limit);

        final int effectiveLimit = Math.max(1, Math.min(limit, 100));
        final List<LikePost> likes = likePostEntityProvider.getRecentLikesForPost(postId, effectiveLimit);

        final List<LikePostResponse> likeResponses = likes.stream()
                .map(like -> entityMapper.map(like, LikePostResponse.class))
                .collect(Collectors.toList());

        log.debug("BATCH_LIKE_SERVICE_ПОЛУЧЕНИЕ_ЛАЙКОВ_ДЛЯ_ПОСТА_УСПЕХ: " +
                "для поста {} найдено {} лайков", postId, likeResponses.size());
        return likeResponses;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<UUID, Boolean> getLikesStatusForPosts(UUID ownerId, List<UUID> postIds) {
        log.debug("BATCH_LIKE_SERVICE_ПОЛУЧЕНИЕ_СТАТУСА_ЛАЙКОВ_НАЧАЛО:" +
                " для пользователя {} и {} постов", ownerId, postIds.size());

        if (postIds.isEmpty()) {
            log.debug("BATCH_LIKE_SERVICE_ПОЛУЧЕНИЕ_СТАТУСА_ЛАЙКОВ_ПУСТОЙ_СПИСОК");
            return Collections.emptyMap();
        }

        final List<UUID> batchPostIds = postIds.size() > MAX_BATCH_SIZE ?
                postIds.subList(0, MAX_BATCH_SIZE) : postIds;

        final Map<UUID, Boolean> result = new ConcurrentHashMap<>();
        batchPostIds.forEach(postId -> result.put(postId, false));

        final List<LikePost> userLikes =
                likePostEntityProvider.getLikesByOwnerAndPosts(ownerId, batchPostIds);

        userLikes.stream()
                .filter(like -> like.getPost() != null)
                .forEach(like -> result.put(like.getPost().getId(), true));

        log.debug("BATCH_LIKE_SERVICE_ПОЛУЧЕНИЕ_СТАТУСА_ЛАЙКОВ_УСПЕХ: " +
                "получен статус лайков для {} постов", result.size());
        return result;
    }
}