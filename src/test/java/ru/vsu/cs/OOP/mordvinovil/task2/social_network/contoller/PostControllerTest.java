package ru.vsu.cs.OOP.mordvinovil.task2.social_network.contoller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller.PostController;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PostResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.PostService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.BaseControllerTest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
public class PostControllerTest extends BaseControllerTest {

    @MockitoBean
    private PostService postService;

    @Test
    @DisplayName("Создание поста без авторизации - должно вернуть 401")
    void createPost_whenUnAuthorized_shouldReturn401() throws Exception {
        var request = TestDataFactory.createPostRequest();

        mockMvcUtils.performPost("/posts/create", request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Создание поста - успешное создание")
    @WithMockUser(username = "testUser", authorities = "USER")
    void createPost_whenRequestIsValid() throws Exception {
        var request = TestDataFactory.createPostRequest();
        var response = TestDataFactory.createPostResponse();

        when(postService.create(any(), any())).thenReturn(response);

        mockMvcUtils.performPost("/posts/create", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("test"))
                .andExpect(jsonPath("$.imageUrl").value("http://example.com/image.jpg"));

        verify(postService, times(1)).create(any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Создание поста - когда сервис выбрасывает исключение")
    void createPost_whenServiceThrowsException_shouldReturnError() throws Exception {
        var request = TestDataFactory.createPostRequest();

        when(postService.create(any(), any()))
                .thenThrow(new RuntimeException("Ошибка создания поста"));

        mockMvcUtils.performPost("/posts/create", request)
                .andExpect(status().isInternalServerError());

        verify(postService, times(1)).create(any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Редактирование поста - успешно")
    void editPost_whenRequestIsValid() throws Exception {
        var request = TestDataFactory.createPostRequest();
        Long postId = 1L;
        var response = TestDataFactory.createPostResponse();
        response.setContent("Обновленный пост");
        response.setImageUrl("http://example.com/updated.jpg");

        when(postService.editPost(any(), eq(postId), any())).thenReturn(response);

        mockMvcUtils.performPut("/posts/edit/" + postId, request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.content").value("Обновленный пост"))
                .andExpect(jsonPath("$.imageUrl").value("http://example.com/updated.jpg"));

        verify(postService, times(1)).editPost(any(), eq(postId), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Редактирование поста - выбрасывает исключение, поста не существует")
    void editPost_whenPostIsNotExists() throws Exception {
        var request = TestDataFactory.createPostRequest();
        Long postId = 1L;

        when(postService.editPost(any(), eq(postId), any()))
                .thenThrow(new RuntimeException("Поста не существует"));

        mockMvcUtils.performPut("/posts/edit/" + postId, request)
                .andExpect(status().isInternalServerError());

        verify(postService, times(1)).editPost(any(), eq(postId), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Загрузка изображения - успешная загрузка")
    void uploadImage_whenRequestIsValid() throws Exception {
        Long postId = 1L;
        var response = TestDataFactory.createPostResponse();
        response.setImageUrl("http://example.com/updated_image.jpg");

        when(postService.uploadImage(eq(postId), any(), any())).thenReturn(response);

        var file = mockMvcUtils.createMockImageFile();

        mockMvcUtils.performMultipart("/posts/" + postId + "/image", file)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.content").value("test"))
                .andExpect(jsonPath("$.imageUrl").value("http://example.com/updated_image.jpg"));

        verify(postService, times(1)).uploadImage(eq(postId), any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение постов текущего пользователя - успешно")
    void getPostsByCurrentUser_whenRequestIsValid() throws Exception {
        var postResponse = TestDataFactory.createPostResponse();
        var pageResponse = PageResponse.<PostResponse>builder()
                .content(List.of(postResponse))
                .totalPages(1)
                .totalElements(1L)
                .pageSize(10)
                .currentPage(0)
                .build();

        when(postService.getAllPostsByUser(any(), any())).thenReturn(pageResponse);

        mockMvcUtils.performGet("/posts/me?size=10&pageNumber=0&sortedBy=createdAt&direction=DESC")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].username").value("testUser"))
                .andExpect(jsonPath("$.content[0].content").value("test"))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.currentPage").value(0));

        verify(postService, times(1)).getAllPostsByUser(any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @DisplayName("Получение постов текущего пользователя без авторизации - должно вернуть 401")
    void getPostsByCurrentUser_whenUnAuthorized_shouldReturn401() throws Exception {
        mockMvcUtils.performGet("/posts/me")
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Удаление изображения поста - успешно")
    void removeImage_whenRequestIsValid() throws Exception {
        Long postId = 1L;
        var response = TestDataFactory.createPostResponse();
        response.setImageUrl(null);

        when(postService.removeImage(eq(postId), any())).thenReturn(response);

        mockMvcUtils.performDelete("/posts/" + postId + "/image")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId))
                .andExpect(jsonPath("$.imageUrl").isEmpty());

        verify(postService, times(1)).removeImage(eq(postId), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @DisplayName("Удаление изображения поста без авторизации - должно вернуть 401")
    void removeImage_whenUnAuthorized_shouldReturn401() throws Exception {
        Long postId = 1L;

        mockMvcUtils.performDelete("/posts/" + postId + "/image")
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение поста по ID - успешно")
    void getPostById_whenRequestIsValid() throws Exception {
        Long postId = 1L;
        var response = TestDataFactory.createPostResponse();

        when(postService.getPostById(postId)).thenReturn(response);

        mockMvcUtils.performGet("/posts/" + postId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.content").value("test"));

        verify(postService, times(1)).getPostById(postId);
        verifyNoInteractions(userService);
    }
}