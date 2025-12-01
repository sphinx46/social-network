package ru.cs.vsu.social_network.contents_service.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.cs.vsu.social_network.contents_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.content.CommentDetailsResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.contents_service.exception.comment.CommentNotFoundException;
import ru.cs.vsu.social_network.contents_service.exception.handler.GlobalExceptionHandler;
import ru.cs.vsu.social_network.contents_service.service.content.CommentDetailsService;
import ru.cs.vsu.social_network.contents_service.utils.TestDataFactory;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CommentDetailsControllerTest extends BaseControllerTest {

    private static final UUID TEST_USER_ID = TestDataFactory.TEST_USER_ID;
    private static final UUID TEST_POST_ID = TestDataFactory.TEST_POST_ID;
    private static final UUID TEST_COMMENT_ID = TestDataFactory.TEST_COMMENT_ID;

    @Mock
    private CommentDetailsService commentDetailsService;

    @Override
    protected Object controllerUnderTest() {
        return new CommentDetailsController(commentDetailsService);
    }

    @Override
    protected Object[] controllerAdvices() {
        return new Object[]{new GlobalExceptionHandler()};
    }

    @Test
    @DisplayName("Получение детальной информации о комментарии - успешно")
    void getCommentDetails_whenCommentExists_shouldReturnOk() throws Exception {
        final CommentDetailsResponse response = TestDataFactory.createCommentDetailsResponse(TEST_COMMENT_ID);

        when(commentDetailsService.getCommentDetails(
                eq(TEST_COMMENT_ID), anyBoolean(), anyInt()))
                .thenReturn(response);

        mockMvcUtils.performGet("/comment-details/" + TEST_COMMENT_ID +
                        "?includeLikes=true&likesLimit=10")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_COMMENT_ID.toString()))
                .andExpect(jsonPath("$.ownerId").value(TEST_USER_ID.toString()))
                .andExpect(jsonPath("$.postId").value(TEST_POST_ID.toString()));

        verify(commentDetailsService).getCommentDetails(
                eq(TEST_COMMENT_ID), anyBoolean(), anyInt());
    }

    @Test
    @DisplayName("Получение детальной информации о комментарии - комментарий не найден")
    void getCommentDetails_whenCommentNotFound_shouldReturnNotFound() throws Exception {
        when(commentDetailsService.getCommentDetails(
                eq(TEST_COMMENT_ID), anyBoolean(), anyInt()))
                .thenThrow(new CommentNotFoundException("Комментарий не найден"));

        mockMvcUtils.performGet("/comment-details/" + TEST_COMMENT_ID)
                .andExpect(status().isNotFound());

        verify(commentDetailsService).getCommentDetails(
                eq(TEST_COMMENT_ID), anyBoolean(), anyInt());
    }

    @Test
    @DisplayName("Получение детальной информации о комментарии - невалидный лимит")
    void getCommentDetails_whenInvalidLimit_shouldReturnOk() throws Exception {
        final CommentDetailsResponse response = TestDataFactory.createCommentDetailsResponse(TEST_COMMENT_ID);

        when(commentDetailsService.getCommentDetails(
                eq(TEST_COMMENT_ID), anyBoolean(), anyInt()))
                .thenReturn(response);

        mockMvcUtils.performGet("/comment-details/" + TEST_COMMENT_ID + "?likesLimit=0")
                .andExpect(status().isOk());

        verify(commentDetailsService).getCommentDetails(
                eq(TEST_COMMENT_ID), anyBoolean(), anyInt());
    }

    @Test
    @DisplayName("Получение детальной информации о комментариях поста - успешно")
    void getPostCommentsDetails_whenRequestIsValid_shouldReturnOk() throws Exception {
        final PageResponse<CommentDetailsResponse> pageResponse = TestDataFactory.createPageResponse(
                List.of(TestDataFactory.createCommentDetailsResponse(TEST_COMMENT_ID)));

        when(commentDetailsService.getPostCommentsDetails(
                eq(TEST_POST_ID), any(PageRequest.class), anyBoolean(), anyInt()))
                .thenReturn(pageResponse);

        mockMvcUtils.performGet("/comment-details/post/" + TEST_POST_ID +
                        "?size=10&pageNumber=0&sortedBy=createdAt&direction=DESC" +
                        "&includeLikes=true&likesLimit=5")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(TEST_COMMENT_ID.toString()));

        verify(commentDetailsService).getPostCommentsDetails(
                eq(TEST_POST_ID), any(PageRequest.class), anyBoolean(), anyInt());
    }

    @Test
    @DisplayName("Получение детальной информации о комментариях пользователя - успешно")
    void getUserCommentsDetails_whenRequestIsValid_shouldReturnOk() throws Exception {
        final PageResponse<CommentDetailsResponse> pageResponse = TestDataFactory.createPageResponse(
                List.of(TestDataFactory.createCommentDetailsResponse(TEST_COMMENT_ID)));

        when(commentDetailsService.getUserCommentsDetails(
                eq(TEST_USER_ID), any(PageRequest.class), anyBoolean(), anyInt()))
                .thenReturn(pageResponse);

        mockMvcUtils.performGet("/comment-details/user/" + TEST_USER_ID +
                        "?size=10&pageNumber=0&sortedBy=createdAt&direction=DESC" +
                        "&includeLikes=true&likesLimit=5")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(TEST_COMMENT_ID.toString()));

        verify(commentDetailsService).getUserCommentsDetails(
                eq(TEST_USER_ID), any(PageRequest.class), anyBoolean(), anyInt());
    }
}