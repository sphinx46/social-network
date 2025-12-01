package ru.cs.vsu.social_network.contents_service.utils.factory.factoryImpl.content;

import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.dto.request.comment.CommentCreateRequest;
import ru.cs.vsu.social_network.contents_service.entity.Comment;
import ru.cs.vsu.social_network.contents_service.entity.Post;
import ru.cs.vsu.social_network.contents_service.provider.PostEntityProvider;
import ru.cs.vsu.social_network.contents_service.utils.factory.content.AbstractContentFactory;
import ru.cs.vsu.social_network.contents_service.utils.factory.content.CommentFactory;

import java.util.UUID;

/**
 * Реализация фабрики для создания сущностей Comment.
 * Создает новые экземпляры комментариев на основе входных данных.
 */
@Component
public class CommentFactoryImpl extends AbstractContentFactory<Comment, CommentCreateRequest>
        implements CommentFactory {
    private final PostEntityProvider postEntityProvider;

    public CommentFactoryImpl(PostEntityProvider postEntityProvider) {
        this.postEntityProvider = postEntityProvider;
    }

    /** {@inheritDoc} */
    @Override
    protected Comment buildEntity(UUID keycloakUserId, CommentCreateRequest request) {
        Post post = postEntityProvider.getById(request.getPostId());

        return Comment.builder()
                .ownerId(keycloakUserId)
                .post(post)
                .content(request.getContent())
                .imageUrl(null)
                .build();
    }

    /** {@inheritDoc} */
    @Override
    protected String getFactoryName() {
        return "КОММЕНТАРИЙ";
    }
}