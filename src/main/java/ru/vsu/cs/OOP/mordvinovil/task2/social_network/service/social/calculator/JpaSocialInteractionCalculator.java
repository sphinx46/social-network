package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.calculator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.FriendshipStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.CommentRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.LikeRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.RelationshipRepository;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public final class JpaSocialInteractionCalculator implements SocialInteractionCalculator {
    private final RelationshipRepository relationshipRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    @Override
    public int calculateMutualFriendsCount(Long user1, Long user2) {
        if (user1.equals(user2)) return 0;

        Set<Long> friends1 = relationshipRepository.findFriendIdsByUserId(user1, FriendshipStatus.ACCEPTED);
        Set<Long> friends2 = relationshipRepository.findFriendIdsByUserId(user2, FriendshipStatus.ACCEPTED);

        Set<Long> mutual = new HashSet<>(friends1);
        mutual.retainAll(friends2);
        return mutual.size();
    }

    @Override
    public int calculateCommonLikesOnPostCount(Long user1, Long user2) {
        if (user1.equals(user2)) return 0;
        return likeRepository.countCommonLikes(user1, user2);
    }

    @Override
    public int calculateCommonLikesOnCommentCount(Long user1, Long user2) {
        if (user1.equals(user2)) return 0;
        return commentRepository.countCommonCommentLikes(user1, user2);
    }
}