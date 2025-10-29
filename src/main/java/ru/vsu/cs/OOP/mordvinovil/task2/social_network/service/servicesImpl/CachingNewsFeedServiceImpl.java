package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.NewsFeedResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.NewsFeedRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.NewsFeedService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class CachingNewsFeedServiceImpl implements NewsFeedService {
    private final NewsFeedRepository newsFeedRepository;
    private final EntityMapper entityMapper;

    @Cacheable(
            value = "newsFeed",
            key = "'user:' + #currentUser.id + " +
                    "':page:' + #pageRequest.pageNumber + '" + ":size:' + " +
                    "#pageRequest.size + ':sort:' + #pageRequest.sortBy + " + "':dir:' + #pageRequest.direction")
            @Override
    public PageResponse<NewsFeedResponse> getPostsByFriends(User currentUser, PageRequest pageRequest) {
        log.info("Получение постов друзей для пользователя id={} с кешированием. Параметры страницы: page={}, size={}, sort={}",
                currentUser.getId(), pageRequest.getPageNumber(), pageRequest.getSize(), pageRequest.getSortBy());

        try {
            Page<Post> posts = newsFeedRepository.findPostsByFriends(currentUser.getId(),
                    pageRequest.toPageable());

            log.debug("Найдено {} постов для пользователя id={} из репозитория",
                    posts.getTotalElements(), currentUser.getId());

            PageResponse<NewsFeedResponse> response = PageResponse.of(posts.map(
                    post -> entityMapper.mapWithName(post, NewsFeedResponse.class, "fullNewsFeed")
            ));

            log.info("Успешно сформирован ответ с кешированием для пользователя id={}. Количество элементов на странице: {}",
                    currentUser.getId(), response.getContent().size());

            return response;
        } catch (Exception e) {
            log.error("Ошибка при получении постов друзей с кешированием для пользователя id={}: {}",
                    currentUser.getId(), e.getMessage(), e);
            throw e;
        }
    }
}

