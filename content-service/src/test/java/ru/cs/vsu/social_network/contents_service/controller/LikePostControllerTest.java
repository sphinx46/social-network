package ru.cs.vsu.social_network.contents_service.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.cs.vsu.social_network.contents_service.dto.request.like.LikePostRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.content.LikePostResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.contents_service.exception.like.LikeAlreadyExistsException;
import ru.cs.vsu.social_network.contents_service.exception.post.PostNotFoundException;
import ru.cs.vsu.social_network.contents_service.exception.handler.GlobalExceptionHandler;
import ru.cs.vsu.social_network.contents_service.service.content.LikePostService;
import ru.cs.vsu.social_network.contents_service.utils.TestDataFactory;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LikePostControllerTest extends BaseControllerTest {

    private static final UUID TEST_USER_ID = TestDataFactory.TEST_USER_ID;
    private static final UUID TEST_POST_ID = TestDataFactory.TEST_POST_ID;
    private static final UUID TEST_LIKE_ID = TestDataFactory.TEST_LIKE_ID;

    @Mock
    private LikePostService likePostService;

    @Override
    protected Object controllerUnderTest() {
        return new LikePostController(likePostService);
    }

    @Override
    protected Object[] controllerAdvices() {
        return new Object[]{new GlobalExceptionHandler()};
    }

    @Test
    @DisplayName("Создание лайка поста - успешно")
    void createLikePost_whenRequestIsValid_shouldReturnOk() throws Exception {
        final LikePostRequest request = TestDataFactory.createLikePostRequest(TEST_POST_ID);
        final LikePostResponse response = TestDataFactory.createLikePostResponse(
                TEST_LIKE_ID, TEST_USER_ID, TEST_POST_ID);

        when(likePostService.create(eq(TEST_USER_ID), any(LikePostRequest.class)))
                .thenReturn(response);

        mockMvcUtils.performPost("/like/post/create", request, "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_LIKE_ID.toString()))
                .andExpect(jsonPath("$.ownerId").value(TEST_USER_ID.toString()))
                .andExpect(jsonPath("$.postId").value(TEST_POST_ID.toString()));

        verify(likePostService).create(eq(TEST_USER_ID), any(LikePostRequest.class));
    }

    @Test
    @DisplayName("Создание лайка поста - пост не найден")
    void createLikePost_whenPostNotFound_shouldReturnNotFound() throws Exception {
        final LikePostRequest request = TestDataFactory.createLikePostRequest(TEST_POST_ID);

        when(likePostService.create(eq(TEST_USER_ID), any(LikePostRequest.class)))
                .thenThrow(new PostNotFoundException("Пост не найден"));

        mockMvcUtils.performPost("/like/post/create", request, "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isNotFound());

        verify(likePostService).create(eq(TEST_USER_ID), any(LikePostRequest.class));
    }

    @Test
    @DisplayName("Создание лайка поста - лайк уже существует")
    void createLikePost_whenLikeAlreadyExists_shouldReturnConflict() throws Exception {
        final LikePostRequest request = TestDataFactory.createLikePostRequest(TEST_POST_ID);

        when(likePostService.create(eq(TEST_USER_ID), any(LikePostRequest.class)))
                .thenThrow(new LikeAlreadyExistsException("Лайк уже существует"));

        mockMvcUtils.performPost("/like/post/create", request, "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isConflict());

        verify(likePostService).create(eq(TEST_USER_ID), any(LikePostRequest.class));
    }

    @Test
    @DisplayName("Получение лайков поста с пагинацией - успешно")
    void getLikesByPost_whenRequestIsValid_shouldReturnOk() throws Exception {
        final PageResponse<LikePostResponse> pageResponse = TestDataFactory.createPageResponse(
                List.of(TestDataFactory.createLikePostResponse(TEST_LIKE_ID, TEST_USER_ID, TEST_POST_ID)));

        when(likePostService.getAllLikesByPost(eq(TEST_POST_ID), any(PageRequest.class)))
                .thenReturn(pageResponse);

        mockMvcUtils.performGet("/like/post/post/" + TEST_POST_ID +
                        "?size=10&pageNumber=0&sortedBy=createdAt&direction=DESC")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(TEST_LIKE_ID.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(likePostService).getAllLikesByPost(eq(TEST_POST_ID), any(PageRequest.class));
    }

    @Test
    @DisplayName("Получение лайков поста с пагинацией - невалидные параметры пагинации")
    void getLikesByPost_whenInvalidPaginationParams_shouldReturnBadRequest() throws Exception {
        final PageResponse<LikePostResponse> pageResponse = TestDataFactory.createPageResponse(
                List.of(TestDataFactory.createLikePostResponse(TEST_LIKE_ID, TEST_USER_ID, TEST_POST_ID)));

        when(likePostService.getAllLikesByPost(eq(TEST_POST_ID), any(PageRequest.class)))
                .thenReturn(pageResponse);

        mockMvcUtils.performGet("/like/post/post/" + TEST_POST_ID + "?size=-1&pageNumber=-1")
                .andExpect(status().isOk());

        verify(likePostService).getAllLikesByPost(eq(TEST_POST_ID), any(PageRequest.class));
    }

    @Test
    @DisplayName("Получение количества лайков поста - успешно")
    void getLikesCountByPost_whenPostExists_shouldReturnOk() throws Exception {
        when(likePostService.getLikesCountByPost(TEST_POST_ID)).thenReturn(10L);

        mockMvcUtils.performGet("/like/post/post/" + TEST_POST_ID + "/count")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(10));

        verify(likePostService).getLikesCountByPost(TEST_POST_ID);
    }

    @Test
    @DisplayName("Получение количества лайков поста - пост не найден")
    void getLikesCountByPost_whenPostNotFound_shouldReturnOkWithZero() throws Exception {
        when(likePostService.getLikesCountByPost(TEST_POST_ID)).thenReturn(0L);

        mockMvcUtils.performGet("/like/post/post/" + TEST_POST_ID + "/count")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(0));

        verify(likePostService).getLikesCountByPost(TEST_POST_ID);
    }

    @Test
    @DisplayName("Создание лайка поста - отсутствует заголовок пользователя")
    void createLikePost_whenUserHeaderMissing_shouldReturnBadRequest() throws Exception {
        final LikePostRequest request = TestDataFactory.createLikePostRequest(TEST_POST_ID);

        mockMvcUtils.performPost("/like/post/create", request)
                .andExpect(status().isBadRequest());

        verify(likePostService, never()).create(any(), any());
    }
}