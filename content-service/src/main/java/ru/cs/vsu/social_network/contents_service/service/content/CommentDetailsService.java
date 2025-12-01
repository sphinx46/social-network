package ru.cs.vsu.social_network.contents_service.service.content;

import ru.cs.vsu.social_network.contents_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.content.CommentDetailsResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;

import java.util.UUID;

/**
 * Сервис для работы с детальной информацией о комментариях.
 * Обеспечивает получение комментариев с связанными сущностями (лайки).
 */
public interface CommentDetailsService {

    /**
     * Получает детальную информацию о комментарии с связанными сущностями.
     *
     * @param commentId идентификатор комментария
     * @param includeLikes флаг включения лайков
     * @param likesLimit ограничение количества лайков
     * @return детальная информация о комментарии
     */
    CommentDetailsResponse getCommentDetails(UUID commentId,
                                             boolean includeLikes,
                                             int likesLimit);

    /**
     * Получает детальную информацию о комментариях поста с пагинацией.
     *
     * @param postId идентификатор поста
     * @param pageRequest параметры пагинации
     * @param includeLikes флаг включения лайков
     * @param likesLimit ограничение количества лайков на комментарий
     * @return страница с детальной информацией о комментариях
     */
    PageResponse<CommentDetailsResponse> getPostCommentsDetails(UUID postId,
                                                                PageRequest pageRequest,
                                                                boolean includeLikes,
                                                                int likesLimit);

    /**
     * Получает детальную информацию о комментариях пользователя с пагинацией.
     *
     * @param userId идентификатор пользователя
     * @param pageRequest параметры пагинации
     * @param includeLikes флаг включения лайков
     * @param likesLimit ограничение количества лайков на комментарий
     * @return страница с детальной информацией о комментариях
     */
    PageResponse<CommentDetailsResponse> getUserCommentsDetails(UUID userId,
                                                                PageRequest pageRequest,
                                                                boolean includeLikes,
                                                                int likesLimit);
}