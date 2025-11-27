package ru.cs.vsu.social_network.contents_service.validation.validationImpl;

import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.entity.Post;
import ru.cs.vsu.social_network.contents_service.provider.PostEntityProvider;
import ru.cs.vsu.social_network.contents_service.validation.AbstractContentValidator;
import ru.cs.vsu.social_network.contents_service.validation.PostValidator;

import java.util.UUID;

/**
 * Реализация валидатора для проверки прав доступа к постам.
 * Обеспечивает проверку владения постами и логирование попыток несанкционированного доступа.
 */
@Component
public class PostValidatorImpl extends AbstractContentValidator<Post> implements PostValidator {
    private static final String ENTITY_NAME = "ПОСТ";

    public PostValidatorImpl(PostEntityProvider postEntityProvider) {
        super(postEntityProvider, ENTITY_NAME);
    }

    /** {@inheritDoc} */
    @Override
    protected UUID extractOwnerId(Post entity) {
        return entity.getOwnerId();
    }
}