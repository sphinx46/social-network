package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.CommentRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentsCalculator {
    private final CommentRepository commentRepository;

    public int calculateCommonCommentsCount(Long user1, Long user2) {
        if (user1.equals(user2)) return 0;
        int count = commentRepository.countCommonCommentLikes(user1, user2);
        log.debug("Common comment between user {} and user {}: {}", user1, user2, count);
        return count;
    }
}