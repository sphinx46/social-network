package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller;

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
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.NewsFeedResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.CacheMode;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.NewsFeedService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.UserService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.CachingNewsFeedServiceImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.NewsFeedServiceImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/newsfeed")
@RequiredArgsConstructor
public class NewsFeedController {
    private final CachingNewsFeedServiceImpl cachingService;
    private final NewsFeedServiceImpl notCachingService;
    private final UserService userService;

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
        log.info("Получение ленты новостей для текущего пользователя. Параметры: size={}, pageNumber={}, sortedBy={}, direction={}, cacheMode={}",
                size, pageNumber, sortedBy, direction, cacheMode);

        User user = userService.getCurrentUser();
        log.debug("Текущий пользователь: id={}, username={}", user.getId(), user.getUsername());

        var pageRequest = PageRequest.builder()
                .pageNumber(pageNumber)
                .size(size)
                .sortBy(sortedBy)
                .direction(Sort.Direction.fromString(direction))
                .build();

        NewsFeedService service = resolveNewsFeedService(cacheMode);
        log.debug("Выбран сервис для обработки: {}", service.getClass().getSimpleName());

        PageResponse<NewsFeedResponse> pageResponse = service.getPostsByFriends(user, pageRequest);
        log.info("Успешно получена лента новостей для пользователя id={}. Количество элементов: {}",
                user.getId(), pageResponse.getContent().size());

        return ResponseEntity.ok(pageResponse);
    }

    private NewsFeedService resolveNewsFeedService(CacheMode cacheMode) {
        log.debug("Выбор сервиса для режима кеширования: {}", cacheMode);
        return switch (cacheMode) {
            case CacheMode.CACHE -> {
                log.debug("Выбран кеширующий сервис");
                yield cachingService;
            }
            case CacheMode.NONE_CACHE -> {
                log.debug("Выбран сервис без кеширования");
                yield notCachingService;
            }
        };
    }
}