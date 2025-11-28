package ru.cs.vsu.social_network.contents_service.provider.providerImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.entity.Comment;
import ru.cs.vsu.social_network.contents_service.exception.comment.CommentNotFoundException;
import ru.cs.vsu.social_network.contents_service.provider.AbstractEntityProvider;
import ru.cs.vsu.social_network.contents_service.provider.CommentEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.CommentRepository;
import ru.cs.vsu.social_network.contents_service.utils.MessageConstants;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация провайдера для получения сущности Comment.
 * Обеспечивает доступ к данным комментариев с обработкой исключительных ситуаций.
 */
@Slf4j
@Component
public final class CommentEntityProviderImpl extends AbstractEntityProvider<Comment>
        implements CommentEntityProvider {
    private static final String ENTITY_NAME = "КОММЕНТАРИЙ";
    private final CommentRepository commentRepository;

    public CommentEntityProviderImpl(CommentRepository commentRepository) {
        super(commentRepository, ENTITY_NAME, () ->
                new CommentNotFoundException(MessageConstants.COMMENT_NOT_FOUND_FAILURE));
        this.commentRepository = commentRepository;
    }

    /** {@inheritDoc} */
    @Override
    public Long getCommentsCountByPost(UUID postId) {
        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_ПО_ПОСТУ_НАЧАЛО: для поста с ID: {}",
                ENTITY_NAME, postId);

        final long count = commentRepository.countByPostId(postId);

        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_ПО_ПОСТУ_УСПЕХ: " +
                        "для поста с ID: {} найдено {} комментариев",
                ENTITY_NAME, postId, count);

        return count;
    }

    /** {@inheritDoc} */
    @Override
    public Map<UUID, Long> getCommentsCountsForPosts(List<UUID> postIds) {
        log.info("{}_ПРОВАЙДЕР_ПАКЕТНОЕ_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_НАЧАЛО: " +
                "для {} постов", postIds.size());

        if (postIds.isEmpty()) {
            log.info("{}_ПРОВАЙДЕР_ПАКЕТНОЕ_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_ПУСТОЙ_СПИСОК", ENTITY_NAME);
            return Collections.emptyMap();
        }

        final List<Object[]> counts = commentRepository.findCommentsCountByPostIds(postIds);

        final Map<UUID, Long> result = counts.stream()
                .collect(Collectors.toMap(
                        tuple -> (UUID) tuple[0],
                        tuple -> (Long) tuple[1]
                ));

        postIds.forEach(postId -> result.putIfAbsent(postId, 0L));

        log.info("{}_ПРОВАЙДЕР_ПАКЕТНОЕ_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_УСПЕХ: " +
                "получено количество комментариев для {} постов", ENTITY_NAME, result.size());

        return result;
    }
}