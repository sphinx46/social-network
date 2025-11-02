package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.RecommendationFriendResponse;

import java.util.List;

public interface FriendRecommendationService {
    List<RecommendationFriendResponse> getFriendRecommendations(Long targetUserId);
}
