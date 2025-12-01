package ru.cs.vsu.social_network.contents_service.service.content;

import ru.cs.vsu.social_network.contents_service.dto.request.like.LikeCommentRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.content.LikeCommentResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;

import java.util.UUID;

/**
 * Сервис для управления лайками комментариев.
 * Предоставляет методы для создания и удаления лайков на комментарии.
 */
public interface LikeCommentService {

    /**
     * Создает лайк для комментария от указанного пользователя.
     *
     * @param keycloakUserId идентификатор пользователя в Keycloak
     * @param likeCommentRequest данные для создания лайка
     * @return созданный лайк
     */
    LikeCommentResponse create(UUID keycloakUserId,
                               LikeCommentRequest likeCommentRequest);

    /**
     * Удаляет лайк с комментария для указанного пользователя.
     *
     * @param keycloakUserId идентификатор пользователя в Keycloak
     * @param likeCommentRequest данные для удаления лайка
     * @return удаленный лайк
     */
    LikeCommentResponse delete(UUID keycloakUserId,
                               LikeCommentRequest likeCommentRequest);

    /**
     * Получает все лайки комментария с пагинацией.
     *
     * @param commentId идентификатор комментария
     * @param pageRequest параметры пагинации
     * @return страница с лайками комментария
     */
    PageResponse<LikeCommentResponse> getAllLikesByComment(UUID commentId,
                                                           PageRequest pageRequest);

    /**
     * Получает количество лайков комментария.
     *
     * @param commentId идентификатор комментария
     * @return количество лайков
     */
    Long getLikesCountByComment(UUID commentId);
}