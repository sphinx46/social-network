package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.NewsFeedResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;

import java.util.List;

public interface NewsFeedService {
    List<NewsFeedResponse> getPostsByFriends(User currentUser);
}
