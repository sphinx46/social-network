package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.FriendshipStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.RelationshipRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DijkstraFriendRecommendationSearchServiceImpl implements DijkstraFriendRecommendationSearchService {
    private final RelationshipRepository relationshipRepository;
    private final ConnectionStrengthCalculator strengthCalculator;

    public Map<Long, Double> findSocialDistances(Long startUserId, int maxDepth) {
        Map<Long, Double> distances = new HashMap<>();
        Set<Long> visited = new HashSet<>();
        PriorityQueue<SocialNode> pq = new PriorityQueue<>();

        distances.put(startUserId, 0.0);
        pq.offer(new SocialNode(startUserId, 0.0));

        while (!pq.isEmpty()) {
            SocialNode current = pq.poll();

            if (visited.contains(current.getUserId())) continue;
            visited.add(current.getUserId());

            if (current.getDistance() > maxDepth) continue;

            Set<Long> friends = relationshipRepository
                    .findFriendIdsByUserId(current.getUserId(), FriendshipStatus.ACCEPTED);

            for (Long friendId : friends) {
                if (visited.contains(friendId)) continue;

                double strength = strengthCalculator.calculateConnectionStrength(current.getUserId(), friendId);
                double newDistance = current.getDistance() + (1.0 - strength);

                if (!distances.containsKey(friendId) || newDistance < distances.get(friendId)) {
                    distances.put(friendId, newDistance);
                    pq.offer(new SocialNode(friendId, newDistance));
                }
            }
        }

        return distances;
    }
}