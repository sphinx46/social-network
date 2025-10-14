package ru.vsu.cs.OOP.mordvinovil.task2.social_network.contoller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller.CommentController;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.CommentService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.BaseControllerTest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
class CommentControllerIntegrationTest extends BaseControllerTest {

    @MockitoBean
    private CommentService commentService;

    @Test
    @DisplayName("Создание комментария без авторизации - должно вернуть 401")
    void createComment_whenUnAuthorized_shouldReturn401() throws Exception {
        var request = TestDataFactory.createCommentRequest();

        mockMvcUtils.performPost("/comments/create", request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Создание комментария - успешное создание комментария")
    void createComment_whenValidData_shouldCreateComment() throws Exception {
        var request = TestDataFactory.createCommentRequest();
        var response = TestDataFactory.createCommentResponse();

        when(commentService.create(any(), any())).thenReturn(response);

        mockMvcUtils.performPost("/comments/create", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("Тестовый комментарий"))
                .andExpect(jsonPath("$.imageUrl").value("http://example.com/image.jpg"));

        verify(commentService, times(1)).create(any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Создание комментария - когда сервис выбрасывает исключение")
    void createComment_whenServiceThrowsException_shouldReturnError() throws Exception {
        var request = TestDataFactory.createCommentRequest();

        when(commentService.create(any(), any()))
                .thenThrow(new RuntimeException("Ошибка создания комментария"));

        mockMvcUtils.performPost("/comments/create", request)
                .andExpect(status().isInternalServerError());

        verify(commentService, times(1)).create(any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Редактирование комментария - успешно")
    void editComment_whenRequestIsValid() throws Exception {
        var request = TestDataFactory.createCommentRequest();
        Long commentId = 1L;
        var response = TestDataFactory.createCommentResponse();
        response.setContent("Обновленный комментарий");
        response.setImageUrl("http://example.com/updated.jpg");

        when(commentService.editComment(eq(commentId), any(), any())).thenReturn(response);

        mockMvcUtils.performPut("/comments/edit/" + commentId, request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.content").value("Обновленный комментарий"))
                .andExpect(jsonPath("$.imageUrl").value("http://example.com/updated.jpg"));

        verify(commentService, times(1)).editComment(eq(commentId), any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Редактирование комментария - выбрасывает исключение, комментария не существует")
    void editComment_whenCommentIsNotExists() throws Exception {
        var request = TestDataFactory.createCommentRequest();
        Long commentId = 1L;

        when(commentService.editComment(eq(commentId), any(), any()))
                .thenThrow(new RuntimeException("Комментария не существует"));

        mockMvcUtils.performPut("/comments/edit/" + commentId, request)
                .andExpect(status().isInternalServerError());

        verify(commentService, times(1)).editComment(eq(commentId), any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Редактирование комментария - выбрасывает исключение, доступ запрещён")
    void editComment_whenIsNotOwnerComment() throws Exception {
        var request = TestDataFactory.createCommentRequest();
        Long commentId = 1L;

        when(commentService.editComment(eq(commentId), any(), any()))
                .thenThrow(new RuntimeException("Доступ запрещён"));

        mockMvcUtils.performPut("/comments/edit/" + commentId, request)
                .andExpect(status().isInternalServerError());

        verify(commentService, times(1)).editComment(eq(commentId), any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Удаление комментария - успешное удаление")
    void deleteComment_whenRequestIsValid() throws Exception {
        Long commentId = 1L;

        when(commentService.deleteComment(eq(commentId), any()))
                .thenReturn(CompletableFuture.completedFuture(true));

        mockMvcUtils.performDelete("/comments/" + commentId)
                .andExpect(status().isOk());

        verify(commentService, times(1)).deleteComment(eq(commentId), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Удаление комментария - выбрасывает исключение 'Доступ запрещён' ")
    void deleteComment_whenUserIsNotOwner() throws Exception {
        Long commentId = 1L;

        when(commentService.deleteComment(eq(commentId), any()))
                .thenThrow(new RuntimeException("Доступ запрещён"));

        mockMvcUtils.performDelete("/comments/" + commentId)
                .andExpect(status().isInternalServerError());

        verify(commentService, times(1)).deleteComment(eq(commentId), any());
        verify(userService, times(1)).getCurrentUser();
    }
}
