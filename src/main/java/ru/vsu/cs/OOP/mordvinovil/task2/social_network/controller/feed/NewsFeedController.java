package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller.feed;

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
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.feed.NewsFeedResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.common.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.CacheMode;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.feed.NewsFeedService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.user.UserService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.factory.NewsFeedServiceFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging.CentralLogger;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/newsfeed")
@RequiredArgsConstructor
public class NewsFeedController {
    private final NewsFeedServiceFactory newsFeedServiceFactory;
    private final UserService userService;
    private final CentralLogger centralLogger;

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение ленты новостей для текущего пользователя")
    @GetMapping()
    public ResponseEntity<PageResponse<NewsFeedResponse>> getNewsFeedByCurrentUser(
            @RequestParam(defaultValue = "1", required = false) @Min(1) Integer size,
            @RequestParam(defaultValue = "0", required = false) @Min(0) Integer pageNumber,
            @RequestParam(defaultValue = "createdAt", required = false) String sortedBy,
            @RequestParam(defaultValue = "DESC", required = false) String direction,
            @RequestParam(value = "cacheMode", defaultValue = "NONE_CACHE") CacheMode cacheMode
    ) {
        Map<String, Object> context = new HashMap<>();
        context.put("size", size);
        context.put("pageNumber", pageNumber);
        context.put("sortedBy", sortedBy);
        context.put("direction", direction);
        context.put("cacheMode", cacheMode);

        centralLogger.logInfo("ЛЕНТА_НОВОСТЕЙ_ЗАПРОС",
                "Запрос ленты новостей для текущего пользователя", context);

        try {
            User user = userService.getCurrentUser();
            context.put("userId", user.getId());
            context.put("username", user.getUsername());

            var pageRequest = PageRequest.builder()
                    .pageNumber(pageNumber)
                    .size(size)
                    .sortBy(sortedBy)
                    .direction(Sort.Direction.fromString(direction))
                    .build();

            NewsFeedService service = newsFeedServiceFactory.getService(cacheMode);
            PageResponse<NewsFeedResponse> pageResponse = service.getPostsByFriends(user, pageRequest);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("contentSize", pageResponse.getContent().size());
            successContext.put("totalElements", pageResponse.getTotalElements());

            centralLogger.logInfo("ЛЕНТА_НОВОСТЕЙ_УСПЕХ",
                    "Лента новостей успешно получена", successContext);

            return ResponseEntity.ok(pageResponse);
        } catch (Exception e) {
            centralLogger.logError("ЛЕНТА_НОВОСТЕЙ_ОШИБКА",
                    "Ошибка при получении ленты новостей", context, e);
            throw e;
        }
    }
}