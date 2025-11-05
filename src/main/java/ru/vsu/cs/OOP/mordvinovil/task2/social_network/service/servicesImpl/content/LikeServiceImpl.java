package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.content;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.post.LikeCommentRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.post.LikePostRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.common.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.post.LikeCommentResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.post.LikePostResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.common.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Comment;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Like;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.cache.CacheEventPublisherService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.notification.NotificationEventPublisherService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.like.LikeNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging.CentralLogger;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.LikeRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.content.LikeService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.LikeFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.LikeValidator;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {
    private final LikeRepository likeRepository;
    private final EntityMapper entityMapper;
    private final EntityUtils entityUtils;
    private final LikeFactory likeFactory;
    private final LikeValidator likeValidator;
    private final NotificationEventPublisherService notificationEventPublisherService;
    private final CacheEventPublisherService cacheEventPublisherService;
    private final CentralLogger centralLogger;

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
        Map<String, Object> context = new HashMap<>();
        context.put("userId", currentUser.getId());
        context.put("commentId", request.getCommentId());

        centralLogger.logInfo("ЛАЙК_КОММЕНТАРИЙ_СОЗДАНИЕ",
                "Создание лайка для комментария", context);

        try {
            likeValidator.validate(request, currentUser);

            Comment comment = entityUtils.getComment(request.getCommentId());
            Long commentOwnerId = comment.getCreator().getId();

            Like like = likeFactory.createCommentLike(currentUser, request.getCommentId());
            Like savedLike = likeRepository.save(like);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("likeId", savedLike.getId());
            successContext.put("commentOwnerId", commentOwnerId);

            centralLogger.logInfo("ЛАЙК_КОММЕНТАРИЙ_СОЗДАН",
                    "Лайк для комментария успешно создан", successContext);

            notificationEventPublisherService.publishCommentLiked(this, commentOwnerId, like.getComment().getId(), currentUser.getId());
            cacheEventPublisherService.publishLikedComment(this, savedLike, comment.getId(), currentUser.getId(), savedLike.getId());
            return entityMapper.map(savedLike, LikeCommentResponse.class);
        } catch (Exception e) {
            centralLogger.logError("ЛАЙК_КОММЕНТАРИЙ_ОШИБКА_СОЗДАНИЯ",
                    "Ошибка при создании лайка для комментария", context, e);
            throw e;
        }
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
        Map<String, Object> context = new HashMap<>();
        context.put("userId", currentUser.getId());
        context.put("postId", request.getPostId());

        centralLogger.logInfo("ЛАЙК_ПОСТ_СОЗДАНИЕ",
                "Создание лайка для поста", context);

        try {
            likeValidator.validate(request, currentUser);

            Post post = entityUtils.getPost(request.getPostId());
            Long postOwnerId = post.getUser().getId();

            Like like = likeFactory.createPostLike(currentUser, request.getPostId());
            Like savedLike = likeRepository.save(like);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("likeId", savedLike.getId());
            successContext.put("postOwnerId", postOwnerId);

            centralLogger.logInfo("ЛАЙК_ПОСТ_СОЗДАН",
                    "Лайк для поста успешно создан", successContext);

            notificationEventPublisherService.publishPostLiked(this, postOwnerId, like.getPost().getId(), currentUser.getId());
            cacheEventPublisherService.publishLikedPost(this, like, post.getId(), currentUser.getId(), savedLike.getId());
            return entityMapper.map(savedLike, LikePostResponse.class);
        } catch (Exception e) {
            centralLogger.logError("ЛАЙК_ПОСТ_ОШИБКА_СОЗДАНИЯ",
                    "Ошибка при создании лайка для поста", context, e);
            throw e;
        }
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
        Map<String, Object> context = new HashMap<>();
        context.put("postId", postId);
        context.put("page", pageRequest.getPageNumber());
        context.put("size", pageRequest.getSize());

        centralLogger.logInfo("ЛАЙКИ_ПОСТ_ПОЛУЧЕНИЕ",
                "Получение лайков поста", context);

        try {
            Page<Like> likes = likeRepository.findByPostId(postId, pageRequest.toPageable());

            Map<String, Object> resultContext = new HashMap<>(context);
            resultContext.put("totalLikes", likes.getTotalElements());

            centralLogger.logInfo("ЛАЙКИ_ПОСТ_ПОЛУЧЕНЫ",
                    "Лайки поста успешно получены", resultContext);

            return PageResponse.of(likes.map(
                    like -> entityMapper.map(like, LikePostResponse.class)
            ));
        } catch (Exception e) {
            centralLogger.logError("ЛАЙКИ_ПОСТ_ОШИБКА_ПОЛУЧЕНИЯ",
                    "Ошибка при получении лайков поста", context, e);
            throw e;
        }
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
        Map<String, Object> context = new HashMap<>();
        context.put("commentId", commentId);
        context.put("page", pageRequest.getPageNumber());
        context.put("size", pageRequest.getSize());

        centralLogger.logInfo("ЛАЙКИ_КОММЕНТАРИЙ_ПОЛУЧЕНИЕ",
                "Получение лайков комментария", context);

        try {
            Page<Like> likes = likeRepository.findByCommentId(commentId, pageRequest.toPageable());

            Map<String, Object> resultContext = new HashMap<>(context);
            resultContext.put("totalLikes", likes.getTotalElements());

            centralLogger.logInfo("ЛАЙКИ_КОММЕНТАРИЙ_ПОЛУЧЕНЫ",
                    "Лайки комментария успешно получены", resultContext);

            return PageResponse.of(likes.map(
                    like -> entityMapper.map(like, LikeCommentResponse.class)
            ));
        } catch (Exception e) {
            centralLogger.logError("ЛАЙКИ_КОММЕНТАРИЙ_ОШИБКА_ПОЛУЧЕНИЯ",
                    "Ошибка при получении лайков комментария", context, e);
            throw e;
        }
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
        Map<String, Object> context = new HashMap<>();
        context.put("userId", currentUser.getId());
        context.put("commentId", commentId);

        centralLogger.logInfo("ЛАЙК_КОММЕНТАРИЙ_УДАЛЕНИЕ",
                "Удаление лайка с комментария", context);

        try {
            likeValidator.validateLikeDeletion(commentId, "comment", currentUser);

            Like like = likeRepository.findByUserIdAndCommentId(currentUser.getId(), commentId)
                    .orElseThrow(() -> new LikeNotFoundException(ResponseMessageConstants.FAILURE_LIKE_NOT_FOUND));

            LikeCommentResponse response = entityMapper.map(like, LikeCommentResponse.class);
            likeRepository.delete(like);

            centralLogger.logInfo("ЛАЙК_КОММЕНТАРИЙ_УДАЛЕН",
                    "Лайк с комментария успешно удален", context);

            return response;
        } catch (Exception e) {
            centralLogger.logError("ЛАЙК_КОММЕНТАРИЙ_ОШИБКА_УДАЛЕНИЯ",
                    "Ошибка при удалении лайка с комментария", context, e);
            throw e;
        }
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
        Map<String, Object> context = new HashMap<>();
        context.put("userId", currentUser.getId());
        context.put("postId", postId);

        centralLogger.logInfo("ЛАЙК_ПОСТ_УДАЛЕНИЕ",
                "Удаление лайка с поста", context);

        try {
            likeValidator.validateLikeDeletion(postId, "post", currentUser);

            Like like = likeRepository.findByUserIdAndPostId(currentUser.getId(), postId)
                    .orElseThrow(() -> new LikeNotFoundException(ResponseMessageConstants.FAILURE_LIKE_NOT_FOUND));

            LikePostResponse response = entityMapper.map(like, LikePostResponse.class);
            likeRepository.delete(like);

            centralLogger.logInfo("ЛАЙК_ПОСТ_УДАЛЕН",
                    "Лайк с поста успешно удален", context);

            cacheEventPublisherService.publishLikeDeleted(this, like, postId, like.getId());
            return response;
        } catch (Exception e) {
            centralLogger.logError("ЛАЙК_ПОСТ_ОШИБКА_УДАЛЕНИЯ",
                    "Ошибка при удалении лайка с поста", context, e);
            throw e;
        }
    }
}