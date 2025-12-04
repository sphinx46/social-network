package ru.cs.vsu.social_network.contents_service.utils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import ru.cs.vsu.social_network.contents_service.dto.request.comment.*;
import ru.cs.vsu.social_network.contents_service.dto.request.like.LikeCommentRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.like.LikePostRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.post.PostCreateRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.post.PostEditRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.post.PostRemoveImageRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.post.PostUploadImageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.content.*;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.contents_service.entity.Comment;
import ru.cs.vsu.social_network.contents_service.entity.LikeComment;
import ru.cs.vsu.social_network.contents_service.entity.LikePost;
import ru.cs.vsu.social_network.contents_service.entity.Post;
import ru.cs.vsu.social_network.contents_service.events.cache.CacheEventType;
import ru.cs.vsu.social_network.contents_service.events.cache.GenericCacheEvent;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Фабрика тестовых данных для messaging-service.
 * Содержит методы для создания тестовых DTO, сущностей и запросов.
 */
public final class TestDataFactory {

    /**
     * Приватный конструктор для запрета создания экземпляров утилитного класса.
     */
    private TestDataFactory() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // Константы для тестовых идентификаторов
    public static final UUID TEST_USER_ID = UUID.fromString("3fc4530f-5c0c-429d-8a64-810ce3bd1415");
    public static final UUID TEST_POST_ID = UUID.fromString("8dce7d87-60d9-4b66-8851-e7dba4684538");
    public static final UUID TEST_COMMENT_ID = UUID.fromString("40d4dde4-663d-4113-bd08-25f5e1d42efe");
    public static final UUID TEST_LIKE_ID = UUID.fromString("5ab3c6d7-ec6f-49ad-95ac-6c752ad8172e");
    public static final UUID TEST_ANOTHER_USER_ID = UUID.fromString("e0d8a734-6f6c-4ab4-b4fe-e93cc63d8406");

    // Константы для тестового содержимого
    public static final String TEST_POST_CONTENT = "Test post messaging";
    public static final String TEST_COMMENT_CONTENT = "Test comment messaging";
    public static final String TEST_IMAGE_URL = "http://example.com/image.jpg";
    public static final String TEST_COMMENT_IMAGE_URL = "http://example.com/comment-image.jpg";
    public static final String TEST_UPDATED_CONTENT = "Updated messaging";

    /**
     * Создает тестовый запрос на создание поста.
     *
     * @param content содержимое поста
     * @return новый экземпляр PostCreateRequest
     */
    public static PostCreateRequest createPostCreateRequest(final String content) {
        return PostCreateRequest.builder()
                .content(content)
                .build();
    }

    /**
     * Создает тестовый запрос на создание поста с дефолтным содержимым.
     *
     * @return новый экземпляр PostCreateRequest
     */
    public static PostCreateRequest createPostCreateRequest() {
        return createPostCreateRequest(TEST_POST_CONTENT);
    }

    /**
     * Создает тестовый запрос на редактирование поста.
     *
     * @param postId  идентификатор поста
     * @param content новое содержимое поста
     * @return новый экземпляр PostEditRequest
     */
    public static PostEditRequest createPostEditRequest(final UUID postId,
                                                        final String content) {
        return PostEditRequest.builder()
                .postId(postId)
                .content(content)
                .build();
    }

    /**
     * Создает тестовый запрос на редактирование поста с дефолтными значениями.
     *
     * @return новый экземпляр PostEditRequest
     */
    public static PostEditRequest createPostEditRequest() {
        return createPostEditRequest(TEST_POST_ID, TEST_UPDATED_CONTENT);
    }

    /**
     * Создает тестовый запрос на загрузку изображения поста.
     *
     * @param postId   идентификатор поста
     * @param imageUrl URL изображения
     * @return новый экземпляр PostUploadImageRequest
     */
    public static PostUploadImageRequest createPostUploadImageRequest(final UUID postId,
                                                                      final String imageUrl) {
        return PostUploadImageRequest.builder()
                .postId(postId)
                .imageUrl(imageUrl)
                .build();
    }

    /**
     * Создает тестовый запрос на загрузку изображения поста с дефолтными значениями.
     *
     * @return новый экземпляр PostUploadImageRequest
     */
    public static PostUploadImageRequest createPostUploadImageRequest() {
        return createPostUploadImageRequest(TEST_POST_ID, TEST_IMAGE_URL);
    }

    /**
     * Создает тестовый запрос на удаление изображения поста.
     *
     * @param postId идентификатор поста
     * @return новый экземпляр PostRemoveImageRequest
     */
    public static PostRemoveImageRequest createPostRemoveImageRequest(final UUID postId) {
        return PostRemoveImageRequest.builder()
                .postId(postId)
                .build();
    }

