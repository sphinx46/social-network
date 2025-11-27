package ru.cs.vsu.social_network.contents_service.provider;

import ru.cs.vsu.social_network.contents_service.entity.Post;

/**
 * Специализированный провайдер для доступа к сущностям Post.
 * Обеспечивает получение постов по идентификатору с обработкой ошибок.
 */
public interface PostEntityProvider extends EntityProvider<Post> {
}