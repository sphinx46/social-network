package ru.cs.vsu.social_network.contents_service.provider;

import ru.cs.vsu.social_network.contents_service.entity.LikePost;

/**
 * Специализированный провайдер для доступа к сущностям LikePost.
 * Обеспечивает получение лайков по идентификатору с обработкой ошибок.
 */
public interface LikePostEntityProvider extends EntityProvider<LikePost> {
}