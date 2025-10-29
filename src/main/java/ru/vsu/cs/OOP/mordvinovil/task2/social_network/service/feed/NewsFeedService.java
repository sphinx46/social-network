package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.feed;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.NewsFeedResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;

public interface NewsFeedService {
    PageResponse<NewsFeedResponse> getPostsByFriends(User currentUser, PageRequest pageRequest);
}
