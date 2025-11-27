package ru.cs.vsu.social_network.contents_service.utils.factory.factoryImpl;

import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.dto.request.like.LikePostRequest;
import ru.cs.vsu.social_network.contents_service.entity.LikePost;
import ru.cs.vsu.social_network.contents_service.entity.Post;
import ru.cs.vsu.social_network.contents_service.provider.PostEntityProvider;
import ru.cs.vsu.social_network.contents_service.utils.factory.AbstractContentFactory;
import ru.cs.vsu.social_network.contents_service.utils.factory.LikePostFactory;

import java.util.UUID;

/**
 * Реализация фабрики для создания сущностей LikePost для постов.
 * Создает новые экземпляры лайков на пост на основе входных данных.
 */
@Component
public class LikePostFactoryImpl extends AbstractContentFactory<LikePost, LikePostRequest>
        implements LikePostFactory {
    private final PostEntityProvider postEntityProvider;

    public LikePostFactoryImpl(PostEntityProvider postEntityProvider) {
        this.postEntityProvider = postEntityProvider;
    }

    /** {@inheritDoc} */
    @Override
    protected LikePost buildEntity(UUID keycloakUserId, LikePostRequest request) {
        Post post = postEntityProvider.getById(request.getPostId());

        return LikePost.builder()
                .ownerId(keycloakUserId)
                .post(post)
                .build();
    }

    /** {@inheritDoc} */
    @Override
    protected String getFactoryName() {
        return "ЛАЙК_ПОСТ";
    }
}