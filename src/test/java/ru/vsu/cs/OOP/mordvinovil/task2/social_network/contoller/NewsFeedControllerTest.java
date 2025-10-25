package ru.vsu.cs.OOP.mordvinovil.task2.social_network.contoller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller.NewsFeedController;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.NewsFeedResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.NewsFeedService;
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
    private NewsFeedService newsFeedService;

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение ленты новостей для авторизованного пользователя - успешно")
    void getNewsFeed_whenUserIsAuth() throws Exception {
        List<NewsFeedResponse> newsFeedResponseList = TestDataFactory.createTestNewsFeedResponseList();

        when(userService.getCurrentUser()).thenReturn(createTestUser(1L, "testUser"));
        when(newsFeedService.getPostsByFriends(any())).thenReturn(newsFeedResponseList);


        mockMvcUtils.performGet("/newsfeed")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(newsFeedResponseList.size()))
                .andExpect(jsonPath("$[0].id").value(0L))
                .andExpect(jsonPath("$[0].author").value("username0"))
                .andExpect(jsonPath("$[0].postResponse.id").value(0L))
                .andExpect(jsonPath("$[0].postResponse.username").value("username0"))
                .andExpect(jsonPath("$[0].postResponse.content").value("test"))
                .andExpect(jsonPath("$[0].postResponse.imageUrl").value("http://example.com/image.jpg"))
                .andExpect(jsonPath("$[9].id").value(9L))
                .andExpect(jsonPath("$[9].author").value("username9"))
                .andExpect(jsonPath("$[9].postResponse.id").value(9L));

        verify(newsFeedService, times(1)).getPostsByFriends(any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @DisplayName("Получение ленты новостей для авторизованного пользователя - 401 ошибка")
    void getNewsFeed_whenUserIsNotAuth() throws Exception {
        mockMvcUtils.performGet("/newsfeed")
                .andExpect(status().isUnauthorized());
    }
}