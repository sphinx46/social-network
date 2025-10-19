package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.NewsFeedResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.UserNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.NewsFeedRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.UserRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsFeedService {
    private final NewsFeedRepository newsFeedRepository;
    private final UserRepository userRepository;
    private final EntityMapper entityMapper;

    public List<NewsFeedResponse> getPostsByFriends(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(ResponseMessageConstants.NOT_FOUND));

        List<Post> posts = newsFeedRepository.findPostsByFriends(user.getId());

        return entityMapper.mapListWithName(posts, NewsFeedResponse.class, "fullNewsFeed");
    }
}
