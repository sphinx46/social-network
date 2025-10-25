package ru.vsu.cs.OOP.mordvinovil.task2.social_network.contoller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller.LikeController;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.LikeService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.BaseControllerTest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LikeController.class)
class LikeControllerTest extends BaseControllerTest {

    @MockitoBean
    private LikeService likeService;

    @Test
    @DisplayName("Лайк поста без авторизации - должно вернуть 401")
    void likePost_whenUnAuthorized_shouldReturn401() throws Exception {
        var request = TestDataFactory.createLikePostRequest();

        mockMvcUtils.performPost("/likes/post", request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Лайк поста - успешное создание")
    void likePost_whenValidData_shouldCreateLike() throws Exception {
        var request = TestDataFactory.createLikePostRequest();
        var response = TestDataFactory.createLikePostResponse();

        when(likeService.likePost(any(), any())).thenReturn(response);

        mockMvcUtils.performPost("/likes/post", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.postId").value(1L));

        verify(likeService, times(1)).likePost(any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Лайк поста - когда сервис выбрасывает исключение")
    void likePost_whenServiceThrowsException_shouldReturnError() throws Exception {
        var request = TestDataFactory.createLikePostRequest();

        when(likeService.likePost(any(), any()))
                .thenThrow(new RuntimeException("Ошибка создания лайка"));

        mockMvcUtils.performPost("/likes/post", request)
                .andExpect(status().isInternalServerError());

        verify(likeService, times(1)).likePost(any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Лайк комментария - успешное создание")
    void likeComment_whenValidData_shouldCreateLike() throws Exception {
        var request = TestDataFactory.createLikeCommentRequest();
        var response = TestDataFactory.createLikeCommentResponse();

        when(likeService.likeComment(any(), any())).thenReturn(response);

        mockMvcUtils.performPost("/likes/comment", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.commentId").value(1L));

        verify(likeService, times(1)).likeComment(any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @DisplayName("Лайк комментария без авторизации - должно вернуть 401")
    void likeComment_whenUnAuthorized_shouldReturn401() throws Exception {
        var request = TestDataFactory.createLikeCommentRequest();

        mockMvcUtils.performPost("/likes/comment", request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение лайков поста - успешно")
    void getLikesOnPost_whenRequestIsValid() throws Exception {
        var responses = List.of(TestDataFactory.createLikePostResponse());

        when(likeService.getLikesByPost(1L)).thenReturn(responses);

        mockMvcUtils.performGet("/likes/post/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].userId").value(1L))
                .andExpect(jsonPath("$[0].username").value("testUser"))
                .andExpect(jsonPath("$[0].postId").value(1L));

        verify(likeService, times(1)).getLikesByPost(1L);
        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение лайков комментария - успешно")
    void getLikesOnComment_whenRequestIsValid() throws Exception {
        var responses = List.of(TestDataFactory.createLikeCommentResponse());

        when(likeService.getLikesByComment(1L)).thenReturn(responses);

        mockMvcUtils.performGet("/likes/comment/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].userId").value(1L))
                .andExpect(jsonPath("$[0].username").value("testUser"))
                .andExpect(jsonPath("$[0].commentId").value(1L));

        verify(likeService, times(1)).getLikesByComment(1L);
        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Удаление лайка с поста - успешно")
    void deleteLikeFromPost_whenRequestIsValid() throws Exception {
        Long postId = 1L;
        var response = TestDataFactory.createLikePostResponse();

        when(likeService.deleteLikeByPost(any(), eq(postId))).thenReturn(response);

        mockMvcUtils.performDelete("/likes/post/" + postId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.postId").value(1L));

        verify(likeService, times(1)).deleteLikeByPost(any(), eq(postId));
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @DisplayName("Удаление лайка с поста без авторизации - должно вернуть 401")
    void deleteLikeFromPost_whenUnAuthorized_shouldReturn401() throws Exception {
        Long postId = 1L;

        mockMvcUtils.performDelete("/likes/post/" + postId)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Удаление лайка с комментария - успешно")
    void deleteLikeFromComment_whenRequestIsValid() throws Exception {
        Long commentId = 1L;
        var response = TestDataFactory.createLikeCommentResponse();

        when(likeService.deleteLikeByComment(any(), eq(commentId))).thenReturn(response);

        mockMvcUtils.performDelete("/likes/comment/" + commentId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.commentId").value(1L));

        verify(likeService, times(1)).deleteLikeByComment(any(), eq(commentId));
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @DisplayName("Удаление лайка с комментария без авторизации - должно вернуть 401")
    void deleteLikeFromComment_whenUnAuthorized_shouldReturn401() throws Exception {
        Long commentId = 1L;

        mockMvcUtils.performDelete("/likes/comment/" + commentId)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Удаление лайка с поста - выбрасывает исключение, лайк не существует")
    void deleteLikeFromPost_whenLikeIsNotExists() throws Exception {
        Long postId = 1L;

        when(likeService.deleteLikeByPost(any(), eq(postId)))
                .thenThrow(new RuntimeException("Лайк не существует"));

        mockMvcUtils.performDelete("/likes/post/" + postId)
                .andExpect(status().isInternalServerError());

        verify(likeService, times(1)).deleteLikeByPost(any(), eq(postId));
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Удаление лайка с комментария - выбрасывает исключение, лайк не существует")
    void deleteLikeFromComment_whenLikeIsNotExists() throws Exception {
        Long commentId = 1L;

        when(likeService.deleteLikeByComment(any(), eq(commentId)))
                .thenThrow(new RuntimeException("Лайк не существует"));

        mockMvcUtils.performDelete("/likes/comment/" + commentId)
                .andExpect(status().isInternalServerError());

        verify(likeService, times(1)).deleteLikeByComment(any(), eq(commentId));
        verify(userService, times(1)).getCurrentUser();
    }
}
