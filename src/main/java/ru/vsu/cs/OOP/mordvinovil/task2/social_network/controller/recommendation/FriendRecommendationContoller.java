package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller.recommendation;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.recommendation.RecommendationFriendResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.social.recommendation.FriendRecommendationService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.user.UserService;

import java.util.List;

@RestController
@RequestMapping("/friendRecommendations")
@RequiredArgsConstructor
public class FriendRecommendationContoller {
    private final FriendRecommendationService friendRecommendationService;
    private final UserService userService;

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение рекомендации друзей для текущего пользователя")
    @GetMapping()
    public ResponseEntity<List<RecommendationFriendResponse>> getFriendRecommendation() {
        User currentUser = userService.getCurrentUser();

        List<RecommendationFriendResponse> recommendationList =
                friendRecommendationService.getFriendRecommendations(currentUser.getId());

        return ResponseEntity.ok(recommendationList);
    }
}
