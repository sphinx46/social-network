package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.LikeCommentRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.LikePostRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.LikeCommentResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.LikePostResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Like;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.LikeNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.LikeRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.LikeFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.LikeValidator;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final EntityMapper entityMapper;
    private final LikeFactory likeFactory;
    private final LikeValidator likeValidator;

    public LikeCommentResponse likeComment(User currentUser, LikeCommentRequest request) {
        likeValidator.validate(request, currentUser);

        Like like = likeFactory.createCommentLike(currentUser, request.getCommentId());
        Like savedLike = likeRepository.save(like);
        return entityMapper.map(savedLike, LikeCommentResponse.class);
    }

    public LikePostResponse likePost(User currentUser, LikePostRequest request) {
        likeValidator.validate(request, currentUser);

        Like like = likeFactory.createPostLike(currentUser, request.getPostId());
        Like savedLike = likeRepository.save(like);
        return entityMapper.map(savedLike, LikePostResponse.class);
    }

    public List<LikePostResponse> getLikesByPost(Long postId) {
        List<Like> likes = likeRepository.findByPostId(postId);
        return entityMapper.mapList(likes, LikePostResponse.class);
    }

    public List<LikeCommentResponse> getLikesByComment(Long commentId) {
        List<Like> likes = likeRepository.findByCommentId(commentId);
        return entityMapper.mapList(likes, LikeCommentResponse.class);
    }

    @Transactional
    public LikeCommentResponse deleteLikeByComment(User currentUser, Long commentId) {
        likeValidator.validateLikeDeletion(commentId, "comment", currentUser);

        Like like = likeRepository.findByUserIdAndCommentId(currentUser.getId(), commentId)
                .orElseThrow(() -> new LikeNotFoundException(ResponseMessageConstants.NOT_FOUND));

        LikeCommentResponse response = entityMapper.map(like, LikeCommentResponse.class);
        likeRepository.delete(like);
        return response;
    }

    @Transactional
    public LikePostResponse deleteLikeByPost(User currentUser, Long postId) {
        likeValidator.validateLikeDeletion(postId, "post", currentUser);

        Like like = likeRepository.findByUserIdAndPostId(currentUser.getId(), postId)
                .orElseThrow(() -> new LikeNotFoundException(ResponseMessageConstants.NOT_FOUND));

        LikePostResponse response = entityMapper.map(like, LikePostResponse.class);
        likeRepository.delete(like);
        return response;
    }
}