package ru.cs.vsu.social_network.contents_service.service.serviceImpl.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.cs.vsu.social_network.contents_service.dto.request.comment.*;
import ru.cs.vsu.social_network.contents_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.content.CommentResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.contents_service.entity.Comment;
import ru.cs.vsu.social_network.contents_service.service.cache.CacheEventPublisherService;
import ru.cs.vsu.social_network.contents_service.exception.post.PostUploadImageException;
import ru.cs.vsu.social_network.contents_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.contents_service.provider.CommentEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.CommentRepository;
import ru.cs.vsu.social_network.contents_service.service.content.CommentService;
import ru.cs.vsu.social_network.contents_service.utils.MessageConstants;
import ru.cs.vsu.social_network.contents_service.utils.factory.content.CommentFactory;
import ru.cs.vsu.social_network.contents_service.validation.CommentValidator;

import java.util.UUID;

/**
 * Реализация сервиса для работы с комментариями.
 * Обеспечивает бизнес-логику создания, редактирования и управления комментариями.
 * Автоматически инвалидирует кэш при изменениях с использованием TransactionSynchronization.
 */
@Slf4j
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final EntityMapper entityMapper;
    private final CommentFactory commentFactory;
    private final CommentEntityProvider commentEntityProvider;
    private final CommentValidator commentValidator;
    private final CacheEventPublisherService cacheEventPublisherService;

    public CommentServiceImpl(final CommentRepository commentRepository,
                              final EntityMapper entityMapper,
                              final CommentFactory commentFactory,
                              final CommentEntityProvider commentEntityProvider,
                              final CommentValidator commentValidator,
                              final CacheEventPublisherService cacheEventPublisherService) {
        this.commentRepository = commentRepository;
        this.entityMapper = entityMapper;
        this.commentFactory = commentFactory;
        this.commentEntityProvider = commentEntityProvider;
        this.commentValidator = commentValidator;
        this.cacheEventPublisherService = cacheEventPublisherService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CommentResponse createComment(final UUID keycloakUserId,
                                         final CommentCreateRequest commentCreateRequest) {
        log.info("КОММЕНТАРИЙ_СЕРВИС_СОЗДАНИЕ_НАЧАЛО: " +
                        "создание комментария для пользователя: {}, " +
                        "пост: {}, длина контента: {}",
                keycloakUserId, commentCreateRequest.getPostId(),
                commentCreateRequest.getContent().length());

        final Comment comment = commentFactory.create(keycloakUserId, commentCreateRequest);
        final Comment savedComment = commentRepository.save(comment);

        cacheEventPublisherService.publishCommentCreated(this, savedComment,
                commentCreateRequest.getPostId(), savedComment.getId(), keycloakUserId);

        log.info("КОММЕНТАРИЙ_СЕРВИС_СОЗДАНИЕ_УСПЕХ: " +
                        "комментарий успешно создан с ID: {} для пользователя: {}, пост: {}",
                savedComment.getId(), keycloakUserId, commentCreateRequest.getPostId());

        return entityMapper.map(savedComment, CommentResponse.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CommentResponse editComment(final UUID keycloakUserId,
                                       final CommentEditRequest commentEditRequest) {
        log.info("КОММЕНТАРИЙ_СЕРВИС_РЕДАКТИРОВАНИЕ_НАЧАЛО: " +
                        "редактирование комментария с ID: {} пользователем: {}, " +
                        "новая длина контента: {}",
                commentEditRequest.getCommentId(),
                keycloakUserId, commentEditRequest.getContent().length());

        commentValidator.validateOwnership(keycloakUserId, commentEditRequest.getCommentId());

        final Comment comment = commentEntityProvider.getById(commentEditRequest.getCommentId());
        comment.setContent(commentEditRequest.getContent());
        final Comment updatedComment = commentRepository.save(comment);

        cacheEventPublisherService.publishCommentUpdated(this, updatedComment,
                comment.getPost().getId(), updatedComment.getId(), keycloakUserId);

        log.info("КОММЕНТАРИЙ_СЕРВИС_РЕДАКТИРОВАНИЕ_УСПЕХ: " +
                        "комментарий с ID: {} успешно обновлен пользователем: {}",
                commentEditRequest.getCommentId(), keycloakUserId);

        return entityMapper.map(updatedComment, CommentResponse.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CommentResponse deleteComment(final UUID keycloakUserId,
                                         final CommentDeleteRequest commentDeleteRequest) {
        log.info("КОММЕНТАРИЙ_СЕРВИС_УДАЛЕНИЕ_НАЧАЛО: " +
                        "удаление комментария с ID: {} пользователем: {}",
                commentDeleteRequest.getCommentId(), keycloakUserId);

        commentValidator.validateOwnership(keycloakUserId, commentDeleteRequest.getCommentId());

        final Comment comment = commentEntityProvider.getById(commentDeleteRequest.getCommentId());
        final UUID postId = comment.getPost().getId();
        commentRepository.delete(comment);

        cacheEventPublisherService.publishCommentDeleted(this, comment,
                postId, comment.getId(), keycloakUserId);

        log.info("КОММЕНТАРИЙ_СЕРВИС_УДАЛЕНИЕ_УСПЕХ: " +
                        "комментарий с ID: {} успешно удален пользователем: {}",
                commentDeleteRequest.getCommentId(), keycloakUserId);

        return entityMapper.map(comment, CommentResponse.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CommentResponse removeImage(final UUID keycloakUserId,
                                       final CommentRemoveImageRequest commentRemoveImageRequest) {
        log.info("КОММЕНТАРИЙ_СЕРВИС_УДАЛЕНИЕ_ИЗОБРАЖЕНИЯ_НАЧАЛО: " +
                        "удаление изображения у комментария с ID: {} пользователем: {}",
                commentRemoveImageRequest.getCommentId(), keycloakUserId);

        commentValidator.validateOwnership(keycloakUserId,
                commentRemoveImageRequest.getCommentId());

        final Comment comment =
                commentEntityProvider.getById(commentRemoveImageRequest.getCommentId());
        comment.setImageUrl(null);
        final Comment updatedComment = commentRepository.save(comment);

        cacheEventPublisherService.publishCommentUpdated(this, updatedComment,
                comment.getPost().getId(), updatedComment.getId(), keycloakUserId);

        log.info("КОММЕНТАРИЙ_СЕРВИС_УДАЛЕНИЕ_ИЗОБРАЖЕНИЯ_УСПЕХ: " +
                        "изображение удалено у комментария с ID: {} пользователем: {}",
                commentRemoveImageRequest.getCommentId(), keycloakUserId);

        return entityMapper.map(updatedComment, CommentResponse.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentResponse getCommentById(final UUID commentId) {
        log.info("КОММЕНТАРИЙ_СЕРВИС_ПОЛУЧЕНИЕ_ПО_ID_НАЧАЛО: " +
                "запрос комментария с ID: {}", commentId);

        final Comment comment = commentEntityProvider.getById(commentId);

        log.info("КОММЕНТАРИЙ_СЕРВИС_ПОЛУЧЕНИЕ_ПО_ID_УСПЕХ: " +
                        "комментарий с ID: {} найден, владелец: {}, пост: {}",
                commentId, comment.getOwnerId(), comment.getPost().getId());

        return entityMapper.map(comment, CommentResponse.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResponse<CommentResponse> getCommentsByPostAndOwner(final UUID keycloakUserId,
                                                                   final UUID postId,
                                                                   final PageRequest pageRequest) {
        log.info("КОММЕНТАРИЙ_СЕРВИС_ПОЛУЧЕНИЕ_ПО_ПОСТУ_И_ВЛАДЕЛЬЦУ_НАЧАЛО: " +
                        "запрос комментариев пользователя: {} для поста: {}, " +
                        "страница: {}, размер: {}, сортировка: {}",
                keycloakUserId, postId, pageRequest.getPageNumber(),
                pageRequest.getSize(), pageRequest.getSortBy());

        final Pageable pageable = pageRequest.toPageable();
        final Page<Comment> commentsPage =
                commentRepository.findAllByOwnerIdAndPostId(keycloakUserId, postId, pageable);

        log.info("КОММЕНТАРИЙ_СЕРВИС_ПОЛУЧЕНИЕ_ПО_ПОСТУ_И_ВЛАДЕЛЬЦУ_УСПЕХ: " +
                        "найдено {} комментариев пользователя: {} " +
                        "для поста: {}, всего страниц: {}",
                commentsPage.getTotalElements(),
                keycloakUserId, postId, commentsPage.getTotalPages());

        return PageResponse.of(commentsPage.map(
                comment -> entityMapper.map(comment, CommentResponse.class)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageResponse<CommentResponse> getCommentsByPost(final UUID postId,
                                                           final PageRequest pageRequest) {
        log.info("КОММЕНТАРИЙ_СЕРВИС_ПОЛУЧЕНИЕ_ПО_ПОСТУ_НАЧАЛО: " +
                        "запрос всех комментариев для поста: {}, " +
                        "страница: {}, размер: {}, сортировка: {}",
                postId, pageRequest.getPageNumber(),
                pageRequest.getSize(), pageRequest.getSortBy());

        final Pageable pageable = pageRequest.toPageable();
        final Page<Comment> commentsPage = commentRepository.findAllByPostId(postId, pageable);

        log.info("КОММЕНТАРИЙ_СЕРВИС_ПОЛУЧЕНИЕ_ПО_ПОСТУ_УСПЕХ: " +
                        "найдено {} комментариев для поста: {}, всего страниц: {}",
                commentsPage.getTotalElements(), postId, commentsPage.getTotalPages());

        return PageResponse.of(commentsPage.map(
                comment -> entityMapper.map(comment, CommentResponse.class)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public CommentResponse uploadImage(final UUID keycloakUserId,
                                       final CommentUploadImageRequest request) {
        log.info("КОММЕНТАРИЙ_СЕРВИС_ЗАГРУЗКА_ИЗОБРАЖЕНИЯ_НАЧАЛО: " +
                        "загрузка изображения для комментария с ID: {} пользователем: {}, " +
                        "URL изображения: {}",
                request.getCommentId(), keycloakUserId, request.getImageUrl());

        commentValidator.validateOwnership(keycloakUserId, request.getCommentId());

        final Comment comment = commentEntityProvider.getById(request.getCommentId());

        final String imageUrl = request.getImageUrl();
        if (!StringUtils.hasText(imageUrl)) {
            log.error("КОММЕНТАРИЙ_СЕРВИС_ЗАГРУЗКА_ИЗОБРАЖЕНИЯ_ОШИБКА: " +
                            "URL изображения пустой для комментария с ID: {}, пользователь: {}",
                    request.getCommentId(), keycloakUserId);
            throw new PostUploadImageException(MessageConstants.COMMENT_UPLOAD_IMAGE_FAILURE);
        }

        comment.setImageUrl(imageUrl);
        final Comment updatedComment = commentRepository.save(comment);

        cacheEventPublisherService.publishCommentUpdated(this, updatedComment,
                comment.getPost().getId(), updatedComment.getId(), keycloakUserId);

        log.info("КОММЕНТАРИЙ_СЕРВИС_ЗАГРУЗКА_ИЗОБРАЖЕНИЯ_УСПЕХ: " +
                        "изображение загружено для комментария с ID: {} пользователем: {}, " +
                        "URL: {}",
                request.getCommentId(), keycloakUserId, request.getImageUrl());

        return entityMapper.map(updatedComment, CommentResponse.class);
    }
}