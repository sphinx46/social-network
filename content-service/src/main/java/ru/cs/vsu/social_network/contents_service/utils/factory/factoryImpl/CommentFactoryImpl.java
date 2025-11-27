package ru.cs.vsu.social_network.contents_service.utils.factory.factoryImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.dto.request.comment.CommentCreateRequest;
import ru.cs.vsu.social_network.contents_service.entity.Comment;
import ru.cs.vsu.social_network.contents_service.entity.Post;
import ru.cs.vsu.social_network.contents_service.provider.PostEntityProvider;
import ru.cs.vsu.social_network.contents_service.utils.factory.CommentFactory;

import java.util.UUID;

/**
 * Реализация фабрики для создания сущностей Comment.
 * Создает новые экземпляры постов на основе входных данных.
 */
@Slf4j
@Component
public class CommentFactoryImpl implements CommentFactory {
    private final PostEntityProvider postEntityProvider;

    public CommentFactoryImpl(PostEntityProvider postEntityProvider) {
        this.postEntityProvider = postEntityProvider;
    }

    /** {@inheritDoc} */
    @Override
    public Comment create(UUID keycloakUserId, CommentCreateRequest request) {
        log.info("КОММЕНТАРИЙ_ФАБРИКА_СОЗДАНИЕ_НАЧАЛО: " +
                "создание комментария для пользователя: {}", keycloakUserId);

        Post post = postEntityProvider.getById(request.getPostId());

        Comment comment = Comment.builder()
                .ownerId(keycloakUserId)
                .post(post)
                .content(request.getContent())
                .imageUrl(null)
                .build();

        log.info("КОММЕНТАРИЙ_ФАБРИКА_СОЗДАНИЕ_УСПЕХ: " +
                "комментарий создан для пользователя: {}", keycloakUserId);
        return comment;
    }
}
