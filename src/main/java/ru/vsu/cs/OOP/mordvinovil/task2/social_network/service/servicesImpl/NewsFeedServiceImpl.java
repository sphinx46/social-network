package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl;

import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class NewsFeedServiceImpl implements NewsFeedService {
    private final NewsFeedRepository newsFeedRepository;
    private final EntityMapper entityMapper;

    @Override
    public PageResponse<NewsFeedResponse> getPostsByFriends(User currentUser, PageRequest pageRequest) {
        Page<Post> posts = newsFeedRepository.findPostsByFriends(currentUser.getId(),
                pageRequest.toPageable());

        return PageResponse.of(posts.map(
                post -> entityMapper.mapWithName(post, NewsFeedResponse.class, "fullNewsFeed")
        ));
    }
}
