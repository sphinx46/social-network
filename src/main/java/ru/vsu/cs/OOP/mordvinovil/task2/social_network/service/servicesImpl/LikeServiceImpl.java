package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl;

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
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.EventPublisherService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.like.LikeNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.LikeRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.LikeService;
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
    private final EventPublisherService eventPublisherService;

    @Transactional
    @Override
    public LikeCommentResponse likeComment(User currentUser, LikeCommentRequest request) {
        likeValidator.validate(request, currentUser);

        Comment comment = entityUtils.getComment(request.getCommentId());
        Long commentOwnerId = comment.getCreator().getId();

        Like like = likeFactory.createCommentLike(currentUser, request.getCommentId());
        Like savedLike = likeRepository.save(like);

        eventPublisherService.publishCommentLiked(this, commentOwnerId, like.getComment().getId(), currentUser.getId());
        return entityMapper.map(savedLike, LikeCommentResponse.class);
    }

    @Transactional
    @Override
    public LikePostResponse likePost(User currentUser, LikePostRequest request) {
        likeValidator.validate(request, currentUser);

        Post post = entityUtils.getPost(request.getPostId());
        Long postOwnerId = post.getUser().getId();

        Like like = likeFactory.createPostLike(currentUser, request.getPostId());
        Like savedLike = likeRepository.save(like);

        eventPublisherService.publishPostLiked(this, postOwnerId, like.getPost().getId(), currentUser.getId());
        return entityMapper.map(savedLike, LikePostResponse.class);
    }

    @Override
    public PageResponse<LikePostResponse> getLikesByPost(Long postId, PageRequest pageRequest) {
        Page<Like> likes = likeRepository.findByPostId(postId, pageRequest.toPageable());
        return PageResponse.of(likes.map(
                like -> entityMapper.map(like, LikePostResponse.class)
        ));
    }

    @Override
    public PageResponse<LikeCommentResponse> getLikesByComment(Long commentId, PageRequest pageRequest) {
        Page<Like> likes = likeRepository.findByCommentId(commentId, pageRequest.toPageable());
        return PageResponse.of(likes.map(
                like -> entityMapper.map(like, LikeCommentResponse.class)
        ));
    }

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

    @Transactional
    @Override
    public LikePostResponse deleteLikeByPost(User currentUser, Long postId) {
        likeValidator.validateLikeDeletion(postId, "post", currentUser);

        Like like = likeRepository.findByUserIdAndPostId(currentUser.getId(), postId)
                .orElseThrow(() -> new LikeNotFoundException(ResponseMessageConstants.FAILURE_LIKE_NOT_FOUND));

        LikePostResponse response = entityMapper.map(like, LikePostResponse.class);
        likeRepository.delete(like);
        return response;
    }
}