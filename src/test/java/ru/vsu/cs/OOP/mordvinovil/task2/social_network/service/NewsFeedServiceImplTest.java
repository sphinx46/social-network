package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.NewsFeedResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.NewsFeedRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.NewsFeedServiceImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NewsFeedServiceImplTest {
    @Mock
    private NewsFeedRepository newsFeedRepository;

    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private NewsFeedServiceImpl newsFeedServiceImpl;

    void getPostsByFriends_whenRequestIsValid() {
        var user = TestDataFactory.createTestUser();
        List<Post> postList = newsFeedRepository.findPostsByFriends(user.getId());
        List<NewsFeedResponse> newsFeedResponseList = entityMapper.mapListWithName(postList,
                NewsFeedResponse.class, "fullNewsFeed");

        when(newsFeedRepository.findPostsByFriends(anyLong())).thenReturn(postList);
        when(entityMapper.mapListWithName(postList, NewsFeedResponse.class, "fullNewsFeed")).thenReturn(newsFeedResponseList);

        List<NewsFeedResponse> result = newsFeedServiceImpl.getPostsByFriends(user);
        assertNotNull(result);
        assertEquals(newsFeedResponseList, result);
    }
}
