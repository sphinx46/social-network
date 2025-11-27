package ru.cs.vsu.social_network.contents_service.provider.providerImpl;

import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.entity.LikePost;
import ru.cs.vsu.social_network.contents_service.exception.like.LikeNotFoundException;
import ru.cs.vsu.social_network.contents_service.provider.AbstractEntityProvider;
import ru.cs.vsu.social_network.contents_service.provider.LikePostEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.LikePostRepository;
import ru.cs.vsu.social_network.contents_service.utils.MessageConstants;

/**
 * Реализация провайдера для получения сущности LikePost.
 * Обеспечивает доступ к данным лайков с обработкой исключительных ситуаций.
 */
@Component
public final class LikePostEntityProviderImpl extends AbstractEntityProvider<LikePost>
        implements LikePostEntityProvider {
    private static final String ENTITY_NAME = "ЛАЙК_ПОСТ";

    public LikePostEntityProviderImpl(LikePostRepository likePostRepository) {
        super(likePostRepository, ENTITY_NAME, () ->
                new LikeNotFoundException(MessageConstants.LIKE_NOT_FOUND_FAILURE));
    }
}