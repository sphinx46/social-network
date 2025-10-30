package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.content;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.LikeCommentRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.LikePostRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.LikeCommentResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.LikePostResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Comment;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Like;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.cache.CacheEventPublisherService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.notification.NotificationEventPublisherService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.like.LikeNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.LikeRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.content.LikeService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.LikeFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.LikeValidator;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {
    private final LikeRepository likeRepository;
    private final EntityMapper entityMapper;
    private final EntityUtils entityUtils;
    private final LikeFactory likeFactory;
    private final LikeValidator likeValidator;
    private final NotificationEventPublisherService notificationEventPublisherService;
    private final CacheEventPublisherService cacheEventPublisherService;

    /**
     * Ставит лайк комментарию
     *
     * @param currentUser текущий пользователь
     * @param request запрос на лайк комментария
     * @return ответ с информацией о лайке
     */
    @Transactional
    @Override
    public LikeCommentResponse likeComment(User currentUser, LikeCommentRequest request) {
        likeValidator.validate(request, currentUser);

        Comment comment = entityUtils.getComment(request.getCommentId());
        Long commentOwnerId = comment.getCreator().getId();

        Like like = likeFactory.createCommentLike(currentUser, request.getCommentId());
        Like savedLike = likeRepository.save(like);

        notificationEventPublisherService.publishCommentLiked(this, commentOwnerId, like.getComment().getId(), currentUser.getId());
        return entityMapper.map(savedLike, LikeCommentResponse.class);
    }

    /**
     * Ставит лайк посту
     *
     * @param currentUser текущий пользователь
     * @param request запрос на лайк поста
     * @return ответ с информацией о лайке
     */
    @Transactional
    @Override
    public LikePostResponse likePost(User currentUser, LikePostRequest request) {
        likeValidator.validate(request, currentUser);

        Post post = entityUtils.getPost(request.getPostId());
        Long postOwnerId = post.getUser().getId();

        Like like = likeFactory.createPostLike(currentUser, request.getPostId());
        Like savedLike = likeRepository.save(like);

        notificationEventPublisherService.publishPostLiked(this, postOwnerId, like.getPost().getId(), currentUser.getId());
        cacheEventPublisherService.publishLikedPost(this, like, post.getId(), currentUser.getId(), savedLike.getId());
        return entityMapper.map(savedLike, LikePostResponse.class);
    }

    /**
     * Получает лайки поста с пагинацией
     *
     * @param postId идентификатор поста
     * @param pageRequest параметры пагинации
     * @return страница с лайками поста
     */
    @Override
    public PageResponse<LikePostResponse> getLikesByPost(Long postId, PageRequest pageRequest) {
        Page<Like> likes = likeRepository.findByPostId(postId, pageRequest.toPageable());
        return PageResponse.of(likes.map(
                like -> entityMapper.map(like, LikePostResponse.class)
        ));
    }

    /**
     * Получает лайки комментария с пагинацией
     *
     * @param commentId идентификатор комментария
     * @param pageRequest параметры пагинации
     * @return страница с лайками комментария
     */
    @Override
    public PageResponse<LikeCommentResponse> getLikesByComment(Long commentId, PageRequest pageRequest) {
        Page<Like> likes = likeRepository.findByCommentId(commentId, pageRequest.toPageable());
        return PageResponse.of(likes.map(
                like -> entityMapper.map(like, LikeCommentResponse.class)
        ));
    }

    /**
     * Удаляет лайк с комментария
     *
     * @param currentUser текущий пользователь
     * @param commentId идентификатор комментария
     * @return ответ с информацией об удаленном лайке
     */
    @Transactional
    @Override
    public LikeCommentResponse deleteLikeByComment(User currentUser, Long commentId) {
        likeValidator.validateLikeDeletion(commentId, "comment", currentUser);

        Like like = likeRepository.findByUserIdAndCommentId(currentUser.getId(), commentId)
                .orElseThrow(() -> new LikeNotFoundException(ResponseMessageConstants.FAILURE_LIKE_NOT_FOUND));

        LikeCommentResponse response = entityMapper.map(like, LikeCommentResponse.class);
        likeRepository.delete(like);
        return response;
    }

    /**
     * Удаляет лайк с поста
     *
     * @param currentUser текущий пользователь
     * @param postId идентификатор поста
     * @return ответ с информацией об удаленном лайке
     */
    @Transactional
    @Override
    public LikePostResponse deleteLikeByPost(User currentUser, Long postId) {
        likeValidator.validateLikeDeletion(postId, "post", currentUser);

        Like like = likeRepository.findByUserIdAndPostId(currentUser.getId(), postId)
                .orElseThrow(() -> new LikeNotFoundException(ResponseMessageConstants.FAILURE_LIKE_NOT_FOUND));

        LikePostResponse response = entityMapper.map(like, LikePostResponse.class);
        likeRepository.delete(like);
        cacheEventPublisherService.publishLikeDeleted(this, like, postId, like.getId());
        return response;
    }
}