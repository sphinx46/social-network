package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller.recommendation;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.common.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.common.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.recommendation.RecommendationFriendResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.recommendation.FriendRecommendationService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.user.UserService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging.CentralLogger;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/friendRecommendations")
@RequiredArgsConstructor
public class FriendRecommendationContoller {
    private final FriendRecommendationService friendRecommendationService;
    private final UserService userService;
    private final CentralLogger centralLogger;

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение рекомендации друзей для текущего пользователя")
    @GetMapping()
    public ResponseEntity<PageResponse<RecommendationFriendResponse>> getFriendRecommendation(
            @RequestParam(defaultValue = "1", required = false) @Min(1) Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) String direction
    ) {
        Map<String, Object> context = new HashMap<>();
        context.put("size", size);
        context.put("pageNumber", pageNumber);
        context.put("sortedBy", sortedBy);
        context.put("direction", direction);

        centralLogger.logInfo("РЕКОМЕНДАЦИИ_ДРУЗЕЙ_ЗАПРОС",
                "Запрос рекомендаций друзей для текущего пользователя", context);

        try {
            User currentUser = userService.getCurrentUser();
            context.put("userId", currentUser.getId());

            var pageRequest = PageRequest.builder()
                    .pageNumber(pageNumber)
                    .size(size)
                    .sortBy(sortedBy)
                    .direction(Sort.Direction.fromString(direction))
                    .build();

            PageResponse<RecommendationFriendResponse> recommendationList =
                    friendRecommendationService.getFriendRecommendations(currentUser.getId(), pageRequest.toPageable());

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("contentSize", recommendationList.getContent().size());
            successContext.put("totalElements", recommendationList.getTotalElements());

            centralLogger.logInfo("РЕКОМЕНДАЦИИ_ДРУЗЕЙ_ПОЛУЧЕНЫ",
                    "Рекомендации друзей успешно получены", successContext);

            return ResponseEntity.ok(recommendationList);
        } catch (Exception e) {
            centralLogger.logError("РЕКОМЕНДАЦИИ_ДРУЗЕЙ_ОШИБКА",
                    "Ошибка при получении рекомендаций друзей", context, e);
            throw e;
        }
    }
}