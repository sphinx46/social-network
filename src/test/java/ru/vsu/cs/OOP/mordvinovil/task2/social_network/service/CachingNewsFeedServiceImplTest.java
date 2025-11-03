package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.common.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.feed.NewsFeedResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.common.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.NewsFeedRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.feed.CachingNewsFeedServiceImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CachingNewsFeedServiceImplTest {
    @Mock
    private NewsFeedRepository newsFeedRepository;

    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private CachingNewsFeedServiceImpl cachingNewsFeedService;

    @Test
    void getPostsByFriends_whenCacheMiss() {
        User user = TestDataFactory.createTestUser(1L, "testUser", "test@example.com");
        List<Post> postList = List.of(
                TestDataFactory.createTestPost(user, "Test post 1", "image1.jpg"),
                TestDataFactory.createTestPost(user, "Test post 2", "image2.jpg")
        );
        Page<Post> postPage = new PageImpl<>(postList);

        List<NewsFeedResponse> newsFeedResponseList = TestDataFactory.createTestNewsFeedResponseList();
        PageRequest pageRequest = PageRequest.builder()
                .pageNumber(0)
                .size(10)
                .sortBy("createdAt")
                .direction(Sort.Direction.DESC)
                .build();

        when(newsFeedRepository.findPostsByFriends(eq(1L), any())).thenReturn(postPage);
        when(entityMapper.mapWithName(any(Post.class), eq(NewsFeedResponse.class), eq("fullNewsFeed")))
                .thenReturn(newsFeedResponseList.get(0), newsFeedResponseList.get(1));

        PageResponse<NewsFeedResponse> result = cachingNewsFeedService.getPostsByFriends(user, pageRequest);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(newsFeedRepository, times(1)).findPostsByFriends(eq(1L), any());
    }

    @Test
    void getPostsByFriends_whenRepositoryThrowsException() {
        User user = TestDataFactory.createTestUser(1L, "testUser", "test@example.com");
        PageRequest pageRequest = PageRequest.builder()
                .pageNumber(0)
                .size(10)
                .sortBy("createdAt")
                .direction(Sort.Direction.DESC)
                .build();

        when(newsFeedRepository.findPostsByFriends(eq(1L), any()))
                .thenThrow(new RuntimeException("Database error"));

        try {
            cachingNewsFeedService.getPostsByFriends(user, pageRequest);
        } catch (RuntimeException e) {
            assertEquals("Database error", e.getMessage());
        }

        verify(newsFeedRepository, times(1)).findPostsByFriends(eq(1L), any());
    }
}