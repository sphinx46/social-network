package ru.cs.vsu.social_network.contents_service.provider.providerImpl;

import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.entity.Comment;
import ru.cs.vsu.social_network.contents_service.exception.comment.CommentNotFoundException;
import ru.cs.vsu.social_network.contents_service.provider.AbstractEntityProvider;
import ru.cs.vsu.social_network.contents_service.provider.CommentEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.CommentRepository;
import ru.cs.vsu.social_network.contents_service.utils.MessageConstants;

/**
 * Реализация провайдера для получения сущности Comment.
 * Обеспечивает доступ к данным комментариев с обработкой исключительных ситуаций.
 */
@Component
public final class CommentEntityProviderImpl extends AbstractEntityProvider<Comment>
        implements CommentEntityProvider {
    private static final String ENTITY_NAME = "КОММЕНТАРИЙ";

    public CommentEntityProviderImpl(CommentRepository commentRepository) {
        super(commentRepository, ENTITY_NAME, () ->
                new CommentNotFoundException(MessageConstants.COMMENT_NOT_FOUND_FAILURE));
    }
}