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
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging.CentralLogger;

import java.util.HashMap;
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
    private final CentralLogger centralLogger;

    /**
     * Получает рекомендации друзей
     *
     * @param targetUserId идентификатор целевого пользователя
     * @param pageRequest параметры пагинации
     * @return страница с рекомендациями друзей
     */
    @Override
    @Cacheable(
            value = "friendRecommendation",
            key = "'recomm:' + #targetUserId + ':page:' + #pageRequest.pageNumber + ':size:' + #pageRequest.pageSize"
    )
    public PageResponse<RecommendationFriendResponse> getFriendRecommendations(Long targetUserId, PageRequest pageRequest) {
        Map<String, Object> context = new HashMap<>();
        context.put("targetUserId", targetUserId);
        context.put("page", pageRequest.getPageNumber());
        context.put("size", pageRequest.getPageSize());

        centralLogger.logInfo("РЕКОМЕНДАЦИИ_ДРУЗЕЙ_ПОЛУЧЕНИЕ",
                "Получение рекомендаций друзей", context);

        try {
            RecommendationStrategy strategy = strategyRegistry.getDefaultStrategy();
            Map<Long, Double> socialDistances = socialGraphService.findSocialDistances(targetUserId, 3);
            Set<Long> existingFriendIds = getExistingFriendIds(targetUserId);

            List<RecommendationFriendResponse> recommendations = strategy.generateRecommendations(targetUserId, socialDistances);

            PageResponse<RecommendationFriendResponse> result = filterExistingConnections(recommendations, targetUserId, existingFriendIds, pageRequest);

            Map<String, Object> resultContext = new HashMap<>(context);
            resultContext.put("totalRecommendations", result.getTotalElements());
            resultContext.put("filteredRecommendations", result.getContent().size());

            centralLogger.logInfo("РЕКОМЕНДАЦИИ_ДРУЗЕЙ_ПОЛУЧЕНЫ",
                    "Рекомендации друзей успешно получены", resultContext);

            return result;
        } catch (Exception e) {
            centralLogger.logError("РЕКОМЕНДАЦИИ_ДРУЗЕЙ_ОШИБКА_ПОЛУЧЕНИЯ",
                    "Ошибка при получении рекомендаций друзей", context, e);
            throw e;
        }
    }

    private Set<Long> getExistingFriendIds(Long userId) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);

        centralLogger.logInfo("СУЩЕСТВУЮЩИЕ_ДРУЗЬЯ_ПОЛУЧЕНИЕ",
                "Получение существующих друзей", context);

        try {
            Set<Long> friendIds = relationshipRepository.findFriendIdsByUserId(userId, FriendshipStatus.ACCEPTED);

            Map<String, Object> resultContext = new HashMap<>(context);
            resultContext.put("friendCount", friendIds.size());

            centralLogger.logInfo("СУЩЕСТВУЮЩИЕ_ДРУЗЬЯ_ПОЛУЧЕНЫ",
                    "Существующие друзья успешно получены", resultContext);

            return friendIds;
        } catch (Exception e) {
            centralLogger.logError("СУЩЕСТВУЮЩИЕ_ДРУЗЬЯ_ОШИБКА_ПОЛУЧЕНИЯ",
                    "Ошибка при получении существующих друзей", context, e);
            throw e;
        }
    }

    private PageResponse<RecommendationFriendResponse> filterExistingConnections(
            List<RecommendationFriendResponse> recommendations,
            Long targetUserId,
            Set<Long> existingFriendIds,
            PageRequest pageRequest) {

        Map<String, Object> context = new HashMap<>();
        context.put("targetUserId", targetUserId);
        context.put("initialRecommendations", recommendations.size());
        context.put("existingFriendIds", existingFriendIds.size());
        context.put("page", pageRequest.getPageNumber());
        context.put("size", pageRequest.getPageSize());

        centralLogger.logInfo("РЕКОМЕНДАЦИИ_ФИЛЬТРАЦИЯ",
                "Фильтрация рекомендаций", context);

        try {
            List<RecommendationFriendResponse> filteredRecommendations = recommendations.stream()
                    .filter(rec -> !rec.getRecommendedUserId().equals(targetUserId))
                    .filter(rec -> !existingFriendIds.contains(rec.getRecommendedUserId()))
                    .collect(Collectors.toList());

            int pageSize = pageRequest.getPageSize();
            int currentPage = pageRequest.getPageNumber();
            int startIndex = currentPage * pageSize;
            int endIndex = Math.min(startIndex + pageSize, filteredRecommendations.size());

            PageResponse<RecommendationFriendResponse> result;

            if (startIndex >= filteredRecommendations.size()) {
                result = PageResponse.<RecommendationFriendResponse>builder()
                        .content(List.of())
                        .currentPage(currentPage)
                        .totalPages((int) Math.ceil((double) filteredRecommendations.size() / pageSize))
                        .totalElements((long) filteredRecommendations.size())
                        .pageSize(pageSize)
                        .first(currentPage == 0)
                        .last(true)
                        .build();
            } else {
                List<RecommendationFriendResponse> paginatedContent = filteredRecommendations.subList(startIndex, endIndex);

                result = PageResponse.<RecommendationFriendResponse>builder()
                        .content(paginatedContent)
                        .currentPage(currentPage)
                        .totalPages((int) Math.ceil((double) filteredRecommendations.size() / pageSize))
                        .totalElements((long) filteredRecommendations.size())
                        .pageSize(pageSize)
                        .first(currentPage == 0)
                        .last(endIndex >= filteredRecommendations.size())
                        .build();
            }

            Map<String, Object> resultContext = new HashMap<>(context);
            resultContext.put("filteredRecommendations", filteredRecommendations.size());
            resultContext.put("finalContentSize", result.getContent().size());
            resultContext.put("totalPages", result.getTotalPages());

            centralLogger.logInfo("РЕКОМЕНДАЦИИ_ОТФИЛЬТРОВАНЫ",
                    "Рекомендации успешно отфильтрованы", resultContext);

            return result;
        } catch (Exception e) {
            centralLogger.logError("РЕКОМЕНДАЦИИ_ОШИБКА_ФИЛЬТРАЦИИ",
                    "Ошибка при фильтрации рекомендаций", context, e);
            throw e;
        }
    }
}