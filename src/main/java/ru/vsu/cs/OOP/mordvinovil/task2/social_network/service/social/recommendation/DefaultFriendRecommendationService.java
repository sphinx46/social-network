package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.recommendation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.common.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.recommendation.RecommendationFriendResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.FriendshipStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.RelationshipRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.graph.SocialGraphService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.recommendation.strategy.RecommendationStrategy;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.recommendation.strategy.RecommendationStrategyRegistry;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultFriendRecommendationService implements FriendRecommendationService {
    private final SocialGraphService socialGraphService;
    private final RelationshipRepository relationshipRepository;
    private final RecommendationStrategyRegistry strategyRegistry;

    @Override
    @Cacheable(
            value = "friendRecommendation",
            key = "'recomm:' + #targetUserId + ':page:' + #pageRequest.pageNumber + ':size:' + #pageRequest.pageSize"
    )
    public PageResponse<RecommendationFriendResponse> getFriendRecommendations(Long targetUserId, PageRequest pageRequest) {
        RecommendationStrategy strategy = strategyRegistry.getDefaultStrategy();
        Map<Long, Double> socialDistances = socialGraphService.findSocialDistances(targetUserId, 3);
        Set<Long> existingFriendIds = getExistingFriendIds(targetUserId);

        List<RecommendationFriendResponse> recommendations = strategy.generateRecommendations(targetUserId, socialDistances);

        return filterExistingConnections(recommendations, targetUserId, existingFriendIds, pageRequest);
    }

    private Set<Long> getExistingFriendIds(Long userId) {
        return relationshipRepository.findFriendIdsByUserId(userId, FriendshipStatus.ACCEPTED);
    }

    private PageResponse<RecommendationFriendResponse> filterExistingConnections(
            List<RecommendationFriendResponse> recommendations,
            Long targetUserId,
            Set<Long> existingFriendIds,
            PageRequest pageRequest) {

        List<RecommendationFriendResponse> filteredRecommendations = recommendations.stream()
                .filter(rec -> !rec.getRecommendedUserId().equals(targetUserId))
                .filter(rec -> !existingFriendIds.contains(rec.getRecommendedUserId()))
                .collect(Collectors.toList());

        int pageSize = pageRequest.getPageSize();
        int currentPage = pageRequest.getPageNumber();
        int startIndex = currentPage * pageSize;
        int endIndex = Math.min(startIndex + pageSize, filteredRecommendations.size());

        if (startIndex >= filteredRecommendations.size()) {
            return PageResponse.<RecommendationFriendResponse>builder()
                    .content(List.of())
                    .currentPage(currentPage)
                    .totalPages((int) Math.ceil((double) filteredRecommendations.size() / pageSize))
                    .totalElements((long) filteredRecommendations.size())
                    .pageSize(pageSize)
                    .first(currentPage == 0)
                    .last(true)
                    .build();
        }

        List<RecommendationFriendResponse> paginatedContent = filteredRecommendations.subList(startIndex, endIndex);

        return PageResponse.<RecommendationFriendResponse>builder()
                .content(paginatedContent)
                .currentPage(currentPage)
                .totalPages((int) Math.ceil((double) filteredRecommendations.size() / pageSize))
                .totalElements((long) filteredRecommendations.size())
                .pageSize(pageSize)
                .first(currentPage == 0)
                .last(endIndex >= filteredRecommendations.size())
                .build();
    }
}