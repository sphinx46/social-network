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
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.feed.NewsFeedServiceImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory.createTestNewsFeedResponseList;

@ExtendWith(MockitoExtension.class)
public class NewsFeedServiceImplTest {
    @Mock
    private NewsFeedRepository newsFeedRepository;

    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private NewsFeedServiceImpl newsFeedServiceImpl;

    @Test
    void getPostsByFriends_whenRequestIsValid() {
        User user = TestDataFactory.createTestUser(1L, "testUser", "test@example.com");
        List<Post> postList = List.of(
                TestDataFactory.createTestPost(user, "Test post 1", "image1.jpg"),
                TestDataFactory.createTestPost(user, "Test post 2", "image2.jpg")
        );

        Page<Post> postPage = new PageImpl<>(
                postList,
                org.springframework.data.domain.PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")),
                postList.size()
        );

        List<NewsFeedResponse> newsFeedResponseList = createTestNewsFeedResponseList();

        PageRequest pageRequest = PageRequest.builder()
                .pageNumber(0)
                .size(10)
                .sortBy("createdAt")
                .direction(Sort.Direction.DESC)
                .build();

        when(newsFeedRepository.findPostsByFriends(eq(1L), any(org.springframework.data.domain.PageRequest.class))).thenReturn(postPage);
        when(entityMapper.mapWithName(any(Post.class), eq(NewsFeedResponse.class), eq("fullNewsFeed")))
                .thenReturn(newsFeedResponseList.get(0), newsFeedResponseList.get(1));

        PageResponse<NewsFeedResponse> result = newsFeedServiceImpl.getPostsByFriends(user, pageRequest);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getCurrentPage());
        assertEquals(10, result.getPageSize());
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
    }

    @Test
    void getPostsByFriends_whenEmptyResult() {
        User user = TestDataFactory.createTestUser(1L, "testUser", "test@example.com");
        Page<Post> emptyPage = new PageImpl<>(
                List.of(),
                org.springframework.data.domain.PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")),
                0
        );
        PageRequest pageRequest = PageRequest.builder()
                .pageNumber(0)
                .size(10)
                .sortBy("createdAt")
                .direction(Sort.Direction.DESC)
                .build();

        when(newsFeedRepository.findPostsByFriends(eq(1L), any(org.springframework.data.domain.PageRequest.class))).thenReturn(emptyPage);

        PageResponse<NewsFeedResponse> result = newsFeedServiceImpl.getPostsByFriends(user, pageRequest);

        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        assertEquals(0, result.getTotalElements());
        assertEquals(10, result.getPageSize());
        assertEquals(0, result.getCurrentPage());
        assertEquals(0, result.getTotalPages());
        assertEquals(true, result.isFirst());
        assertEquals(true, result.isLast());
    }
}

