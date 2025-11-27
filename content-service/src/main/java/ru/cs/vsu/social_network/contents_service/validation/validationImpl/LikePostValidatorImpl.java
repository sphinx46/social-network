package ru.cs.vsu.social_network.contents_service.validation.validationImpl;

import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.entity.LikePost;
import ru.cs.vsu.social_network.contents_service.provider.LikePostEntityProvider;
import ru.cs.vsu.social_network.contents_service.validation.AbstractContentValidator;
import ru.cs.vsu.social_network.contents_service.validation.LikePostValidator;

import java.util.UUID;

/**
 * Реализация валидатора для проверки прав доступа к лайкам постов.
 * Обеспечивает проверку владения лайками постов и логирование попыток несанкционированного доступа.
 */
@Component
public class LikePostValidatorImpl extends AbstractContentValidator<LikePost>
        implements LikePostValidator {
    private static final String ENTITY_NAME = "ЛАЙК_ПОСТ";

    public LikePostValidatorImpl(LikePostEntityProvider likePostEntityProvider) {
        super(likePostEntityProvider, ENTITY_NAME);
    }

    /** {@inheritDoc} */
    @Override
    protected UUID extractOwnerId(LikePost entity) {
        return entity.getOwnerId();
    }
}