    /**
     * Создает тестовый запрос на удаление изображения поста с дефолтными значениями.
     *
     * @return новый экземпляр PostRemoveImageRequest
     */
    public static PostRemoveImageRequest createPostRemoveImageRequest() {
        return createPostRemoveImageRequest(TEST_POST_ID);
    }

    /**
     * Создает тестовый запрос на создание комментария.
     *
     * @param postId  идентификатор поста
     * @param content содержимое комментария
     * @return новый экземпляр CommentCreateRequest
     */
    public static CommentCreateRequest createCommentCreateRequest(final UUID postId,
                                                                  final String content) {
        return CommentCreateRequest.builder()
                .postId(postId)
                .content(content)
                .build();
    }

    /**
     * Создает тестовый запрос на создание комментария с дефолтными значениями.
     *
     * @return новый экземпляр CommentCreateRequest
     */
    public static CommentCreateRequest createCommentCreateRequest() {
        return createCommentCreateRequest(TEST_POST_ID, TEST_COMMENT_CONTENT);
    }

    /**
     * Создает тестовый запрос на редактирование комментария.
     *
     * @param commentId идентификатор комментария
     * @param content   новое содержимое комментария
     * @return новый экземпляр CommentEditRequest
     */
    public static CommentEditRequest createCommentEditRequest(final UUID commentId,
                                                              final String content) {
        return CommentEditRequest.builder()
                .commentId(commentId)
                .content(content)
                .build();
    }

    /**
     * Создает тестовый запрос на редактирование комментария с дефолтными значениями.
     *
     * @return новый экземпляр CommentEditRequest
     */
    public static CommentEditRequest createCommentEditRequest() {
        return createCommentEditRequest(TEST_COMMENT_ID, TEST_UPDATED_CONTENT);
    }

    /**
     * Создает тестовый запрос на удаление комментария.
     *
     * @param commentId идентификатор комментария
     * @return новый экземпляр CommentDeleteRequest
     */
    public static CommentDeleteRequest createCommentDeleteRequest(final UUID commentId) {
        return CommentDeleteRequest.builder()
                .commentId(commentId)
                .build();
    }

    /**
     * Создает тестовый запрос на удаление комментария с дефолтными значениями.
     *
     * @return новый экземпляр CommentDeleteRequest
     */
    public static CommentDeleteRequest createCommentDeleteRequest() {
        return createCommentDeleteRequest(TEST_COMMENT_ID);
    }

    /**
     * Создает тестовый запрос на загрузку изображения комментария.
     *
     * @param commentId идентификатор комментария
     * @param imageUrl  URL изображения
     * @return новый экземпляр CommentUploadImageRequest
     */
    public static CommentUploadImageRequest createCommentUploadImageRequest(final UUID commentId,
                                                                            final String imageUrl) {
        return CommentUploadImageRequest.builder()
                .commentId(commentId)
                .imageUrl(imageUrl)
                .build();
    }

    /**
     * Создает тестовый запрос на загрузку изображения комментария с дефолтными значениями.
     *
     * @return новый экземпляр CommentUploadImageRequest
     */
    public static CommentUploadImageRequest createCommentUploadImageRequest() {
        return createCommentUploadImageRequest(TEST_COMMENT_ID, TEST_COMMENT_IMAGE_URL);
    }

    /**
     * Создает тестовый запрос на удаление изображения комментария.
     *
     * @param commentId идентификатор комментария
     * @return новый экземпляр CommentRemoveImageRequest
     */
    public static CommentRemoveImageRequest createCommentRemoveImageRequest(final UUID commentId) {
        return CommentRemoveImageRequest.builder()
                .commentId(commentId)
                .build();
    }

    /**
     * Создает тестовый запрос на удаление изображения комментария с дефолтными значениями.
     *
     * @return новый экземпляр CommentRemoveImageRequest
     */
    public static CommentRemoveImageRequest createCommentRemoveImageRequest() {
        return createCommentRemoveImageRequest(TEST_COMMENT_ID);
    }

    /**
     * Создает тестовый запрос на лайк поста.
     *
     * @param postId идентификатор поста
     * @return новый экземпляр LikePostRequest
     */
    public static LikePostRequest createLikePostRequest(final UUID postId) {
        return LikePostRequest.builder()
                .postId(postId)
                .build();
    }

    /**
     * Создает тестовый запрос на лайк поста с дефолтными значениями.
     *
     * @return новый экземпляр LikePostRequest
     */
    public static LikePostRequest createLikePostRequest() {
        return createLikePostRequest(TEST_POST_ID);
    }

