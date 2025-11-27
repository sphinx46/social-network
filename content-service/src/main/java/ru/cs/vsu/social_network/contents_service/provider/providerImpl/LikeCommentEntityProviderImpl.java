package ru.cs.vsu.social_network.contents_service.provider.providerImpl;

import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.entity.LikeComment;
import ru.cs.vsu.social_network.contents_service.exception.like.LikeNotFoundException;
import ru.cs.vsu.social_network.contents_service.provider.AbstractEntityProvider;
import ru.cs.vsu.social_network.contents_service.provider.LikeCommentEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.LikeCommentRepository;
import ru.cs.vsu.social_network.contents_service.utils.MessageConstants;

/**
 * Реализация провайдера для получения сущности LikeComment.
 * Обеспечивает доступ к данным лайков с обработкой исключительных ситуаций.
 */
@Component
public final class LikeCommentEntityProviderImpl extends AbstractEntityProvider<LikeComment>
        implements LikeCommentEntityProvider {
    private static final String ENTITY_NAME = "ЛАЙК_КОММЕНТАРИЙ";

    public LikeCommentEntityProviderImpl(LikeCommentRepository likeCommentRepository) {
        super(likeCommentRepository, ENTITY_NAME, () ->
                new LikeNotFoundException(MessageConstants.LIKE_NOT_FOUND_FAILURE));
    }
}