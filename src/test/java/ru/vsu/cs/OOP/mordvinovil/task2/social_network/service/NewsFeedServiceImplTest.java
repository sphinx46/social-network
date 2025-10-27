package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.NewsFeedResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.NewsFeedRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.NewsFeedServiceImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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
        Page<Post> postPage = new PageImpl<>(postList);
        List<NewsFeedResponse> newsFeedResponseList = TestDataFactory.createTestNewsFeedResponseList();
        PageRequest pageRequest = PageRequest.builder()
                .pageNumber(0)
                .size(10)
                .sortBy("createdAt")
                .direction(org.springframework.data.domain.Sort.Direction.DESC)
                .build();

        when(newsFeedRepository.findPostsByFriends(eq(1L), any())).thenReturn(postPage);

        PageResponse<NewsFeedResponse> result = newsFeedServiceImpl.getPostsByFriends(user, pageRequest);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
    }
}