    /**
     * Создает тестовый запрос на лайк комментария.
     *
     * @param commentId идентификатор комментария
     * @return новый экземпляр LikeCommentRequest
     */
    public static LikeCommentRequest createLikeCommentRequest(final UUID commentId) {
        return LikeCommentRequest.builder()
                .commentId(commentId)
                .build();
    }

    /**
     * Создает тестовый запрос на постраничный вывод.
     *
     * @param page номер страницы
     * @param size размер страницы
     * @return новый экземпляр PageRequest
     */
    public static PageRequest createPageRequest(final int page,
                                                final int size) {
        return PageRequest.builder()
                .pageNumber(page)
                .size(size)
                .sortBy("createdAt")
                .direction(org.springframework.data.domain.Sort.Direction.DESC)
                .build();
    }

    /**
     * Создает тестовую сущность поста.
     *
     * @param postId   идентификатор поста
     * @param ownerId  идентификатор владельца
     * @param content  содержимое поста
     * @param imageUrl URL изображения
     * @return новый экземпляр Post
     */
    public static Post createPostEntity(final UUID postId,
                                        final UUID ownerId,
                                        final String content,
                                        final String imageUrl) {
        final Post post = new Post();
        post.setId(postId);
        post.setOwnerId(ownerId);
        post.setContent(content);
        post.setImageUrl(imageUrl);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        return post;
    }

    /**
     * Создает тестовую сущность поста с дефолтными значениями.
     *
     * @param postId идентификатор поста
     * @return новый экземпляр Post
     */
    public static Post createPostEntity(final UUID postId) {
        return createPostEntity(postId, TEST_USER_ID, TEST_POST_CONTENT, TEST_IMAGE_URL);
    }

    /**
     * Создает тестовую сущность комментария.
     *
     * @param commentId идентификатор комментария
     * @param ownerId   идентификатор владельца
     * @param postId    идентификатор поста
     * @param content   содержимое комментария
     * @param imageUrl  URL изображения
     * @return новый экземпляр Comment
     */
    public static Comment createCommentEntity(final UUID commentId,
                                              final UUID ownerId,
                                              final UUID postId,
                                              final String content,
                                              final String imageUrl) {
        final Comment comment = new Comment();
        comment.setId(commentId);
        comment.setOwnerId(ownerId);
        comment.setPost(createPostEntity(postId, ownerId, "Post messaging", null));
        comment.setContent(content);
        comment.setImageUrl(imageUrl);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        return comment;
    }

    /**
     * Создает тестовую сущность комментария с дефолтными значениями.
     *
     * @param commentId идентификатор комментария
     * @return новый экземпляр Comment
     */
    public static Comment createCommentEntity(final UUID commentId) {
        return createCommentEntity(commentId, TEST_USER_ID, TEST_POST_ID, TEST_COMMENT_CONTENT, null);
    }

    /**
     * Создает тестовую сущность лайка поста.
     *
     * @param likeId  идентификатор лайка
     * @param ownerId идентификатор владельца
     * @param postId  идентификатор поста
     * @return новый экземпляр LikePost
     */
    public static LikePost createLikePostEntity(final UUID likeId,
                                                final UUID ownerId,
                                                final UUID postId) {
        final LikePost like = new LikePost();
        like.setId(likeId);
        like.setOwnerId(ownerId);
        like.setPost(createPostEntity(postId, ownerId, "Post messaging", null));
        like.setCreatedAt(LocalDateTime.now());
        return like;
    }

    /**
     * Создает тестовую сущность лайка комментария.
     *
     * @param likeId    идентификатор лайка
     * @param ownerId   идентификатор владельца
     * @param commentId идентификатор комментария
     * @return новый экземпляр LikeComment
     */
    public static LikeComment createLikeCommentEntity(final UUID likeId,
                                                      final UUID ownerId,
                                                      final UUID commentId) {
        final LikeComment like = new LikeComment();
        like.setId(likeId);
        like.setOwnerId(ownerId);
        like.setComment(createCommentEntity(commentId, ownerId, TEST_POST_ID, "Comment messaging", null));
        like.setCreatedAt(LocalDateTime.now());
        return like;
    }

