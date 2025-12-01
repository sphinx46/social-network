package ru.cs.vsu.social_network.contents_service.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.cs.vsu.social_network.contents_service.dto.request.like.LikeCommentRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.content.LikeCommentResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.contents_service.exception.comment.CommentNotFoundException;
import ru.cs.vsu.social_network.contents_service.exception.like.LikeAlreadyExistsException;
import ru.cs.vsu.social_network.contents_service.exception.handler.GlobalExceptionHandler;
import ru.cs.vsu.social_network.contents_service.service.content.LikeCommentService;
import ru.cs.vsu.social_network.contents_service.utils.TestDataFactory;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LikeCommentControllerTest extends BaseControllerTest {

    private static final UUID TEST_USER_ID = TestDataFactory.TEST_USER_ID;
    private static final UUID TEST_COMMENT_ID = TestDataFactory.TEST_COMMENT_ID;
    private static final UUID TEST_LIKE_ID = TestDataFactory.TEST_LIKE_ID;

    @Mock
    private LikeCommentService likeCommentService;

    @Override
    protected Object controllerUnderTest() {
        return new LikeCommentController(likeCommentService);
    }

    @Override
    protected Object[] controllerAdvices() {
        return new Object[]{new GlobalExceptionHandler()};
    }

    @Test
    @DisplayName("Создание лайка комментария - успешно")
    void createLikeComment_whenRequestIsValid_shouldReturnOk() throws Exception {
        final LikeCommentRequest request = TestDataFactory.createLikeCommentRequest(TEST_COMMENT_ID);
        final LikeCommentResponse response = TestDataFactory.createLikeCommentResponse(
                TEST_LIKE_ID, TEST_USER_ID, TEST_COMMENT_ID);

        when(likeCommentService.create(eq(TEST_USER_ID), any(LikeCommentRequest.class)))
                .thenReturn(response);

        mockMvcUtils.performPost("/like/comment/create", request, "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_LIKE_ID.toString()))
                .andExpect(jsonPath("$.ownerId").value(TEST_USER_ID.toString()))
                .andExpect(jsonPath("$.commentId").value(TEST_COMMENT_ID.toString()));

        verify(likeCommentService).create(eq(TEST_USER_ID), any(LikeCommentRequest.class));
    }

    @Test
    @DisplayName("Создание лайка комментария - комментарий не найден")
    void createLikeComment_whenCommentNotFound_shouldReturnNotFound() throws Exception {
        final LikeCommentRequest request = TestDataFactory.createLikeCommentRequest(TEST_COMMENT_ID);

        when(likeCommentService.create(eq(TEST_USER_ID), any(LikeCommentRequest.class)))
                .thenThrow(new CommentNotFoundException("Комментарий не найден"));

        mockMvcUtils.performPost("/like/comment/create", request, "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isNotFound());

        verify(likeCommentService).create(eq(TEST_USER_ID), any(LikeCommentRequest.class));
    }

    @Test
    @DisplayName("Создание лайка комментария - лайк уже существует")
    void createLikeComment_whenLikeAlreadyExists_shouldReturnConflict() throws Exception {
        final LikeCommentRequest request = TestDataFactory.createLikeCommentRequest(TEST_COMMENT_ID);

        when(likeCommentService.create(eq(TEST_USER_ID), any(LikeCommentRequest.class)))
                .thenThrow(new LikeAlreadyExistsException("Лайк уже существует"));

        mockMvcUtils.performPost("/like/comment/create", request, "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isConflict());

        verify(likeCommentService).create(eq(TEST_USER_ID), any(LikeCommentRequest.class));
    }

    @Test
    @DisplayName("Получение лайков комментария с пагинацией - успешно")
    void getLikesByComment_whenRequestIsValid_shouldReturnOk() throws Exception {
        final PageResponse<LikeCommentResponse> pageResponse = TestDataFactory.createPageResponse(
                List.of(TestDataFactory.createLikeCommentResponse(TEST_LIKE_ID, TEST_USER_ID, TEST_COMMENT_ID)));

        when(likeCommentService.getAllLikesByComment(eq(TEST_COMMENT_ID), any(PageRequest.class)))
                .thenReturn(pageResponse);

        mockMvcUtils.performGet("/like/comment/comment/" + TEST_COMMENT_ID +
                        "?size=10&pageNumber=0&sortedBy=createdAt&direction=DESC")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(TEST_LIKE_ID.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(likeCommentService).getAllLikesByComment(eq(TEST_COMMENT_ID), any(PageRequest.class));
    }

    @Test
    @DisplayName("Получение количества лайков комментария - успешно")
    void getLikesCountByComment_whenCommentExists_shouldReturnOk() throws Exception {
        when(likeCommentService.getLikesCountByComment(TEST_COMMENT_ID)).thenReturn(5L);

        mockMvcUtils.performGet("/like/comment/comment/" + TEST_COMMENT_ID + "/count")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));

        verify(likeCommentService).getLikesCountByComment(TEST_COMMENT_ID);
    }

    @Test
    @DisplayName("Получение количества лайков комментария - комментарий не найден")
    void getLikesCountByComment_whenCommentNotFound_shouldReturnOkWithZero() throws Exception {
        when(likeCommentService.getLikesCountByComment(TEST_COMMENT_ID)).thenReturn(0L);

        mockMvcUtils.performGet("/like/comment/comment/" + TEST_COMMENT_ID + "/count")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(0));

        verify(likeCommentService).getLikesCountByComment(TEST_COMMENT_ID);
    }

    @Test
    @DisplayName("Создание лайка комментария - отсутствует заголовок пользователя")
    void createLikeComment_whenUserHeaderMissing_shouldReturnBadRequest() throws Exception {
        final LikeCommentRequest request = TestDataFactory.createLikeCommentRequest(TEST_COMMENT_ID);

        mockMvcUtils.performPost("/like/comment/create", request)
                .andExpect(status().isBadRequest());

        verify(likeCommentService, never()).create(any(), any());
    }
}