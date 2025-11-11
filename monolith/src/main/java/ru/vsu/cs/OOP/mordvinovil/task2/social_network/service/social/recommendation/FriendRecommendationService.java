package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.recommendation;

import org.springframework.data.domain.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.common.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.recommendation.RecommendationFriendResponse;

public interface FriendRecommendationService {
    PageResponse<RecommendationFriendResponse> getFriendRecommendations(Long targetUserId, PageRequest pageRequest);
}