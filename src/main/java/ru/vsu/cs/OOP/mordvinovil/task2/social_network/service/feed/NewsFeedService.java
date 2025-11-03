package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.feed;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.common.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.feed.NewsFeedResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.common.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;

public interface NewsFeedService {
    PageResponse<NewsFeedResponse> getPostsByFriends(User currentUser, PageRequest pageRequest);
}
