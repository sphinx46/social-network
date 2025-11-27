package ru.cs.vsu.social_network.contents_service.provider;

import ru.cs.vsu.social_network.contents_service.entity.Post;
import ru.cs.vsu.social_network.contents_service.exception.post.PostNotFoundException;

import java.util.UUID;

/**
 * Провайдер для доступа к сущностям Post.
 * Обеспечивает получение постов по идентификатору с обработкой ошибок.
 */
public interface PostEntityProvider {

    /**
     * Получает пост по идентификатору.
     *
     * @param id идентификатор поста
     * @return найденный пост
     * @throws PostNotFoundException если пост не найден
     */
    Post getById(UUID id);
}