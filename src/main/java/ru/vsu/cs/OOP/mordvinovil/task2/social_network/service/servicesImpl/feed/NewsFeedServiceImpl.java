package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.feed;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.common.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.feed.NewsFeedResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.common.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.NewsFeedRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.feed.NewsFeedService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging.CentralLogger;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NewsFeedServiceImpl implements NewsFeedService {
    private final NewsFeedRepository newsFeedRepository;
    private final EntityMapper entityMapper;
    private final CentralLogger centralLogger;

    /**
     * Получает ленту новостей с постами друзей пользователя
     *
     * @param currentUser текущий пользователь
     * @param pageRequest параметры пагинации
     * @return страница с постами друзей в ленте новостей
     */
    @Override
    public PageResponse<NewsFeedResponse> getPostsByFriends(User currentUser, PageRequest pageRequest) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", currentUser.getId());
        context.put("page", pageRequest.getPageNumber());
        context.put("size", pageRequest.getSize());
        context.put("sortBy", pageRequest.getSortBy());
        context.put("direction", pageRequest.getDirection());

        centralLogger.logInfo("ЛЕНТА_НОВОСТЕЙ_ПОЛУЧЕНИЕ",
                "Получение ленты новостей", context);

        try {
            Page<Post> posts = newsFeedRepository.findPostsByFriends(currentUser.getId(),
                    pageRequest.toPageable());

            Map<String, Object> resultContext = new HashMap<>(context);
            resultContext.put("totalPosts", posts.getTotalElements());
            resultContext.put("currentPagePosts", posts.getContent().size());

            centralLogger.logInfo("ЛЕНТА_НОВОСТЕЙ_ПОЛУЧЕНА",
                    "Лента новостей успешно получена", resultContext);

            return PageResponse.of(posts.map(
                    post -> entityMapper.mapWithName(post, NewsFeedResponse.class, "fullNewsFeed")
            ));
        } catch (Exception e) {
            centralLogger.logError("ЛЕНТА_НОВОСТЕЙ_ОШИБКА_ПОЛУЧЕНИЯ",
                    "Ошибка при получении ленты новостей", context, e);
            throw e;
        }
    }
}