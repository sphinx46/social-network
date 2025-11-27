package ru.cs.vsu.social_network.contents_service.utils.factory;

import ru.cs.vsu.social_network.contents_service.dto.request.like.LikePostRequest;
import ru.cs.vsu.social_network.contents_service.entity.LikePost;

/**
 * Фабрика для создания сущностей LikePost для постов.
 * Специализированная реализация для работы с лайками на пост.
 */
public interface LikePostFactory extends ContentFactory<LikePost, LikePostRequest> {
}