package ru.cs.vsu.social_network.contents_service.utils.factory.content;

import ru.cs.vsu.social_network.contents_service.dto.request.post.PostCreateRequest;
import ru.cs.vsu.social_network.contents_service.entity.Post;

/**
 * Фабрика для создания сущностей Post.
 * Специализированная реализация для работы с постами.
 */
public interface PostFactory extends ContentFactory<Post, PostCreateRequest> {
}