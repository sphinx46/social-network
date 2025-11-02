package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.FriendshipStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.RelationshipRepository;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MutualFriendsCalculator {
    private final RelationshipRepository relationshipRepository;

    public int calculateMutualFriendsCount(Long user1, Long user2) {
        Set<Long> friends1 = relationshipRepository.findFriendIdsByUserId(user1, FriendshipStatus.ACCEPTED);
        Set<Long> friends2 = relationshipRepository.findFriendIdsByUserId(user2, FriendshipStatus.ACCEPTED);

        Set<Long> mutual = new HashSet<>(friends1);
        mutual.retainAll(friends2);
        return mutual.size();
    }
}