package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.content;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.post.CommentRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.common.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.post.CommentResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.common.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Comment;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.cache.CacheEventPublisherService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.notification.NotificationEventPublisherService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging.Logger;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.CommentRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.content.CommentService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.ContentFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.CommentValidator;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final EntityMapper entityMapper;
    private final ContentFactory contentFactory;
    private final CommentValidator commentValidator;
    private final EntityUtils entityUtils;
    private final NotificationEventPublisherService notificationEventPublisherService;
    private final CacheEventPublisherService cacheEventPublisherService;
    private final Logger logger;

    @Transactional
    @Override
    public CommentResponse create(CommentRequest request, User currentUser) {
        Map<String, Object> context = Map.of(
                "userId", currentUser.getId(),
                "postId", request.getPostId(),
                "contentLength", request.getContent() != null ? request.getContent().length() : 0
        );

        logger.logInfo("КОММЕНТАРИЙ_СОЗДАНИЕ", "Создание комментария к посту", context);

        try {
            commentValidator.validate(request, currentUser);

            Post post = entityUtils.getPost(request.getPostId());
            Long postOwnerId = post.getUser().getId();
            Comment comment = contentFactory.createComment(currentUser, post, request.getContent(), request.getImageUrl());
            Comment savedComment = commentRepository.save(comment);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("commentId", savedComment.getId());
            successContext.put("postOwnerId", postOwnerId);

            logger.logInfo("КОММЕНТАРИЙ_СОЗДАН", "Комментарий успешно создан", successContext);

            notificationEventPublisherService.publishCommentAdded(this, postOwnerId, post.getId(), savedComment.getId());
            cacheEventPublisherService.publishCommentCreated(this, savedComment, post.getId(), currentUser.getId(), savedComment.getId());

            return entityMapper.mapWithName(savedComment, CommentResponse.class, "withLikes");
        } catch (Exception e) {
            logger.logError("КОММЕНТАРИЙ_ОШИБКА_СОЗДАНИЯ", "Ошибка при создании комментария", context, e);
            throw e;
        }
    }

    @Transactional
    @Override
    public CommentResponse editComment(Long id, CommentRequest request, User currentUser) {
        Map<String, Object> context = Map.of(
                "commentId", id,
                "userId", currentUser.getId(),
                "postId", request.getPostId()
        );

        logger.logInfo("КОММЕНТАРИЙ_РЕДАКТИРОВАНИЕ", "Редактирование комментария", context);

        try {
            commentValidator.validateCommentUpdate(request, id, currentUser);

            Comment comment = entityUtils.getComment(id);

            if (request.getContent() != null) {
                comment.setContent(request.getContent());
            }

            comment.setImageUrl(request.getImageUrl());

            Comment updatedComment = commentRepository.save(comment);

            logger.logInfo("КОММЕНТАРИЙ_ОБНОВЛЕН", "Комментарий успешно обновлен", context);

            cacheEventPublisherService.publishCommentEdit(this, updatedComment, request.getPostId(), updatedComment.getId());
            return entityMapper.mapWithName(updatedComment, CommentResponse.class, "withLikes");
        } catch (Exception e) {
            logger.logError("КОММЕНТАРИЙ_ОШИБКА_РЕДАКТИРОВАНИЯ", "Ошибка при редактировании комментария", context, e);
            throw e;
        }
    }

    @Transactional
    @Override
    public CompletableFuture<Boolean> deleteComment(Long commentId, User currentUser) {
        Map<String, Object> context = Map.of(
                "commentId", commentId,
                "userId", currentUser.getId()
        );

        logger.logInfo("КОММЕНТАРИЙ_УДАЛЕНИЕ", "Удаление комментария", context);

        try {
            commentValidator.validateCommentOwnership(commentId, currentUser);

            Comment comment = entityUtils.getComment(commentId);
            commentRepository.delete(comment);

            logger.logInfo("КОММЕНТАРИЙ_УДАЛЕН", "Комментарий успешно удален", context);

            cacheEventPublisherService.publishCommentDeleted(this, comment, comment.getPost().getId(), commentId);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            logger.logError("КОММЕНТАРИЙ_ОШИБКА_УДАЛЕНИЯ", "Ошибка при удалении комментария", context, e);
            throw e;
        }
    }

    @Override
    public CommentResponse getCommentById(Long commentId) {
        Map<String, Object> context = Map.of("commentId", commentId);
        logger.logInfo("КОММЕНТАРИЙ_ПОЛУЧЕНИЕ", "Получение комментария по ID", context);

        try {
            Comment comment = entityUtils.getComment(commentId);
            return entityMapper.mapWithName(comment, CommentResponse.class, "withLikes");
        } catch (Exception e) {
            logger.logError("КОММЕНТАРИЙ_ОШИБКА_ПОЛУЧЕНИЯ", "Ошибка при получении комментария", context, e);
            throw e;
        }
    }

    @Override
    public PageResponse<CommentResponse> getAllCommentsOnPost(Long postId, PageRequest pageRequest) {
        Map<String, Object> context = Map.of(
                "postId", postId,
                "page", pageRequest.getPageNumber(),
                "size", pageRequest.getSize()
        );

        logger.logInfo("КОММЕНТАРИИ_ПОСТ_ПОЛУЧЕНИЕ", "Получение комментариев к посту", context);

        try {
            Post post = entityUtils.getPost(postId);
            Page<Comment> commentPage = commentRepository.findByPostIdWithLikes(postId, pageRequest.toPageable());

            Page<CommentResponse> responsePage = commentPage.map(comment -> {
                CommentResponse response = entityMapper.mapWithName(comment, CommentResponse.class, "withLikes");
                return response;
            });

            Map<String, Object> resultContext = new HashMap<>(context);
            resultContext.put("totalComments", commentPage.getTotalElements());

            logger.logInfo("КОММЕНТАРИИ_ПОСТ_ПОЛУЧЕНЫ", "Комментарии к посту успешно получены", resultContext);

            return PageResponse.of(responsePage);
        } catch (Exception e) {
            logger.logError("КОММЕНТАРИИ_ОШИБКА_ПОЛУЧЕНИЯ", "Ошибка при получении комментариев к посту", context, e);
            throw e;
        }
    }
}