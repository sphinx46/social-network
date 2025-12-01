package ru.cs.vsu.social_network.contents_service.service.content;

import ru.cs.vsu.social_network.contents_service.dto.request.comment.*;
import ru.cs.vsu.social_network.contents_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.content.CommentResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;

import java.util.UUID;

/**
 * Сервис для работы с комментариями.
 * Предоставляет методы для создания, редактирования, удаления и получения комментариев.
 * Обеспечивает бизнес-логику управления комментариями к постам.
 */
public interface CommentService {

    /**
     * Создает новый комментарий к посту.
     * Валидирует входные данные и сохраняет комментарий в базе данных.
     *
     * @param keycloakUserId идентификатор пользователя, создающего комментарий
     * @param commentCreateRequest DTO с данными для создания комментария
     * @return DTO созданного комментария
     */
    CommentResponse createComment(UUID keycloakUserId,
                                  CommentCreateRequest commentCreateRequest);

    /**
     * Редактирует существующий комментарий.
     * Проверяет права доступа пользователя к комментарию перед редактированием.
     *
     * @param keycloakUserId идентификатор пользователя, редактирующего комментарий
     * @param commentEditRequest DTO с данными для редактирования комментария
     * @return DTO отредактированного комментария
     */
    CommentResponse editComment(UUID keycloakUserId,
                                CommentEditRequest commentEditRequest);

    /**
     * Удаляет комментарий.
     * Проверяет права доступа пользователя перед удалением комментария.
     *
     * @param keycloakUserId идентификатор пользователя, удаляющего комментарий
     * @param commentDeleteRequest DTO с данными для удаления комментария
     * @return DTO удаленного комментария
     */
    CommentResponse deleteComment(UUID keycloakUserId,
                                  CommentDeleteRequest commentDeleteRequest);

    /**
     * Удаляет изображение из комментария.
     * Устанавливает значение imageUrl в null для указанного комментария.
     *
     * @param keycloakUserId идентификатор пользователя, удаляющего изображение
     * @param commentRemoveImageRequest DTO с данными для удаления изображения
     * @return DTO комментария с удаленным изображением
     */
    CommentResponse removeImage(UUID keycloakUserId,
                                CommentRemoveImageRequest commentRemoveImageRequest);

    /**
     * Получает комментарий по его идентификатору.
     * Возвращает полную информацию о комментарии включая метаданные.
     *
     * @param commentId идентификатор комментария
     * @return DTO найденного комментария
     */
    CommentResponse getCommentById(UUID commentId);

    /**
     * Получает страницу комментариев пользователя к указанному посту.
     * Возвращает только комментарии текущего пользователя для конкретного поста.
     *
     * @param keycloakUserId идентификатор пользователя, запрашивающего комментарии
     * @param postId идентификатор поста, для которого запрашиваются комментарии
     * @param pageRequest параметры пагинации и сортировки
     * @return страница с комментариями пользователя к указанному посту
     */
    PageResponse<CommentResponse> getCommentsByPostAndOwner(UUID keycloakUserId,
                                                            UUID postId,
                                                            PageRequest pageRequest);

    /**
     * Получает страницу всех комментариев к указанному посту.
     * Возвращает комментарии всех пользователей для конкретного поста.
     *
     * @param postId идентификатор поста, для которого запрашиваются комментарии
     * @param pageRequest параметры пагинации и сортировки
     * @return страница со всеми комментариями к указанному посту
     */
    PageResponse<CommentResponse> getCommentsByPost(UUID postId,
                                                    PageRequest pageRequest);

    /**
     * Загружает изображение для комментария.
     * Обновляет URL изображения для указанного комментария после проверки прав доступа.
     *
     * @param keycloakUserId идентификатор пользователя, загружающего изображение
     * @param request DTO с данными для загрузки изображения комментария
     * @return DTO комментария с обновленным изображением
     */
    CommentResponse uploadImage(UUID keycloakUserId,
                                CommentUploadImageRequest request);
}