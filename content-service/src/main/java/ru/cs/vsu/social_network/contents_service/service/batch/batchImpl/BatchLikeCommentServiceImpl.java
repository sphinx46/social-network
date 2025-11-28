package ru.cs.vsu.social_network.contents_service.service.batch.batchImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.cs.vsu.social_network.contents_service.dto.response.content.LikeCommentResponse;
import ru.cs.vsu.social_network.contents_service.entity.LikeComment;
import ru.cs.vsu.social_network.contents_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.contents_service.provider.LikeCommentEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.LikeCommentRepository;
import ru.cs.vsu.social_network.contents_service.service.batch.BatchLikeCommentService;

import java.util.List;
import java.util.Map;
import java.util.UUID;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<UUID, Long> getLikesCountsForComments(final List<UUID> commentIds) {
        log.debug("BATCH_LIKE_COMMENT_SERVICE_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_ЛАЙКОВ_НАЧАЛО: " +
                "для {} комментариев", commentIds.size());

        final Map<UUID, Long> result = commentIds.stream()
                .collect(Collectors.toMap(
                        commentId -> commentId,
                        likeCommentEntityProvider::getLikesCountByComment
                ));

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

        final Map<UUID, List<LikeCommentResponse>> result = commentIds.stream()
                .collect(Collectors.toMap(
                        commentId -> commentId,
                        commentId -> getLikesForComment(commentId, likesLimit)
                ));

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