package ru.cs.vsu.social_network.contents_service.service.batch.batchImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cs.vsu.social_network.contents_service.dto.response.content.LikeCommentResponse;
import ru.cs.vsu.social_network.contents_service.entity.LikeComment;
import ru.cs.vsu.social_network.contents_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.contents_service.provider.LikeCommentEntityProvider;
import ru.cs.vsu.social_network.contents_service.service.batch.BatchLikeCommentService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для пакетных операций с лайками комментариев.
 * Обеспечивает эффективное получение лайков для множества комментариев.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BatchLikeCommentServiceImpl implements BatchLikeCommentService {

    private final LikeCommentEntityProvider likeCommentEntityProvider;
    private final EntityMapper entityMapper;

    private static final int MAX_BATCH_SIZE = 1000;
    private static final int BATCH_QUERY_SIZE = 100;
    private static final int DEFAULT_LIKES_LIMIT = 50;

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<UUID, Long> getLikesCountsForComments(final List<UUID> commentIds) {
        log.debug("BATCH_LIKE_COMMENT_SERVICE_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_ЛАЙКОВ_НАЧАЛО: " +
                "для {} комментариев", commentIds.size());

        if (commentIds.isEmpty()) {
            log.debug("BATCH_LIKE_COMMENT_SERVICE_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_ЛАЙКОВ_ПУСТОЙ_СПИСОК");
            return Collections.emptyMap();
        }

        final List<UUID> batchCommentIds = commentIds.size() > MAX_BATCH_SIZE ?
                commentIds.subList(0, MAX_BATCH_SIZE) : commentIds;

        final Map<UUID, Long> result = new ConcurrentHashMap<>();

        for (int i = 0; i < batchCommentIds.size(); i += BATCH_QUERY_SIZE) {
            List<UUID> subList = batchCommentIds.subList(i, Math.min(i + BATCH_QUERY_SIZE, batchCommentIds.size()));
            Map<UUID, Long> batchResult = likeCommentEntityProvider.getLikesCountsForComments(subList);
            result.putAll(batchResult);
        }

        batchCommentIds.forEach(commentId -> result.putIfAbsent(commentId, 0L));

        log.debug("BATCH_LIKE_COMMENT_SERVICE_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_ЛАЙКОВ_УСПЕХ: " +
                "получено количество лайков для {} комментариев", result.size());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<UUID, List<LikeCommentResponse>> getLikesForComments(final List<UUID> commentIds,
                                                                    final int likesLimit) {
        log.debug("BATCH_LIKE_COMMENT_SERVICE_ПОЛУЧЕНИЕ_ЛАЙКОВ_ДЛЯ_КОММЕНТАРИЕВ_НАЧАЛО: " +
                "для {} комментариев с лимитом {}", commentIds.size(), likesLimit);

        if (commentIds.isEmpty()) {
            log.debug("BATCH_LIKE_COMMENT_SERVICE_ПОЛУЧЕНИЕ_ЛАЙКОВ_ДЛЯ_КОММЕНТАРИЕВ_ПУСТОЙ_СПИСОК");
            return Collections.emptyMap();
        }

        final List<UUID> batchCommentIds = commentIds.size() > MAX_BATCH_SIZE ?
                commentIds.subList(0, MAX_BATCH_SIZE) : commentIds;

        final int effectiveLimit = Math.max(1, Math.min(likesLimit, DEFAULT_LIKES_LIMIT));

        final List<LikeComment> allLikes =
                likeCommentEntityProvider.getRecentLikesForComments(batchCommentIds, effectiveLimit);

        final Map<UUID, List<LikeCommentResponse>> result = new ConcurrentHashMap<>();
        batchCommentIds.forEach(commentId -> result.put(commentId, new ArrayList<>()));

        Map<UUID, List<LikeComment>> likesByCommentId = allLikes.stream()
                .filter(like -> like.getComment() != null)
                .collect(Collectors.groupingBy(like -> like.getComment().getId()));

        likesByCommentId.forEach((commentId, likes) -> {
            List<LikeCommentResponse> responses = likes.stream()
                    .limit(effectiveLimit)
                    .map(like -> entityMapper.map(like, LikeCommentResponse.class))
                    .collect(Collectors.toList());
            result.put(commentId, responses);
        });

        log.debug("BATCH_LIKE_COMMENT_SERVICE_ПОЛУЧЕНИЕ_ЛАЙКОВ_ДЛЯ_КОММЕНТАРИЕВ_УСПЕХ: " +
                "получены лайки для {} комментариев", result.size());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LikeCommentResponse> getLikesForComment(final UUID commentId,
                                                        final int limit) {
        log.debug("BATCH_LIKE_COMMENT_SERVICE_ПОЛУЧЕНИЕ_ЛАЙКОВ_ДЛЯ_КОММЕНТАРИЯ_НАЧАЛО: " +
                "для комментария {} с лимитом {}", commentId, limit);

        final int effectiveLimit = Math.max(1, Math.min(limit, 100));
        final List<LikeComment> likes =
                likeCommentEntityProvider.getRecentLikesForComment(commentId, effectiveLimit);

        final List<LikeCommentResponse> likeResponses = likes.stream()
                .map(like -> entityMapper.map(like, LikeCommentResponse.class))
                .collect(Collectors.toList());

        log.debug("BATCH_LIKE_COMMENT_SERVICE_ПОЛУЧЕНИЕ_ЛАЙКОВ_ДЛЯ_КОММЕНТАРИЯ_УСПЕХ: " +
                "для комментария {} найдено {} лайков", commentId, likeResponses.size());
        return likeResponses;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LikeCommentResponse> getLikesWithComments(List<UUID> commentIds, int limit) {
        log.debug("BATCH_LIKE_COMMENT_SERVICE_ПОЛУЧЕНИЕ_ЛАЙКОВ_С_КОММЕНТАРИЯМИ_НАЧАЛО: " +
                "для {} комментариев", commentIds.size());

        if (commentIds.isEmpty()) {
            log.debug("BATCH_LIKE_COMMENT_SERVICE_ПОЛУЧЕНИЕ_ЛАЙКОВ_С_КОММЕНТАРИЯМИ_ПУСТОЙ_СПИСОК");
            return Collections.emptyList();
        }

        final int effectiveLimit = Math.max(1, Math.min(limit, DEFAULT_LIKES_LIMIT));
        final List<LikeComment> likes = likeCommentEntityProvider.getLikesWithComments(commentIds, effectiveLimit);

        final List<LikeCommentResponse> likeResponses = likes.stream()
                .map(like -> entityMapper.map(like, LikeCommentResponse.class))
                .collect(Collectors.toList());

        log.debug("BATCH_LIKE_COMMENT_SERVICE_ПОЛУЧЕНИЕ_ЛАЙКОВ_С_КОММЕНТАРИЯМИ_УСПЕХ: " +
                "получено {} лайков с комментариями", likeResponses.size());
        return likeResponses;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<UUID, Boolean> getLikesStatusForComments(UUID ownerId, List<UUID> commentIds) {
        log.debug("BATCH_LIKE_COMMENT_SERVICE_ПОЛУЧЕНИЕ_СТАТУСА_ЛАЙКОВ_НАЧАЛО: " +
                "для пользователя {} и {} комментариев", ownerId, commentIds.size());

        if (commentIds.isEmpty()) {
            log.debug("BATCH_LIKE_COMMENT_SERVICE_ПОЛУЧЕНИЕ_СТАТУСА_ЛАЙКОВ_ПУСТОЙ_СПИСОК");
            return Collections.emptyMap();
        }

        final List<UUID> batchCommentIds = commentIds.size() > MAX_BATCH_SIZE ?
                commentIds.subList(0, MAX_BATCH_SIZE) : commentIds;

        final Map<UUID, Boolean> result = new ConcurrentHashMap<>();
        batchCommentIds.forEach(commentId -> result.put(commentId, false));

        batchCommentIds.forEach(commentId -> {
            boolean exists = likeCommentEntityProvider.existsByOwnerIdAndCommentId(ownerId, commentId);
            result.put(commentId, exists);
        });

        log.debug("BATCH_LIKE_COMMENT_SERVICE_ПОЛУЧЕНИЕ_СТАТУСА_ЛАЙКОВ_УСПЕХ:" +
                " получен статус лайков для {} комментариев", result.size());
        return result;
    }
}
