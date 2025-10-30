package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.content;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.CommentRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.CommentResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Comment;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.cache.CacheEventPublisherService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.notification.NotificationEventPublisherService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.CommentRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.content.CommentService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.ContentFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.CommentValidator;

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

    /**
     * Создает новый комментарий к посту
     *
     * @param request запрос на создание комментария
     * @param currentUser текущий пользователь
     * @return ответ с созданным комментарием
     */
    @Transactional
    @Override
    public CommentResponse create(CommentRequest request, User currentUser) {
        commentValidator.validate(request, currentUser);

        Post post = entityUtils.getPost(request.getPostId());
        Long postOwnerId = post.getUser().getId();
        Comment comment = contentFactory.createComment(currentUser, post, request.getContent(), request.getImageUrl());
        Comment savedComment = commentRepository.save(comment);

        notificationEventPublisherService.publishCommentAdded(this, postOwnerId, post.getId(), savedComment.getId());
        cacheEventPublisherService.publishCommentCreated(this, savedComment, post.getId(), currentUser.getId(), savedComment.getId());

        return entityMapper.map(savedComment, CommentResponse.class);
    }

    /**
     * Редактирует существующий комментарий
     *
     * @param id идентификатор комментария
     * @param request запрос на редактирование
     * @param currentUser текущий пользователь
     * @return ответ с отредактированным комментарием
     */
    @Transactional
    @Override
    public CommentResponse editComment(Long id, CommentRequest request, User currentUser) {
        commentValidator.validateCommentUpdate(request, id, currentUser);

        Comment comment = entityUtils.getComment(id);

        if (request.getContent() != null) {
            comment.setContent(request.getContent());
        }

        comment.setImageUrl(request.getImageUrl());

        Comment updatedComment = commentRepository.save(comment);
        cacheEventPublisherService.publishCommentEdit(this, updatedComment, request.getPostId(), updatedComment.getId());
        return entityMapper.map(updatedComment, CommentResponse.class);
    }

    /**
     * Удаляет комментарий
     *
     * @param commentId идентификатор комментария
     * @param currentUser текущий пользователь
     * @return CompletableFuture с результатом удаления
     */
    @Transactional
    @Override
    public CompletableFuture<Boolean> deleteComment(Long commentId, User currentUser) {
        commentValidator.validateCommentOwnership(commentId, currentUser);

        Comment comment = entityUtils.getComment(commentId);
        commentRepository.delete(comment);
        cacheEventPublisherService.publishCommentDeleted(this, comment, comment.getPost().getId(), commentId);
        return CompletableFuture.completedFuture(true);
    }

    /**
     * Получает комментарий по идентификатору
     *
     * @param commentId идентификатор комментария
     * @return ответ с данными комментария
     */
    @Override
    public CommentResponse getCommentById(Long commentId) {
        Comment comment = entityUtils.getComment(commentId);
        return entityMapper.map(comment, CommentResponse.class);
    }

    /**
     * Получает все комментарии к посту с пагинацией
     *
     * @param postId идентификатор поста
     * @param pageRequest параметры пагинации
     * @return страница с комментариями к посту
     */
    @Override
    public PageResponse<CommentResponse> getAllCommentsOnPost(Long postId, PageRequest pageRequest) {
        Post post = entityUtils.getPost(postId);

        Page<Comment> commentPage = commentRepository.findByPostId(postId, pageRequest.toPageable());

        return PageResponse.of(commentPage.map(
                comment -> entityMapper.map(comment, CommentResponse.class)
        ));
    }
}