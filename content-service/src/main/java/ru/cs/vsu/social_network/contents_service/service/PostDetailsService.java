package ru.cs.vsu.social_network.contents_service.service;

import ru.cs.vsu.social_network.contents_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.content.PostDetailsResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;

import java.util.UUID;

/**
 * Сервис для работы с детальной информацией о постах.
 * Обеспечивает получение постов с связанными сущностями (лайки, комментарии).
 */
public interface PostDetailsService {

    /**
     * Получает детальную информацию о посте с связанными сущностями.
     *
     * @param postId идентификатор поста
     * @param includeComments флаг включения комментариев
     * @param includeLikes флаг включения лайков
     * @param commentsLimit ограничение количества комментариев
     * @param likesLimit ограничение количества лайков
     * @return детальная информация о посте
     */
    PostDetailsResponse getPostDetails(UUID postId,
                                       boolean includeComments,
                                       boolean includeLikes,
                                       int commentsLimit,
                                       int likesLimit);

    /**
     * Получает детальную информацию о постах пользователя с пагинацией.
     *
     * @param userId идентификатор пользователя
     * @param pageRequest параметры пагинации
     * @param includeComments флаг включения комментариев
     * @param includeLikes флаг включения лайков
     * @param commentsLimit ограничение количества комментариев на пост
     * @param likesLimit ограничение количества лайков на пост
     * @return страница с детальной информацией о постах
     */
    PageResponse<PostDetailsResponse> getUserPostsDetails(UUID userId,
                                                          PageRequest pageRequest,
                                                          boolean includeComments,
                                                          boolean includeLikes,
                                                          int commentsLimit,
                                                          int likesLimit);

    /**
     * Получает детальную информацию о всех постах с пагинацией.
     *
     * @param pageRequest параметры пагинации
     * @param includeComments флаг включения комментариев
     * @param includeLikes флаг включения лайков
     * @param commentsLimit ограничение количества комментариев на пост
     * @param likesLimit ограничение количества лайков на пост
     * @return страница с детальной информацией о постах
     */
    PageResponse<PostDetailsResponse> getAllPostsDetails(PageRequest pageRequest,
                                                         boolean includeComments,
                                                         boolean includeLikes,
                                                         int commentsLimit,
                                                         int likesLimit);
}