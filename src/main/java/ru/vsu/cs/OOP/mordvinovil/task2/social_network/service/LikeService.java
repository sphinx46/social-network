package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ModelMapper modelMapper;

    public LikeCommentResponse likeComment(User currentUser, LikeCommentRequest request) {
        Comment comment = commentRepository.findById(request.getCommentId())
                .orElseThrow(() -> new CommentNotFoundException(ResponseMessageConstants.NOT_FOUND));

        Optional<Like> existingLike = likeRepository.findByUserIdAndCommentId(currentUser.getId(), request.getCommentId());
        if (existingLike.isPresent()) {
            return modelMapper.map(existingLike.get(), LikeCommentResponse.class);
        }

        var like = Like.builder()
                .user(currentUser)
                .post(null)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .build();

        Like savedLike = likeRepository.save(like);
        return modelMapper.map(savedLike, LikeCommentResponse.class);
    }

    public LikePostResponse likePost(User currentUser, LikePostRequest request) {
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new PostNotFoundException(ResponseMessageConstants.NOT_FOUND));

        Optional<Like> existingLike = likeRepository.findByUserIdAndPostId(currentUser.getId(), request.getPostId());
        if (existingLike.isPresent()) {
            return modelMapper.map(existingLike.get(), LikePostResponse.class);
        }

        var like = Like.builder()
                .user(currentUser)
                .post(post)
                .comment(null)
                .createdAt(LocalDateTime.now())
                .build();

        Like savedLike = likeRepository.save(like);
        return modelMapper.map(savedLike, LikePostResponse.class);
    }

    public List<LikePostResponse> getLikesByPost(Long postId) {
        List<Like> likes = likeRepository.findByPostId(postId);
        return likes.stream()
                .map(like -> modelMapper.map(like, LikePostResponse.class))
                .toList();
    }

    public List<LikeCommentResponse> getLikesByComment(Long commentId) {
        List<Like> likes = likeRepository.findByCommentId(commentId);
        return likes.stream()
                .map(like -> modelMapper.map(like, LikeCommentResponse.class))
                .toList();
    }

    @Transactional
    public LikeCommentResponse deleteLikeByComment(User currentUser, Long commentId) {
        Like like = likeRepository.findByUserIdAndCommentId(currentUser.getId(), commentId)
                .orElseThrow(() -> new LikeNotFoundException(ResponseMessageConstants.NOT_FOUND));

        LikeCommentResponse response = modelMapper.map(like, LikeCommentResponse.class);
        likeRepository.delete(like);
        return response;
    }

    @Transactional
    public LikePostResponse deleteLikeByPost(User currentUser, Long postId) {
        Like like = likeRepository.findByUserIdAndPostId(currentUser.getId(), postId)
                .orElseThrow(() -> new LikeNotFoundException(ResponseMessageConstants.NOT_FOUND));

        LikePostResponse response = modelMapper.map(like, LikePostResponse.class);
        likeRepository.delete(like);
        return response;
    }
}