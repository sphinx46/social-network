package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.FriendshipStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.RelationshipRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendRecommendationServiceImpl implements FriendRecommendationService {
    private final DijkstraFriendRecommendationSearchService searchService;
    private final RelationshipRepository relationshipRepository;

    @Override
    public List<Recommendation> getFriendRecommendations(Long targetUserId) {
        Map<Long, Double> socialDistances = searchService.findSocialDistances(targetUserId, 3);

        Set<Long> existingFriendIds = relationshipRepository
                .findFriendIdsByUserId(targetUserId, FriendshipStatus.ACCEPTED);

        List<Recommendation> recommendations = socialDistances.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(targetUserId))
                .filter(entry -> !existingFriendIds.contains(entry.getKey()))
                .map(entry -> new Recommendation(
                        entry.getKey(),
                        1.0 / (1.0 + entry.getValue()),
                        LocalDateTime.now()
                ))
                .sorted(Comparator.comparing(Recommendation::getScore).reversed())
                .limit(10)
                .collect(Collectors.toList());

        return recommendations;
    }
}