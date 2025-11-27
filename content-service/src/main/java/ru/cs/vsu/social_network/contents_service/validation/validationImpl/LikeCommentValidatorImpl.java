package ru.cs.vsu.social_network.contents_service.validation.validationImpl;

import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.entity.LikeComment;
import ru.cs.vsu.social_network.contents_service.provider.LikeCommentEntityProvider;
import ru.cs.vsu.social_network.contents_service.validation.AbstractContentValidator;
import ru.cs.vsu.social_network.contents_service.validation.LikeCommentValidator;

import java.util.UUID;

/**
 * Реализация валидатора для проверки прав доступа к лайкам комментариев.
 * Обеспечивает проверку владения лайками комментариев и логирование попыток несанкционированного доступа.
 */
@Component
public class LikeCommentValidatorImpl extends AbstractContentValidator<LikeComment>
        implements LikeCommentValidator {
    private static final String ENTITY_NAME = "ЛАЙК_КОММЕНТАРИЙ";

    public LikeCommentValidatorImpl(LikeCommentEntityProvider likeCommentEntityProvider) {
        super(likeCommentEntityProvider, ENTITY_NAME);
    }

    /** {@inheritDoc} */
    @Override
    protected UUID extractOwnerId(LikeComment entity) {
        return entity.getOwnerId();
    }
}