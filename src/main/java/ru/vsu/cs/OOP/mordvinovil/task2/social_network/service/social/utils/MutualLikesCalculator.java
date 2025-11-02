package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.LikeRepository;

@Service
@RequiredArgsConstructor
public class MutualLikesCalculator {
    private final LikeRepository likeRepository;

    public int calculateMutualLikesCount(Long user1, Long user2) {
        return likeRepository.getMutualLikes(user1, user2).size();
    }
}