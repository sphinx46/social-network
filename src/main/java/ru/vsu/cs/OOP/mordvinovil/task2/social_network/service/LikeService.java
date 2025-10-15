package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.LikeCommentRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.LikePostRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.LikeCommentResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.LikePostResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Comment;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Like;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.CommentNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.LikeNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.PostNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.CommentRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.LikeRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.PostRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.AccessValidator;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.LikeFactory;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final EntityMapper entityMapper;
    private final LikeFactory likeFactory;
    private final AccessValidator accessValidator;

    public LikeCommentResponse likeComment(User currentUser, LikeCommentRequest request) {
        Comment comment = getCommentEntity(request.getCommentId());

        return likeRepository.findByUserIdAndCommentId(currentUser.getId(), request.getCommentId())
                .map(existingLike -> entityMapper.map(existingLike, LikeCommentResponse.class))
                .orElseGet(() -> createNewCommentLike(currentUser, comment));
    }

    public LikePostResponse likePost(User currentUser, LikePostRequest request) {
        Post post = getPostEntity(request.getPostId());

        return likeRepository.findByUserIdAndPostId(currentUser.getId(), request.getPostId())
                .map(existingLike -> entityMapper.map(existingLike, LikePostResponse.class))
                .orElseGet(() -> createNewPostLike(currentUser, post));
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
        Like like = likeRepository.findByUserIdAndCommentId(currentUser.getId(), commentId)
                .orElseThrow(() -> new LikeNotFoundException(ResponseMessageConstants.NOT_FOUND));

        accessValidator.validateOwnership(currentUser, like.getUser()); // Добавлена проверка прав

        LikeCommentResponse response = entityMapper.map(like, LikeCommentResponse.class);
        likeRepository.delete(like);
        return response;
    }

    @Transactional
    public LikePostResponse deleteLikeByPost(User currentUser, Long postId) {
        Like like = likeRepository.findByUserIdAndPostId(currentUser.getId(), postId)
                .orElseThrow(() -> new LikeNotFoundException(ResponseMessageConstants.NOT_FOUND));

        accessValidator.validateOwnership(currentUser, like.getUser()); // Добавлена проверка прав

        LikePostResponse response = entityMapper.map(like, LikePostResponse.class);
        likeRepository.delete(like);
        return response;
    }


    private LikeCommentResponse createNewCommentLike(User currentUser, Comment comment) {
        Like like = likeFactory.createCommentLike(currentUser, comment);
        Like savedLike = likeRepository.save(like);
        return entityMapper.map(savedLike, LikeCommentResponse.class);
    }

    private LikePostResponse createNewPostLike(User currentUser, Post post) {
        Like like = likeFactory.createPostLike(currentUser, post);
        Like savedLike = likeRepository.save(like);
        return entityMapper.map(savedLike, LikePostResponse.class);
    }

    private Comment getCommentEntity(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(ResponseMessageConstants.NOT_FOUND));
    }

    private Post getPostEntity(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ResponseMessageConstants.NOT_FOUND));
    }
}