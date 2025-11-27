package ru.cs.vsu.social_network.contents_service.provider;

import ru.cs.vsu.social_network.contents_service.entity.LikeComment;

/**
 * Специализированный провайдер для доступа к сущностям LikeComment.
 * Обеспечивает получение лайков по идентификатору с обработкой ошибок.
 */
public interface LikeCommentEntityProvider extends EntityProvider<LikeComment>{
}
