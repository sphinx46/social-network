package ru.cs.vsu.social_network.contents_service.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.cs.vsu.social_network.contents_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.content.PostDetailsResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.contents_service.exception.post.PostNotFoundException;
import ru.cs.vsu.social_network.contents_service.exception.handler.GlobalExceptionHandler;
import ru.cs.vsu.social_network.contents_service.service.content.PostDetailsService;
import ru.cs.vsu.social_network.contents_service.utils.TestDataFactory;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PostDetailsControllerTest extends BaseControllerTest {

    private static final UUID TEST_USER_ID = TestDataFactory.TEST_USER_ID;
    private static final UUID TEST_POST_ID = TestDataFactory.TEST_POST_ID;

    @Mock
    private PostDetailsService postDetailsService;

    @Override
    protected Object controllerUnderTest() {
        return new PostDetailsController(postDetailsService);
    }

    @Override
    protected Object[] controllerAdvices() {
        return new Object[]{new GlobalExceptionHandler()};
    }

    @Test
    @DisplayName("Получение детальной информации о посте - успешно")
    void getPostDetails_whenPostExists_shouldReturnOk() throws Exception {
        final PostDetailsResponse response = TestDataFactory.createPostDetailsResponse(TEST_POST_ID);

        when(postDetailsService.getPostDetails(
                eq(TEST_POST_ID), anyBoolean(), anyBoolean(), anyInt(), anyInt()))
                .thenReturn(response);

        mockMvcUtils.performGet("/post-details/" + TEST_POST_ID +
                        "?includeComments=true&includeLikes=true&commentsLimit=10&likesLimit=10")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_POST_ID.toString()))
                .andExpect(jsonPath("$.ownerId").value(TEST_USER_ID.toString()))
                .andExpect(jsonPath("$.commentsCount").value(5))
                .andExpect(jsonPath("$.likesCount").value(10));

        verify(postDetailsService).getPostDetails(
                eq(TEST_POST_ID), anyBoolean(), anyBoolean(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Получение детальной информации о посте - пост не найден")
    void getPostDetails_whenPostNotFound_shouldReturnNotFound() throws Exception {
        when(postDetailsService.getPostDetails(
                eq(TEST_POST_ID), anyBoolean(), anyBoolean(), anyInt(), anyInt()))
                .thenThrow(new PostNotFoundException("Пост не найден"));

        mockMvcUtils.performGet("/post-details/" + TEST_POST_ID)
                .andExpect(status().isNotFound());

        verify(postDetailsService).getPostDetails(
                eq(TEST_POST_ID), anyBoolean(), anyBoolean(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Получение детальной информации о постах пользователя - успешно")
    void getUserPostsDetails_whenRequestIsValid_shouldReturnOk() throws Exception {
        final PageResponse<PostDetailsResponse> pageResponse = TestDataFactory.createPageResponse(
                List.of(TestDataFactory.createPostDetailsResponse(TEST_POST_ID)));

        when(postDetailsService.getUserPostsDetails(
                eq(TEST_USER_ID), any(PageRequest.class), anyBoolean(), anyBoolean(), anyInt(), anyInt()))
                .thenReturn(pageResponse);

        mockMvcUtils.performGet("/post-details/user/" + TEST_USER_ID +
                        "?size=1&pageNumber=0&sortedBy=createdAt&direction=DESC" +
                        "&includeComments=true&includeLikes=true&commentsLimit=5&likesLimit=5")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(TEST_POST_ID.toString()));

        verify(postDetailsService).getUserPostsDetails(
                eq(TEST_USER_ID), any(PageRequest.class), anyBoolean(), anyBoolean(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Получение детальной информации о всех постах - успешно")
    void getAllPostsDetails_whenRequestIsValid_shouldReturnOk() throws Exception {
        final PageResponse<PostDetailsResponse> pageResponse = TestDataFactory.createPageResponse(
                List.of(TestDataFactory.createPostDetailsResponse(TEST_POST_ID)));

        when(postDetailsService.getAllPostsDetails(
                any(PageRequest.class), anyBoolean(), anyBoolean(), anyInt(), anyInt()))
                .thenReturn(pageResponse);

        mockMvcUtils.performGet("/post-details/all" +
                        "?size=1&pageNumber=0&sortedBy=createdAt&direction=DESC" +
                        "&includeComments=true&includeLikes=true&commentsLimit=3&likesLimit=3")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(TEST_POST_ID.toString()));

        verify(postDetailsService).getAllPostsDetails(
                any(PageRequest.class), anyBoolean(), anyBoolean(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Получение детальной информации о посте - отключены опциональные данные")
    void getPostDetails_whenOptionalDataDisabled_shouldReturnOk() throws Exception {
        final PostDetailsResponse response = TestDataFactory.createPostDetailsResponse(TEST_POST_ID);

        when(postDetailsService.getPostDetails(
                eq(TEST_POST_ID), eq(false), eq(false), anyInt(), anyInt()))
                .thenReturn(response);

        mockMvcUtils.performGet("/post-details/" + TEST_POST_ID +
                        "?includeComments=false&includeLikes=false")
                .andExpect(status().isOk());

        verify(postDetailsService).getPostDetails(
                eq(TEST_POST_ID), eq(false), eq(false), anyInt(), anyInt());
    }
}