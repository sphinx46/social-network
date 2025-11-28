package ru.cs.vsu.social_network.contents_service.service.batch.batchImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.cs.vsu.social_network.contents_service.dto.response.content.CommentResponse;
import ru.cs.vsu.social_network.contents_service.entity.Comment;
import ru.cs.vsu.social_network.contents_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.contents_service.provider.CommentEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.CommentRepository;
import ru.cs.vsu.social_network.contents_service.service.batch.BatchCommentService;

import java.util.*;
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

    private static final int MAX_BATCH_SIZE = 1000;

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<UUID, Long> getCommentsCountsForPosts(final List<UUID> postIds) {
        log.debug("BATCH_COMMENT_SERVICE_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_КОММЕНТАРИЕВ_НАЧАЛО: " +
                "для {} постов", postIds.size());

        if (postIds.isEmpty()) {
            log.debug("BATCH_COMMENT_SERVICE_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_КОММЕНТАРИЕВ_ПУСТОЙ_СПИСОК");
            return Collections.emptyMap();
        }

        final List<UUID> batchPostIds = postIds.size() > MAX_BATCH_SIZE ?
                postIds.subList(0, MAX_BATCH_SIZE) : postIds;

        final Map<UUID, Long> result =
                commentEntityProvider.getCommentsCountsForPosts(batchPostIds);

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

        if (postIds.isEmpty()) {
            log.debug("BATCH_COMMENT_SERVICE_ПОЛУЧЕНИЕ_КОММЕНТАРИЕВ_ДЛЯ_ПОСТОВ_ПУСТОЙ_СПИСОК");
            return Collections.emptyMap();
        }

        final List<UUID> batchPostIds = postIds.size() > MAX_BATCH_SIZE ?
                postIds.subList(0, MAX_BATCH_SIZE) : postIds;

        final Pageable pageable = PageRequest.of(0, commentsLimit);
        final List<Comment> allComments = commentRepository
                .findRecentCommentsForPosts(batchPostIds, pageable);

        final Map<UUID, List<CommentResponse>> result = new HashMap<>();

        batchPostIds.forEach(postId -> result.put(postId, new ArrayList<>()));

        for (Comment comment : allComments) {
            UUID postId = comment.getPost() != null ? comment.getPost().getId() : null;
            if (postId != null && result.containsKey(postId)) {
                CommentResponse response = entityMapper.map(comment, CommentResponse.class);
                result.get(postId).add(response);
            }
        }

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
                .findByPostIdOrderByCreatedAtDesc(postId, PageRequest.of(0, limit))
                .getContent();

        final List<CommentResponse> commentResponses = comments.stream()
                .map(comment -> entityMapper.map(comment, CommentResponse.class))
                .collect(Collectors.toList());

        log.debug("BATCH_COMMENT_SERVICE_ПОЛУЧЕНИЕ_КОММЕНТАРИЕВ_ДЛЯ_ПОСТА_УСПЕХ: " +
                "для поста {} найдено {} комментариев", postId, commentResponses.size());
        return commentResponses;
    }
}