package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social;

import java.util.List;

public interface FriendRecommendationService {
    List<Recommendation> getFriendRecommendations(Long targetUserId);
}
