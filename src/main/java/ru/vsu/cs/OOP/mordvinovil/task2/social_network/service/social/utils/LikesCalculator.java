package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.utils;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.LikeRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikesCalculator {
    private final LikeRepository likeRepository;

    public int calculateCommonLikesCount(Long user1, Long user2) {
        if (user1.equals(user2)) return 0;
        int count = likeRepository.countCommonLikes(user1, user2);
        log.debug("Common likes between user {} and user {}: {}", user1, user2, count);
        return count;
    }
}