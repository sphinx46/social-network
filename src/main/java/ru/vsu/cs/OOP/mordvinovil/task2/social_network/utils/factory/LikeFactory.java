package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Comment;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Like;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.CommentNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.PostNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.CommentRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.PostRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class LikeFactory {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public Like createCommentLike(User user, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(ResponseMessageConstants.FAILURE_COMMENT_NOT_FOUND));

        return Like.builder()
                .user(user)
                .post(null)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public Like createPostLike(User user, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ResponseMessageConstants.FAILURE_POST_NOT_FOUND));

        return Like.builder()
                .user(user)
                .post(post)
                .comment(null)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public Like createCommentLike(User user, Comment comment) {
        return Like.builder()
                .user(user)
                .post(null)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public Like createPostLike(User user, Post post) {
        return Like.builder()
                .user(user)
                .post(post)
                .comment(null)
                .createdAt(LocalDateTime.now())
                .build();
    }
}