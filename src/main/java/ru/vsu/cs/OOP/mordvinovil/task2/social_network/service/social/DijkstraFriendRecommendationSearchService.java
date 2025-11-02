package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social;

import java.util.Map;

public interface DijkstraFriendRecommendationSearchService {
    Map<Long, Double> findSocialDistances(Long startUserId, int maxDepth);
}
