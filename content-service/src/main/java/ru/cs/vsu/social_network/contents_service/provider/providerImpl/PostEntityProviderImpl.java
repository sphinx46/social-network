package ru.cs.vsu.social_network.contents_service.provider.providerImpl;

import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.entity.Post;
import ru.cs.vsu.social_network.contents_service.exception.post.PostNotFoundException;
import ru.cs.vsu.social_network.contents_service.provider.AbstractEntityProvider;
import ru.cs.vsu.social_network.contents_service.provider.PostEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.PostRepository;
import ru.cs.vsu.social_network.contents_service.utils.MessageConstants;

/**
 * Реализация провайдера для получения сущности Post.
 * Обеспечивает доступ к данным постов с обработкой исключительных ситуаций.
 */
@Component
public final class PostEntityProviderImpl extends AbstractEntityProvider<Post>
        implements PostEntityProvider {
    private static final String ENTITY_NAME = "ПОСТ";

    public PostEntityProviderImpl(PostRepository postRepository) {
        super(postRepository, ENTITY_NAME, () ->
                new PostNotFoundException(MessageConstants.POST_NOT_FOUND_FAILURE));
    }
}