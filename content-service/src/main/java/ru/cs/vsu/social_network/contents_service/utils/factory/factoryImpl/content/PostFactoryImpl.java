package ru.cs.vsu.social_network.contents_service.utils.factory.factoryImpl.content;

import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.dto.request.post.PostCreateRequest;
import ru.cs.vsu.social_network.contents_service.entity.Post;
import ru.cs.vsu.social_network.contents_service.utils.factory.content.AbstractContentFactory;
import ru.cs.vsu.social_network.contents_service.utils.factory.content.PostFactory;

import java.util.UUID;

/**
 * Реализация фабрики для создания сущностей Post.
 * Создает новые экземпляры постов на основе входных данных.
 */
@Component
public class PostFactoryImpl extends AbstractContentFactory<Post, PostCreateRequest>
        implements PostFactory {

    /** {@inheritDoc} */
    @Override
    protected Post buildEntity(UUID keycloakUserId, PostCreateRequest request) {
        return Post.builder()
                .ownerId(keycloakUserId)
                .content(request.getContent())
                .imageUrl(null)
                .build();
    }

    /** {@inheritDoc} */
    @Override
    protected String getFactoryName() {
        return "ПОСТ";
    }
}