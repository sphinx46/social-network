package ru.vsu.cs.OOP.mordvinovil.task2.social_network.contoller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller.NewsFeedController;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.NewsFeedResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.CacheMode;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.factory.NewsFeedServiceFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.feed.NewsFeedService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.BaseControllerTest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NewsFeedController.class)
public class NewsFeedControllerTest extends BaseControllerTest {

    @MockitoBean
    private NewsFeedServiceFactory newsFeedServiceFactory;

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение ленты новостей с кешированием - успешно")
    void getNewsFeed_withCacheMode() throws Exception {
        List<NewsFeedResponse> newsFeedResponseList = TestDataFactory.createTestNewsFeedResponseList();
        var pageResponse = PageResponse.<NewsFeedResponse>builder()
                .content(newsFeedResponseList)
                .currentPage(0)
                .totalPages(1)
                .totalElements(10L)
                .pageSize(10)
                .first(true)
                .last(true)
                .build();

        var user = TestDataFactory.createTestUser(1L, "testUser");
        var cachingService = mock(NewsFeedService.class);

        when(userService.getCurrentUser()).thenReturn(user);
        when(newsFeedServiceFactory.getService(CacheMode.CACHE)).thenReturn(cachingService);
        when(cachingService.getPostsByFriends(any(), any())).thenReturn(pageResponse);

        mockMvcUtils.performGet("/newsfeed?size=10&pageNumber=0&cacheMode=CACHE")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.content[0].id").value(0L))
                .andExpect(jsonPath("$.content[0].author").value("username0"))
                .andExpect(jsonPath("$.content[0].postResponse.id").value(0L))
                .andExpect(jsonPath("$.content[0].postResponse.username").value("username0"))
                .andExpect(jsonPath("$.content[0].postResponse.content").value("test"))
                .andExpect(jsonPath("$.content[0].postResponse.imageUrl").value("http://example.com/image.jpg"))
                .andExpect(jsonPath("$.content[9].id").value(9L))
                .andExpect(jsonPath("$.content[9].author").value("username9"))
                .andExpect(jsonPath("$.content[9].postResponse.id").value(9L))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(10));

        verify(newsFeedServiceFactory, times(1)).getService(CacheMode.CACHE);
        verify(cachingService, times(1)).getPostsByFriends(any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение ленты новостей без кеширования - успешно")
    void getNewsFeed_withoutCacheMode() throws Exception {
        List<NewsFeedResponse> newsFeedResponseList = TestDataFactory.createTestNewsFeedResponseList();
        var pageResponse = PageResponse.<NewsFeedResponse>builder()
                .content(newsFeedResponseList)
                .currentPage(0)
                .totalPages(1)
                .totalElements(10L)
                .pageSize(10)
                .first(true)
                .last(true)
                .build();

        var user = TestDataFactory.createTestUser(1L, "testUser");
        var nonCachingService = mock(NewsFeedService.class);

        when(userService.getCurrentUser()).thenReturn(user);
        when(newsFeedServiceFactory.getService(CacheMode.NONE_CACHE)).thenReturn(nonCachingService);
        when(nonCachingService.getPostsByFriends(any(), any())).thenReturn(pageResponse);

        mockMvcUtils.performGet("/newsfeed?size=10&pageNumber=0&cacheMode=NONE_CACHE")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(10));

        verify(newsFeedServiceFactory, times(1)).getService(CacheMode.NONE_CACHE);
        verify(nonCachingService, times(1)).getPostsByFriends(any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение ленты новостей с режимом кеширования по умолчанию")
    void getNewsFeed_withDefaultCacheMode() throws Exception {
        List<NewsFeedResponse> newsFeedResponseList = TestDataFactory.createTestNewsFeedResponseList();
        var pageResponse = PageResponse.<NewsFeedResponse>builder()
                .content(newsFeedResponseList)
                .currentPage(0)
                .totalPages(1)
                .totalElements(10L)
                .pageSize(10)
                .first(true)
                .last(true)
                .build();

        var user = TestDataFactory.createTestUser(1L, "testUser");
        var nonCachingService = mock(NewsFeedService.class);

        when(userService.getCurrentUser()).thenReturn(user);
        when(newsFeedServiceFactory.getService(CacheMode.NONE_CACHE)).thenReturn(nonCachingService);
        when(nonCachingService.getPostsByFriends(any(), any())).thenReturn(pageResponse);

        mockMvcUtils.performGet("/newsfeed?size=10&pageNumber=0")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10));

        verify(newsFeedServiceFactory, times(1)).getService(CacheMode.NONE_CACHE);
        verify(nonCachingService, times(1)).getPostsByFriends(any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @DisplayName("Получение ленты новостей для неавторизованного пользователя - 401 ошибка")
    void getNewsFeed_whenUserIsNotAuth() throws Exception {
        mockMvcUtils.performGet("/newsfeed")
                .andExpect(status().isUnauthorized());

        verify(newsFeedServiceFactory, never()).getService(any());
        verify(userService, never()).getCurrentUser();
    }
}