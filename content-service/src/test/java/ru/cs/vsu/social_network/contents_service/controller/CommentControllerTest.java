package ru.cs.vsu.social_network.contents_service.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import ru.cs.vsu.social_network.contents_service.dto.request.comment.CommentCreateRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.comment.CommentEditRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.comment.CommentRemoveImageRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.content.CommentResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.contents_service.exception.comment.CommentNotFoundException;
import ru.cs.vsu.social_network.contents_service.exception.handler.GlobalExceptionHandler;
import ru.cs.vsu.social_network.contents_service.exception.post.PostNotFoundException;
import ru.cs.vsu.social_network.contents_service.service.content.CommentService;
import ru.cs.vsu.social_network.contents_service.utils.TestDataFactory;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest extends BaseControllerTest {

    private static final UUID TEST_USER_ID = TestDataFactory.TEST_USER_ID;
    private static final UUID TEST_POST_ID = TestDataFactory.TEST_POST_ID;
    private static final UUID TEST_COMMENT_ID = TestDataFactory.TEST_COMMENT_ID;
    private static final UUID TEST_ANOTHER_USER_ID = TestDataFactory.TEST_ANOTHER_USER_ID;

    @Mock
    private CommentService commentService;

    @Override
    protected Object controllerUnderTest() {
        return new CommentController(commentService);
    }

    @Override
    protected Object[] controllerAdvices() {
        return new Object[]{new GlobalExceptionHandler()};
    }

    @Test
    @DisplayName("Создание комментария - успешно")
    void createComment_whenRequestIsValid_shouldReturnOk() throws Exception {
        final CommentCreateRequest request = TestDataFactory.createCommentCreateRequest(TEST_POST_ID, "Test comment");
        final CommentResponse response = TestDataFactory.createCommentResponse(
                TEST_COMMENT_ID, TEST_USER_ID, TEST_POST_ID);

        when(commentService.createComment(eq(TEST_USER_ID), any(CommentCreateRequest.class)))
                .thenReturn(response);

        mockMvcUtils.performPost("/comment/create", request, "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_COMMENT_ID.toString()))
                .andExpect(jsonPath("$.ownerId").value(TEST_USER_ID.toString()))
                .andExpect(jsonPath("$.postId").value(TEST_POST_ID.toString()))
                .andExpect(jsonPath("$.content").value("Test comment content"));

        verify(commentService).createComment(eq(TEST_USER_ID), any(CommentCreateRequest.class));
    }

    @Test
    @DisplayName("Создание комментария - пост не найден")
    void createComment_whenPostNotFound_shouldReturnNotFound() throws Exception {
        final CommentCreateRequest request = TestDataFactory.createCommentCreateRequest(TEST_POST_ID, "Test comment");

        when(commentService.createComment(eq(TEST_USER_ID), any(CommentCreateRequest.class)))
                .thenThrow(new PostNotFoundException("Пост не найден"));

        mockMvcUtils.performPost("/comment/create", request, "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Post Not Found"));

        verify(commentService).createComment(eq(TEST_USER_ID), any(CommentCreateRequest.class));
    }

    @Test
    @DisplayName("Создание комментария - отсутствует заголовок пользователя")
    void createComment_whenUserHeaderMissing_shouldReturnBadRequest() throws Exception {
        final CommentCreateRequest request = TestDataFactory.createCommentCreateRequest(TEST_POST_ID, "Test comment");

        mockMvcUtils.performPost("/comment/create", request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(commentService, never()).createComment(any(), any());
    }

    @Test
    @DisplayName("Редактирование комментария - успешно")
    void editComment_whenRequestIsValid_shouldReturnOk() throws Exception {
        final CommentEditRequest request = TestDataFactory.createCommentEditRequest(TEST_COMMENT_ID, "Updated comment");
        final CommentResponse response = TestDataFactory.createCommentResponse(
                TEST_COMMENT_ID, TEST_USER_ID, TEST_POST_ID);

        when(commentService.editComment(eq(TEST_USER_ID), any(CommentEditRequest.class)))
                .thenReturn(response);

        mockMvcUtils.performPut("/comment/edit", request, "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_COMMENT_ID.toString()))
                .andExpect(jsonPath("$.ownerId").value(TEST_USER_ID.toString()));

        verify(commentService).editComment(eq(TEST_USER_ID), any(CommentEditRequest.class));
    }

    @Test
    @DisplayName("Редактирование комментария - доступ запрещен")
    void editComment_whenUserIsNotOwner_shouldReturnForbidden() throws Exception {
        final CommentEditRequest request = TestDataFactory.createCommentEditRequest(TEST_COMMENT_ID, "Updated comment");

        when(commentService.editComment(eq(TEST_ANOTHER_USER_ID), any(CommentEditRequest.class)))
                .thenThrow(new AccessDeniedException("Доступ запрещен"));

        mockMvcUtils.performPut("/comment/edit", request, "X-User-Id", TEST_ANOTHER_USER_ID.toString())
                .andExpect(status().isForbidden());

        verify(commentService).editComment(eq(TEST_ANOTHER_USER_ID), any(CommentEditRequest.class));
    }

    @Test
    @DisplayName("Получение комментария по ID - успешно")
    void getCommentById_whenCommentExists_shouldReturnOk() throws Exception {
        final CommentResponse response = TestDataFactory.createCommentResponse(
                TEST_COMMENT_ID, TEST_USER_ID, TEST_POST_ID);

        when(commentService.getCommentById(TEST_COMMENT_ID)).thenReturn(response);

        mockMvcUtils.performGet("/comment/" + TEST_COMMENT_ID)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_COMMENT_ID.toString()))
                .andExpect(jsonPath("$.ownerId").value(TEST_USER_ID.toString()));

        verify(commentService).getCommentById(TEST_COMMENT_ID);
    }

    @Test
    @DisplayName("Получение комментария по ID - комментарий не найден")
    void getCommentById_whenCommentNotFound_shouldReturnNotFound() throws Exception {
        when(commentService.getCommentById(TEST_COMMENT_ID))
                .thenThrow(new CommentNotFoundException("Комментарий не найден"));

        mockMvcUtils.performGet("/comment/" + TEST_COMMENT_ID)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Comment Not Found"));

        verify(commentService).getCommentById(TEST_COMMENT_ID);
    }

    @Test
    @DisplayName("Получение комментариев пользователя к посту - успешно")
    void getCommentsByPostAndOwner_whenRequestIsValid_shouldReturnOk() throws Exception {
        final PageResponse<CommentResponse> pageResponse = TestDataFactory.createPageResponse(
                List.of(TestDataFactory.createCommentResponse(TEST_COMMENT_ID, TEST_USER_ID, TEST_POST_ID)));

        when(commentService.getCommentsByPostAndOwner(
                eq(TEST_USER_ID), eq(TEST_POST_ID), any(PageRequest.class)))
                .thenReturn(pageResponse);

        mockMvcUtils.performGet("/comment/pagesCommentByUserAndPost?postId=" + TEST_POST_ID +
                                "&size=10&pageNumber=0&sortedBy=createdAt&direction=DESC",
                        "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(TEST_COMMENT_ID.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(commentService).getCommentsByPostAndOwner(
                eq(TEST_USER_ID), eq(TEST_POST_ID), any(PageRequest.class));
    }

    @Test
    @DisplayName("Получение всех комментариев к посту - успешно")
    void getCommentsByPost_whenRequestIsValid_shouldReturnOk() throws Exception {
        final PageResponse<CommentResponse> pageResponse = TestDataFactory.createPageResponse(
                List.of(TestDataFactory.createCommentResponse(TEST_COMMENT_ID, TEST_USER_ID, TEST_POST_ID)));

        when(commentService.getCommentsByPost(eq(TEST_POST_ID), any(PageRequest.class)))
                .thenReturn(pageResponse);

        mockMvcUtils.performGet("/comment/pagesCommentByPost?postId=" + TEST_POST_ID +
                        "&size=10&pageNumber=0&sortedBy=createdAt&direction=DESC")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(TEST_COMMENT_ID.toString()));

        verify(commentService).getCommentsByPost(eq(TEST_POST_ID), any(PageRequest.class));
    }

    @Test
    @DisplayName("Удаление изображения с комментария - успешно")
    void deleteImageFromComment_whenRequestIsValid_shouldReturnOk() throws Exception {
        final CommentRemoveImageRequest request = CommentRemoveImageRequest.builder()
                .commentId(TEST_COMMENT_ID)
                .imageUrl("dummy-url")
                .build();

        final CommentResponse response = TestDataFactory.createCommentResponse(
                TEST_COMMENT_ID, TEST_USER_ID, TEST_POST_ID);

        when(commentService.removeImage(eq(TEST_USER_ID), any(CommentRemoveImageRequest.class)))
                .thenReturn(response);

        mockMvcUtils.performPatch("/comment/deleteImage", request,
                        "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_COMMENT_ID.toString()));

        verify(commentService).removeImage(eq(TEST_USER_ID), any(CommentRemoveImageRequest.class));
    }

    @Test
    @DisplayName("Создание комментария - пустой контент")
    void createComment_whenContentIsEmpty_shouldReturnBadRequest() throws Exception {
        final CommentCreateRequest request = TestDataFactory.createCommentCreateRequest(TEST_POST_ID, "");

        mockMvcUtils.performPost("/comment/create", request, "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isBadRequest());

        verify(commentService, never()).createComment(any(), any());
    }

    @Test
    @DisplayName("Создание комментария - слишком длинный контент")
    void createComment_whenContentTooLong_shouldReturnBadRequest() throws Exception {
        final String longContent = "a".repeat(1001);
        final CommentCreateRequest request = TestDataFactory.createCommentCreateRequest(TEST_POST_ID, longContent);

        mockMvcUtils.performPost("/comment/create", request, "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isBadRequest());

        verify(commentService, never()).createComment(any(), any());
    }
}