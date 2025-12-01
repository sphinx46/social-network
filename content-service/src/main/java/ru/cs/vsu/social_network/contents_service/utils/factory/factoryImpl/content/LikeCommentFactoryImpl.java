package ru.cs.vsu.social_network.contents_service.utils.factory.factoryImpl.content;

import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.dto.request.like.LikeCommentRequest;
import ru.cs.vsu.social_network.contents_service.entity.Comment;
import ru.cs.vsu.social_network.contents_service.entity.LikeComment;
import ru.cs.vsu.social_network.contents_service.provider.CommentEntityProvider;
import ru.cs.vsu.social_network.contents_service.utils.factory.content.AbstractContentFactory;
import ru.cs.vsu.social_network.contents_service.utils.factory.content.LikeCommentFactory;

import java.util.UUID;

/**
 * Реализация фабрики для создания сущностей LikeComment для комментариев.
 * Создает новые экземпляры лайков на комментарий на основе входных данных.
 */
@Component
public class LikeCommentFactoryImpl extends AbstractContentFactory<LikeComment, LikeCommentRequest>
        implements LikeCommentFactory {
    private final CommentEntityProvider commentEntityProvider;

    public LikeCommentFactoryImpl(CommentEntityProvider commentEntityProvider) {
        this.commentEntityProvider = commentEntityProvider;
    }

    /** {@inheritDoc} */
    @Override
    protected LikeComment  buildEntity(UUID keycloakUserId, LikeCommentRequest request) {
        Comment comment = commentEntityProvider.getById(request.getCommentId());

        return LikeComment .builder()
                .ownerId(keycloakUserId)
                .comment(comment)
                .build();
    }

    /** {@inheritDoc} */
    @Override
    protected String getFactoryName() {
        return "ЛАЙК_КОММЕНТАРИЙ";
    }
}