package ru.cs.vsu.social_network.contents_service.service.batch.batchImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.cs.vsu.social_network.contents_service.dto.response.content.LikeCommentResponse;
import ru.cs.vsu.social_network.contents_service.entity.LikeComment;
import ru.cs.vsu.social_network.contents_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.contents_service.provider.LikeCommentEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.LikeCommentRepository;
import ru.cs.vsu.social_network.contents_service.service.batch.BatchLikeCommentService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для пакетных операций с лайками комментариев.
 * Обеспечивает эффективное получение лайков для множества комментариев.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchLikeCommentServiceImpl implements BatchLikeCommentService {

    private final LikeCommentRepository likeCommentRepository;
    private final LikeCommentEntityProvider likeCommentEntityProvider;
    private final EntityMapper entityMapper;

    private static final int MAX_BATCH_SIZE = 1000;

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

        final Map<UUID, Long> result =
                likeCommentEntityProvider.getLikesCountsForComments(batchCommentIds);

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

        final Pageable pageable = PageRequest.of(0, likesLimit);
        final List<LikeComment> allLikes = likeCommentRepository
                .findRecentLikesForComments(batchCommentIds, pageable);

        final Map<UUID, List<LikeCommentResponse>> result = new HashMap<>();

        batchCommentIds.forEach(commentId -> result.put(commentId, new ArrayList<>()));

        for (LikeComment like : allLikes) {
            UUID commentId = like.getComment() != null ? like.getComment().getId() : null;
            if (commentId != null && result.containsKey(commentId)) {
                LikeCommentResponse response = entityMapper.map(like, LikeCommentResponse.class);
                result.get(commentId).add(response);
            }
        }

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

        final List<LikeComment> likes = likeCommentRepository
                .findAllByCommentId(commentId, PageRequest.of(0, limit))
                .getContent();

        final List<LikeCommentResponse> likeResponses = likes.stream()
                .map(like -> entityMapper.map(like, LikeCommentResponse.class))
                .collect(Collectors.toList());

        log.debug("BATCH_LIKE_COMMENT_SERVICE_ПОЛУЧЕНИЕ_ЛАЙКОВ_ДЛЯ_КОММЕНТАРИЯ_УСПЕХ: " +
                "для комментария {} найдено {} лайков", commentId, likeResponses.size());
        return likeResponses;
    }
}