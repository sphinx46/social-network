package ru.cs.vsu.social_network.contents_service.provider.providerImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.entity.Comment;
import ru.cs.vsu.social_network.contents_service.exception.comment.CommentNotFoundException;
import ru.cs.vsu.social_network.contents_service.provider.AbstractEntityProvider;
import ru.cs.vsu.social_network.contents_service.provider.CommentEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.CommentRepository;
import ru.cs.vsu.social_network.contents_service.utils.MessageConstants;

import java.util.UUID;

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

    /**
     * Получает количество комментариев для поста.
     *
     * @param postId идентификатор поста
     * @return количество комментариев
     */
    public Long getCommentsCountByPost(UUID postId) {
        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_ПО_ПОСТУ_НАЧАЛО: для поста с ID: {}",
                ENTITY_NAME, postId);

        final long count = commentRepository.countByPostId(postId);

        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_ПО_ПОСТУ_УСПЕХ: " +
                        "для поста с ID: {} найдено {} комментариев",
                ENTITY_NAME, postId, count);

        return count;
    }
}