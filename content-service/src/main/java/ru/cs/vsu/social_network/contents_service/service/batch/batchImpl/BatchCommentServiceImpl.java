package ru.cs.vsu.social_network.contents_service.service.batch.batchImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.cs.vsu.social_network.contents_service.dto.response.content.CommentResponse;
import ru.cs.vsu.social_network.contents_service.entity.Comment;
import ru.cs.vsu.social_network.contents_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.contents_service.provider.CommentEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.CommentRepository;
import ru.cs.vsu.social_network.contents_service.service.batch.BatchCommentService;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для пакетных операций с комментариями.
 * Обеспечивает эффективное получение комментариев для множества постов.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchCommentServiceImpl implements BatchCommentService {

    private final CommentRepository commentRepository;
    private final CommentEntityProvider commentEntityProvider;
    private final EntityMapper entityMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<UUID, Long> getCommentsCountsForPosts(final List<UUID> postIds) {
        log.debug("BATCH_COMMENT_SERVICE_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_КОММЕНТАРИЕВ_НАЧАЛО: " +
                "для {} постов", postIds.size());

        final Map<UUID, Long> result = postIds.stream()
                .collect(Collectors.toMap(
                        postId -> postId,
                        commentEntityProvider::getCommentsCountByPost
                ));

        log.debug("BATCH_COMMENT_SERVICE_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_КОММЕНТАРИЕВ_УСПЕХ: " +
                "получено количество комментариев для {} постов", result.size());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<UUID, List<CommentResponse>> getCommentsForPosts(final List<UUID> postIds,
                                                                final int commentsLimit) {
        log.debug("BATCH_COMMENT_SERVICE_ПОЛУЧЕНИЕ_КОММЕНТАРИЕВ_ДЛЯ_ПОСТОВ_НАЧАЛО: " +
                "для {} постов с лимитом {}", postIds.size(), commentsLimit);

        final Map<UUID, List<CommentResponse>> result = postIds.stream()
                .collect(Collectors.toMap(
                        postId -> postId,
                        postId -> getCommentsForPost(postId, commentsLimit)
                ));

        log.debug("BATCH_COMMENT_SERVICE_ПОЛУЧЕНИЕ_КОММЕНТАРИЕВ_ДЛЯ_ПОСТОВ_УСПЕХ: " +
                "получены комментарии для {} постов", result.size());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CommentResponse> getCommentsForPost(final UUID postId,
                                                    final int limit) {
        log.debug("BATCH_COMMENT_SERVICE_ПОЛУЧЕНИЕ_КОММЕНТАРИЕВ_ДЛЯ_ПОСТА_НАЧАЛО: " +
                "для поста {} с лимитом {}", postId, limit);

        final List<Comment> comments = commentRepository
                .findByPostIdOrderByCreatedAtDesc(postId, PageRequest.of(0, limit));

        final List<CommentResponse> commentResponses = comments.stream()
                .map(comment -> entityMapper.map(comment, CommentResponse.class))
                .collect(Collectors.toList());

        log.debug("BATCH_COMMENT_SERVICE_ПОЛУЧЕНИЕ_КОММЕНТАРИЕВ_ДЛЯ_ПОСТА_УСПЕХ: " +
                "для поста {} найдено {} комментариев", postId, commentResponses.size());
        return commentResponses;
    }
}