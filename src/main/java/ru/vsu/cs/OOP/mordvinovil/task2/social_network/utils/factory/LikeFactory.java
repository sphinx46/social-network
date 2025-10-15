package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory;

import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Comment;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Like;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;

import java.time.LocalDateTime;

@Component
public class LikeFactory {

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