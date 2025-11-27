package ru.cs.vsu.social_network.contents_service.service;

import ru.cs.vsu.social_network.contents_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.post.PostCreateRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.post.PostEditRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.post.PostRemoveImageRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.post.PostUploadImageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.content.PostResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;

import java.util.UUID;

/**
 * Сервис для управления постами.
 * Предоставляет методы для создания, редактирования, получения и управления изображениями постов.
 */
public interface PostService {

    /**
     * Создает новый пост для указанного пользователя.
     *
     * @param keycloakUserId идентификатор пользователя в Keycloak
     * @param request данные для создания поста
     * @return созданный пост
     */
    PostResponse create(UUID keycloakUserId, PostCreateRequest request);

    /**
     * Редактирует существующий пост.
     *
     * @param keycloakUserId идентификатор пользователя в Keycloak
     * @param request данные для редактирования поста
     * @return отредактированный пост
     */
    PostResponse editPost(UUID keycloakUserId, PostEditRequest request);

    /**
     * Удаляет изображение из поста.
     *
     * @param keycloakUserId идентификатор пользователя в Keycloak
     * @param request данные для удаления изображения
     * @return пост без изображения
     */
    PostResponse removeImage(UUID keycloakUserId, PostRemoveImageRequest request);

    /**
     * Получает пост по его идентификатору.
     *
     * @param postId идентификатор поста
     * @return найденный пост
     */
    PostResponse getPostById(UUID postId);

    /**
     * Загружает изображение для поста.
     * Обновляет URL изображения для указанного поста после проверки прав доступа.
     *
     * @param keycloakUserId идентификатор пользователя в Keycloak
     * @param request данные для загрузки изображения поста
     * @return пост с обновленным изображением
     */
    PostResponse uploadPostImage(UUID keycloakUserId, PostUploadImageRequest request);

    /**
     * Получает все посты пользователя с пагинацией.
     *
     * @param keycloakUserId идентификатор пользователя в Keycloak
     * @param pageRequest параметры пагинации
     * @return страница с постами пользователя
     */
    PageResponse<PostResponse> getAllPostsByUser(UUID keycloakUserId, PageRequest pageRequest);
}