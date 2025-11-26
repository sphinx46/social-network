package ru.cs.vsu.social_network.contents_service.utils.factory.factoryImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.dto.request.post.PostCreateRequest;
import ru.cs.vsu.social_network.contents_service.entity.Post;
import ru.cs.vsu.social_network.contents_service.utils.factory.PostFactory;

import java.util.UUID;

/**
 * Реализация фабрики для создания сущностей Post.
 * Создает новые экземпляры постов на основе входных данных.
 */
@Slf4j
@Component
public class PostFactoryImpl implements PostFactory {

    /** {@inheritDoc} */
    @Override
    public Post create(UUID keycloakUserId, PostCreateRequest request) {
        log.info("ПОСТ_ФАБРИКА_СОЗДАНИЕ_НАЧАЛО: создание поста для пользователя: {}", keycloakUserId);

        Post post = Post.builder()
                .ownerId(keycloakUserId)
                .content(request.getContent())
                .imageUrl(null)
                .build();

        log.info("ПОСТ_ФАБРИКА_СОЗДАНИЕ_УСПЕХ: пост создан для пользователя: {}", keycloakUserId);
        return post;
    }
}