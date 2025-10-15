package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory;

import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Comment;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;

import java.time.LocalDateTime;

@Component
public class ContentFactory {

    public Post createPost(User user, String content, String imageUrl) {
        return Post.builder()
                .user(user)
                .content(content)
                .imageUrl(imageUrl)
                .time(LocalDateTime.now())
                .build();
    }

    public Comment createComment(User creator, Post post, String content, String imageUrl) {
        return Comment.builder()
                .post(post)
                .creator(creator)
                .content(content)
                .imageUrl(imageUrl)
                .time(LocalDateTime.now())
                .build();
    }
}