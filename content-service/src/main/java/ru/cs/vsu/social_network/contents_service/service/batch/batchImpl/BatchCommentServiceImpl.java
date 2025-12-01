package ru.cs.vsu.social_network.contents_service.service.batch.batchImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cs.vsu.social_network.contents_service.dto.response.content.CommentResponse;
import ru.cs.vsu.social_network.contents_service.entity.Comment;
import ru.cs.vsu.social_network.contents_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.contents_service.provider.CommentEntityProvider;
import ru.cs.vsu.social_network.contents_service.service.batch.BatchCommentService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для пакетных операций с комментариями.
 * Обеспечивает эффективное получение комментариев для множества постов.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BatchCommentServiceImpl implements BatchCommentService {

    private final CommentEntityProvider commentEntityProvider;
    private final EntityMapper entityMapper;

    private static final int MAX_BATCH_SIZE = 1000;
    private static final int BATCH_QUERY_SIZE = 100;
    private static final int DEFAULT_COMMENTS_LIMIT = 50;

    /**
     * Получает количество комментариев для списка постов в одном пакетном запросе.
     * Использует батчинг для больших списков и оптимизированные GROUP BY запросы.
     *
     * @param postIds список идентификаторов постов
     * @return карта соответствия идентификатора поста к количеству комментариев
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

        final Map<UUID, Long> result = new ConcurrentHashMap<>();

        for (int i = 0; i < batchPostIds.size(); i += BATCH_QUERY_SIZE) {
            List<UUID> subList = batchPostIds.subList(i, Math.min(i + BATCH_QUERY_SIZE, batchPostIds.size()));
            Map<UUID, Long> batchResult = commentEntityProvider.getCommentsCountsForPosts(subList);
            result.putAll(batchResult);
        }

        batchPostIds.forEach(postId -> result.putIfAbsent(postId, 0L));

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

        final int effectiveLimit = Math.max(1, Math.min(commentsLimit, DEFAULT_COMMENTS_LIMIT));

        final List<Comment> allComments =
                commentEntityProvider.getRecentCommentsForPosts(batchPostIds, effectiveLimit);

        final Map<UUID, List<CommentResponse>> result = new ConcurrentHashMap<>();
        batchPostIds.forEach(postId -> result.put(postId, new ArrayList<>()));

        Map<UUID, List<Comment>> commentsByPostId = allComments.stream()
                .filter(comment -> comment.getPost() != null)
                .collect(Collectors.groupingBy(comment -> comment.getPost().getId()));

        commentsByPostId.forEach((postId, comments) -> {
            List<CommentResponse> responses = comments.stream()
                    .limit(effectiveLimit)
                    .map(comment -> entityMapper.map(comment, CommentResponse.class))
                    .collect(Collectors.toList());
            result.put(postId, responses);
        });

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

        final int effectiveLimit = Math.max(1, Math.min(limit, 100));
        final List<Comment> comments = commentEntityProvider.getRecentCommentsForPost(postId, effectiveLimit);

        final List<CommentResponse> commentResponses = comments.stream()
                .map(comment -> entityMapper.map(comment, CommentResponse.class))
                .collect(Collectors.toList());

        log.debug("BATCH_COMMENT_SERVICE_ПОЛУЧЕНИЕ_КОММЕНТАРИЕВ_ДЛЯ_ПОСТА_УСПЕХ:" +
                " для поста {} найдено {} комментариев", postId, commentResponses.size());
        return commentResponses;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CommentResponse> getCommentsWithPosts(List<UUID> commentIds, int limit) {
        log.debug("BATCH_COMMENT_SERVICE_ПОЛУЧЕНИЕ_КОММЕНТАРИЕВ_С_ПОСТАМИ_НАЧАЛО: " +
                "для {} комментариев", commentIds.size());

        if (commentIds.isEmpty()) {
            log.debug("BATCH_COMMENT_SERVICE_ПОЛУЧЕНИЕ_КОММЕНТАРИЕВ_С_ПОСТАМИ_ПУСТОЙ_СПИСОК");
            return Collections.emptyList();
        }

        final int effectiveLimit = Math.max(1, Math.min(limit, DEFAULT_COMMENTS_LIMIT));
        final List<Comment> comments = commentEntityProvider.getCommentsWithPosts(commentIds, effectiveLimit);

        final List<CommentResponse> commentResponses = comments.stream()
                .map(comment -> entityMapper.map(comment, CommentResponse.class))
                .collect(Collectors.toList());

        log.debug("BATCH_COMMENT_SERVICE_ПОЛУЧЕНИЕ_КОММЕНТАРИЕВ_С_ПОСТАМИ_УСПЕХ: " +
                "получено {} комментариев с постами", commentResponses.size());
        return commentResponses;
    }
}