    /**
     * Создает тестовый ответ поста.
     *
     * @param postId  идентификатор поста
     * @param ownerId идентификатор владельца
     * @return новый экземпляр PostResponse
     */
    public static PostResponse createPostResponse(final UUID postId,
                                                  final UUID ownerId) {
        return PostResponse.builder()
                .id(postId)
                .ownerId(ownerId)
                .content(TEST_POST_CONTENT)
                .imageUrl(TEST_IMAGE_URL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Создает тестовый ответ комментария.
     *
     * @param commentId идентификатор комментария
     * @param ownerId   идентификатор владельца
     * @param postId    идентификатор поста
     * @return новый экземпляр CommentResponse
     */
    public static CommentResponse createCommentResponse(final UUID commentId,
                                                        final UUID ownerId,
                                                        final UUID postId) {
        return CommentResponse.builder()
                .id(commentId)
                .ownerId(ownerId)
                .postId(postId)
                .content(TEST_COMMENT_CONTENT)
                .imageUrl(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Создает тестовый ответ лайка поста.
     *
     * @param likeId  идентификатор лайка
     * @param ownerId идентификатор владельца
     * @param postId  идентификатор поста
     * @return новый экземпляр LikePostResponse
     */
    public static LikePostResponse createLikePostResponse(final UUID likeId,
                                                          final UUID ownerId,
                                                          final UUID postId) {
        return LikePostResponse.builder()
                .id(likeId)
                .ownerId(ownerId)
                .postId(postId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Создает тестовый ответ лайка комментария.
     *
     * @param likeId    идентификатор лайка
     * @param ownerId   идентификатор владельца
     * @param commentId идентификатор комментария
     * @return новый экземпляр LikeCommentResponse
     */
    public static LikeCommentResponse createLikeCommentResponse(final UUID likeId,
                                                                final UUID ownerId,
                                                                final UUID commentId) {
        return LikeCommentResponse.builder()
                .id(likeId)
                .ownerId(ownerId)
                .commentId(commentId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Создает тестовый ответ деталей поста.
     *
     * @param postId идентификатор поста
     * @return новый экземпляр PostDetailsResponse
     */
    public static PostDetailsResponse createPostDetailsResponse(final UUID postId) {
        return PostDetailsResponse.builder()
                .id(postId)
                .ownerId(TEST_USER_ID)
                .content("Detailed post messaging")
                .imageUrl(TEST_IMAGE_URL)
                .commentsCount(5L)
                .likesCount(10L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Создает тестовый ответ деталей комментария.
     *
     * @param commentId идентификатор комментария
     * @return новый экземпляр CommentDetailsResponse
     */
    public static CommentDetailsResponse createCommentDetailsResponse(final UUID commentId) {
        return CommentDetailsResponse.builder()
                .id(commentId)
                .ownerId(TEST_USER_ID)
                .postId(TEST_POST_ID)
                .content("Detailed comment messaging")
                .imageUrl(null)
                .likesCount(3L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Создает тестовую страницу сущностей.
     *
     * @param <T>     тип элементов
     * @param content список элементов
     * @return страница с элементами
     */
    public static <T> Page<T> createPage(final List<T> content) {
        return new PageImpl<>(content, org.springframework.data.domain.PageRequest.of(0, 10), content.size());
    }

    /**
     * Создает тестовый ответ постраничного вывода.
     *
     * @param <T>     тип элементов
     * @param content список элементов
     * @return ответ с постраничным выводом
     */
    public static <T> PageResponse<T> createPageResponse(final List<T> content) {
        return PageResponse.<T>builder()
                .content(content)
                .currentPage(0)
                .pageSize(10)
                .totalElements((long) content.size())
                .totalPages(1)
                .first(true)
                .last(true)
                .build();
    }

    /**
     * Создает тестовое событие кэша.
     *
     * @param eventType тип события кэша
     * @param postId    идентификатор поста
     * @return новый экземпляр GenericCacheEvent
     */
    public static GenericCacheEvent createCacheEvent(final CacheEventType eventType, final UUID postId) {
        return new GenericCacheEvent(
                new Object(),
                eventType,
                postId,
                Map.of()
        );
    }

    /**
     * Создает тестовое событие создания поста.
     *
     * @param postId идентификатор поста
     * @return новое событие создания поста
     */
    public static GenericCacheEvent createPostCreatedEvent(final UUID postId) {
        return createCacheEvent(CacheEventType.POST_CREATED, postId);
    }

    /**
     * Создает тестовое событие обновления поста.
     *
     * @param postId идентификатор поста
     * @return новое событие обновления поста
     */
    public static GenericCacheEvent createPostUpdatedEvent(final UUID postId) {
        return createCacheEvent(CacheEventType.POST_UPDATED, postId);
    }

    /**
     * Создает тестовое событие добавления комментария.
     *
     * @param postId идентификатор поста
     * @return новое событие добавления комментария
     */
    public static GenericCacheEvent createCommentAddedEvent(final UUID postId) {
        return createCacheEvent(CacheEventType.COMMENT_ADDED, postId);
    }

    /**
     * Создает тестовое событие добавления лайка.
     *
     * @param postId идентификатор поста
     * @return новое событие добавления лайка
     */
    public static GenericCacheEvent createLikeAddedEvent(final UUID postId) {
        return createCacheEvent(CacheEventType.LIKE_ADDED, postId);
    }

    /**
     * Создает тестовую карту количества комментариев для постов.
     *
     * @param postIds список идентификаторов постов
     * @return карта с количеством комментариев
     */
    public static Map<UUID, Long> createCommentsCountMap(final List<UUID> postIds) {
        final Map<UUID, Long> result = new HashMap<>();
        postIds.forEach(postId -> result.put(postId, 5L));
        return result;
    }

    /**
     * Создает тестовую карту количества лайков для постов.
     *
     * @param postIds список идентификаторов постов
     * @return карта с количеством лайков
     */
    public static Map<UUID, Long> createLikesCountMap(final List<UUID> postIds) {
        final Map<UUID, Long> result = new HashMap<>();
        postIds.forEach(postId -> result.put(postId, 10L));
        return result;
    }

    /**
     * Создает тестовую карту количества лайков для комментариев.
     *
     * @param commentIds список идентификаторов комментариев
     * @return карта с количеством лайков
     */
    public static Map<UUID, Long> createCommentLikesCountMap(final List<UUID> commentIds) {
        final Map<UUID, Long> result = new HashMap<>();
        commentIds.forEach(commentId -> result.put(commentId, 3L));
        return result;
    }

    /**
     * Создает тестовую карту комментариев для постов.
     *
     * @param postIds список идентификаторов постов
     * @param limit   лимит комментариев на пост
     * @return карта с комментариями
     */
    public static Map<UUID, List<CommentResponse>> createCommentsForPostsMap(
            final List<UUID> postIds, final int limit) {
        final Map<UUID, List<CommentResponse>> result = new HashMap<>();
        postIds.forEach(postId -> {
            final List<CommentResponse> comments = new ArrayList<>();
            for (int i = 0; i < Math.min(limit, 3); i++) {
                comments.add(createCommentResponse(
                        UUID.randomUUID(),
                        TEST_USER_ID,
                        postId
                ));
            }
            result.put(postId, comments);
        });
        return result;
    }

    /**
     * Создает тестовую карту лайков для постов.
     *
     * @param postIds список идентификаторов постов
     * @param limit   лимит лайков на пост
     * @return карта с лайками
     */
    public static Map<UUID, List<LikePostResponse>> createLikesForPostsMap(
            final List<UUID> postIds, final int limit) {
        final Map<UUID, List<LikePostResponse>> result = new HashMap<>();
        postIds.forEach(postId -> {
            final List<LikePostResponse> likes = new ArrayList<>();
            for (int i = 0; i < Math.min(limit, 2); i++) {
                likes.add(createLikePostResponse(
                        UUID.randomUUID(),
                        i == 0 ? TEST_USER_ID : TEST_ANOTHER_USER_ID,
                        postId
                ));
            }
            result.put(postId, likes);
        });
        return result;
    }

    /**
     * Создает тестовую карту лайков для комментариев.
     *
     * @param commentIds список идентификаторов комментариев
     * @param limit      лимит лайков на комментарий
     * @return карта с лайками
     */
    public static Map<UUID, List<LikeCommentResponse>> createLikesForCommentsMap(
            final List<UUID> commentIds, final int limit) {
        final Map<UUID, List<LikeCommentResponse>> result = new HashMap<>();
        commentIds.forEach(commentId -> {
            final List<LikeCommentResponse> likes = new ArrayList<>();
            for (int i = 0; i < Math.min(limit, 1); i++) {
                likes.add(createLikeCommentResponse(
                        UUID.randomUUID(),
                        TEST_USER_ID,
                        commentId
                ));
            }
            result.put(commentId, likes);
        });
        return result;
    }

    /**
     * Создает тестовый список идентификаторов постов.
     *
     * @param count количество идентификаторов
     * @return список идентификаторов
     */
    public static List<UUID> createPostIds(final int count) {
        final List<UUID> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ids.add(UUID.randomUUID());
        }
        return ids;
    }

    /**
     * Создает тестовый список идентификаторов комментариев.
     *
     * @param count количество идентификаторов
     * @return список идентификаторов
     */
    public static List<UUID> createCommentIds(final int count) {
        final List<UUID> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ids.add(UUID.randomUUID());
        }
        return ids;
    }
}