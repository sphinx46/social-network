package ru.cs.vsu.social_network.contents_service.validation.validationImpl;

import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.entity.Comment;
import ru.cs.vsu.social_network.contents_service.provider.CommentEntityProvider;
import ru.cs.vsu.social_network.contents_service.validation.AbstractContentValidator;
import ru.cs.vsu.social_network.contents_service.validation.CommentValidator;

import java.util.UUID;

/**
 * Реализация валидатора для проверки прав доступа к комментариям.
 * Обеспечивает проверку владения комментариями и логирование попыток несанкционированного доступа.
 */
@Component
public class CommentValidatorImpl extends AbstractContentValidator<Comment>
        implements CommentValidator {
    private static final String ENTITY_NAME = "КОММЕНТАРИЙ";

    public CommentValidatorImpl(CommentEntityProvider commentEntityProvider) {
        super(commentEntityProvider, ENTITY_NAME);
    }

    /** {@inheritDoc} */
    @Override
    protected UUID extractOwnerId(Comment entity) {
        return entity.getOwnerId();
    }
}