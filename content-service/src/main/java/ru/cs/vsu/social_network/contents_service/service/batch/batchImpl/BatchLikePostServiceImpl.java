package ru.cs.vsu.social_network.contents_service.service.batch.batchImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.cs.vsu.social_network.contents_service.dto.response.content.LikePostResponse;
import ru.cs.vsu.social_network.contents_service.entity.LikePost;
import ru.cs.vsu.social_network.contents_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.contents_service.provider.LikePostEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.LikePostRepository;
import ru.cs.vsu.social_network.contents_service.service.batch.BatchLikePostService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для пакетных операций с лайками.
 * Обеспечивает эффективное получение лайков для множества постов.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchLikePostServiceImpl implements BatchLikePostService {

    private final LikePostRepository likePostRepository;
    private final LikePostEntityProvider likePostEntityProvider;
    private final EntityMapper entityMapper;

    private static final int MAX_BATCH_SIZE = 1000;

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

        final Map<UUID, Long> result =
                likePostEntityProvider.getLikesCountsForPosts(batchPostIds);

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

        final int effectiveLimit = Math.max(1, likesLimit);
        final List<LikePost> allLikes = likePostRepository
                .findRecentLikesForPosts(batchPostIds, effectiveLimit);

        final Map<UUID, List<LikePostResponse>> result = new HashMap<>();
        batchPostIds.forEach(postId -> result.put(postId, new ArrayList<>()));

        for (LikePost like : allLikes) {
            if (like.getPost() != null) {
                UUID postId = like.getPost().getId();
                if (result.containsKey(postId)) {
                    List<LikePostResponse> postLikes = result.get(postId);
                    if (postLikes.size() < effectiveLimit) {
                        LikePostResponse response = entityMapper.map(like, LikePostResponse.class);
                        postLikes.add(response);
                    }
                }
            }
        }

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

        final List<LikePost> likes = likePostRepository
                .findByPostIdOrderByCreatedAtDesc(postId, PageRequest.of(0, limit));

        final List<LikePostResponse> likeResponses = likes.stream()
                .map(like -> entityMapper.map(like, LikePostResponse.class))
                .collect(Collectors.toList());

        log.debug("BATCH_LIKE_SERVICE_ПОЛУЧЕНИЕ_ЛАЙКОВ_ДЛЯ_ПОСТА_УСПЕХ: " +
                "для поста {} найдено {} лайков", postId, likeResponses.size());
        return likeResponses;
    }
}