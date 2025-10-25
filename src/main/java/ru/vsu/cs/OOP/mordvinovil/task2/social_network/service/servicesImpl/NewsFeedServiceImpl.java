package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.NewsFeedResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.NewsFeedRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.NewsFeedService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsFeedServiceImpl implements NewsFeedService {
    private final NewsFeedRepository newsFeedRepository;
    private final EntityMapper entityMapper;

    @Override
    public List<NewsFeedResponse> getPostsByFriends(User currentUser) {
        List<Post> posts = newsFeedRepository.findPostsByFriends(currentUser.getId());

        return entityMapper.mapListWithName(posts, NewsFeedResponse.class, "fullNewsFeed");
    }
}
