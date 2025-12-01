package ru.cs.vsu.social_network.contents_service.service.content;

import ru.cs.vsu.social_network.contents_service.dto.request.like.LikePostRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.content.LikePostResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;

import java.util.UUID;

/**
 * Сервис для управления лайками постов.
 * Предоставляет методы для создания и удаления лайков на посты.
 */
public interface LikePostService {

    /**
     * Создает лайк для поста от указанного пользователя.
     *
     * @param keycloakUserId идентификатор пользователя в Keycloak
     * @param likePostRequest данные для создания лайка
     * @return созданный лайк
     */
    LikePostResponse create(UUID keycloakUserId,
                            LikePostRequest likePostRequest);

    /**
     * Удаляет лайк с поста для указанного пользователя.
     *
     * @param keycloakUserId идентификатор пользователя в Keycloak
     * @param likePostRequest данные для удаления лайка
     * @return удаленный лайк
     */
    LikePostResponse delete(UUID keycloakUserId,
                            LikePostRequest likePostRequest);

    /**
     * Получает все лайки поста с пагинацией.
     *
     * @param postId идентификатор поста
     * @param pageRequest параметры пагинации
     * @return страница с лайками поста
     */
    PageResponse<LikePostResponse> getAllLikesByPost(UUID postId,
                                                     PageRequest pageRequest);

    /**
     * Получает количество лайков поста.
     *
     * @param postId идентификатор поста
     * @return количество лайков
     */
    Long getLikesCountByPost(UUID postId);
}