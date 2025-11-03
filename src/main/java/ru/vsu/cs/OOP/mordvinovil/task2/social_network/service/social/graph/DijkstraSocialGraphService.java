package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.graph;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.FriendshipStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.RelationshipRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.SocialNode;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.strength.ConnectionStrengthCalculator;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public final class DijkstraSocialGraphService implements SocialGraphService {
    private final RelationshipRepository relationshipRepository;
    private final ConnectionStrengthCalculator strengthCalculator;

    @Override
    public Map<Long, Double> findSocialDistances(Long startUserId, int maxDepth) {
        Map<Long, Double> distances = new HashMap<>();
        Set<Long> visited = new HashSet<>();
        PriorityQueue<SocialNode> pq = new PriorityQueue<>();

        distances.put(startUserId, 0.0);
        pq.offer(new SocialNode(startUserId, 0.0));

        while (!pq.isEmpty()) {
            SocialNode current = pq.poll();
            Long currentUserId = current.getUserId();

            if (visited.contains(currentUserId)) continue;
            visited.add(currentUserId);

            if (current.getDistance() >= maxDepth) continue;

            Set<Long> friends = relationshipRepository
                    .findFriendIdsByUserId(currentUserId, FriendshipStatus.ACCEPTED);

            for (Long friendId : friends) {
                if (visited.contains(friendId)) continue;

                double strength = Math.max(0.1, strengthCalculator.calculateOverallStrength(currentUserId, friendId));
                double edgeWeight = 1.0 - (strength * 0.3);
                double newDistance = current.getDistance() + edgeWeight;

                if (newDistance < distances.getOrDefault(friendId, Double.MAX_VALUE)) {
                    distances.put(friendId, newDistance);
                    pq.offer(new SocialNode(friendId, newDistance));
                }
            }
        }

        distances.remove(startUserId);
        return distances;
    }
}