package ru.cs.vsu.social_network.contents_service.utils.factory;

import ru.cs.vsu.social_network.contents_service.dto.request.like.LikeCommentRequest;
import ru.cs.vsu.social_network.contents_service.entity.LikeComment;

/**
 * Фабрика для создания сущностей LikeComment.
 * Специализированная реализация для работы с лайками на комментарий.
 */
public interface LikeCommentFactory extends ContentFactory<LikeComment, LikeCommentRequest>{
}
