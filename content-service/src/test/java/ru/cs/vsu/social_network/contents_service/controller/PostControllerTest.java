package ru.cs.vsu.social_network.contents_service.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import ru.cs.vsu.social_network.contents_service.dto.request.post.PostCreateRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.post.PostEditRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.content.PostResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.contents_service.exception.post.PostNotFoundException;
import ru.cs.vsu.social_network.contents_service.exception.handler.GlobalExceptionHandler;
import ru.cs.vsu.social_network.contents_service.service.content.PostService;
import ru.cs.vsu.social_network.contents_service.utils.TestDataFactory;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PostControllerTest extends BaseControllerTest {

    private static final UUID TEST_USER_ID = TestDataFactory.TEST_USER_ID;
    private static final UUID TEST_POST_ID = TestDataFactory.TEST_POST_ID;
    private static final UUID TEST_ANOTHER_USER_ID = TestDataFactory.TEST_ANOTHER_USER_ID;

    @Mock
    private PostService postService;

    @Override
    protected Object controllerUnderTest() {
        return new PostController(postService);
    }

    @Override
    protected Object[] controllerAdvices() {
        return new Object[]{new GlobalExceptionHandler()};
    }

    @Test
    @DisplayName("Создание поста - успешно")
    void createPost_whenRequestIsValid_shouldReturnOk() throws Exception {
        final PostCreateRequest request = TestDataFactory.createPostCreateRequest("Test post");
        final PostResponse response = TestDataFactory.createPostResponse(TEST_POST_ID, TEST_USER_ID);

        when(postService.create(eq(TEST_USER_ID), any(PostCreateRequest.class)))
                .thenReturn(response);

        mockMvcUtils.performPost("/post/create", request, "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_POST_ID.toString()))
                .andExpect(jsonPath("$.ownerId").value(TEST_USER_ID.toString()));

        verify(postService).create(eq(TEST_USER_ID), any(PostCreateRequest.class));
    }

    @Test
    @DisplayName("Создание поста - пост не найден")
    void createPost_whenRequestIsValid_shouldReturnOkWithResponse() throws Exception {
        final PostCreateRequest request = TestDataFactory.createPostCreateRequest("Test post");
        final PostResponse response = TestDataFactory.createPostResponse(TEST_POST_ID, TEST_USER_ID);

        when(postService.create(eq(TEST_USER_ID), any(PostCreateRequest.class)))
                .thenReturn(response);

        mockMvcUtils.performPost("/post/create", request, "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isOk());

        verify(postService).create(eq(TEST_USER_ID), any(PostCreateRequest.class));
    }

    @Test
    @DisplayName("Создание поста - пустой контент")
    void createPost_whenContentIsEmpty_shouldReturnBadRequest() throws Exception {
        final PostCreateRequest request = TestDataFactory.createPostCreateRequest("");

        mockMvcUtils.performPost("/post/create", request, "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isBadRequest());

        verify(postService, never()).create(any(), any());
    }

    @Test
    @DisplayName("Редактирование поста - успешно")
    void editPost_whenRequestIsValid_shouldReturnOk() throws Exception {
        final PostEditRequest request = TestDataFactory.createPostEditRequest(TEST_POST_ID, "Updated messaging");
        final PostResponse response = TestDataFactory.createPostResponse(TEST_POST_ID, TEST_USER_ID);

        when(postService.editPost(eq(TEST_USER_ID), any(PostEditRequest.class)))
                .thenReturn(response);

        mockMvcUtils.performPut("/post/edit", request, "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_POST_ID.toString()));

        verify(postService).editPost(eq(TEST_USER_ID), any(PostEditRequest.class));
    }

    @Test
    @DisplayName("Редактирование поста - доступ запрещен")
    void editPost_whenUserIsNotOwner_shouldReturnForbidden() throws Exception {
        final PostEditRequest request = TestDataFactory.createPostEditRequest(TEST_POST_ID, "Updated messaging");

        when(postService.editPost(eq(TEST_ANOTHER_USER_ID), any(PostEditRequest.class)))
                .thenThrow(new AccessDeniedException("Доступ запрещен"));

        mockMvcUtils.performPut("/post/edit", request, "X-User-Id", TEST_ANOTHER_USER_ID.toString())
                .andExpect(status().isForbidden());

        verify(postService).editPost(eq(TEST_ANOTHER_USER_ID), any(PostEditRequest.class));
    }

    @Test
    @DisplayName("Редактирование поста - пост не найден")
    void editPost_whenPostNotFound_shouldReturnNotFound() throws Exception {
        final PostEditRequest request = TestDataFactory.createPostEditRequest(TEST_POST_ID, "Updated messaging");

        when(postService.editPost(eq(TEST_USER_ID), any(PostEditRequest.class)))
                .thenThrow(new PostNotFoundException("Пост не найден"));

        mockMvcUtils.performPut("/post/edit", request, "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isNotFound());

        verify(postService).editPost(eq(TEST_USER_ID), any(PostEditRequest.class));
    }

    @Test
    @DisplayName("Получение поста по ID - успешно")
    void getPostById_whenPostExists_shouldReturnOk() throws Exception {
        final PostResponse response = TestDataFactory.createPostResponse(TEST_POST_ID, TEST_USER_ID);

        when(postService.getPostById(TEST_POST_ID)).thenReturn(response);

        mockMvcUtils.performGet("/post/" + TEST_POST_ID)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_POST_ID.toString()))
                .andExpect(jsonPath("$.ownerId").value(TEST_USER_ID.toString()));

        verify(postService).getPostById(TEST_POST_ID);
    }

    @Test
    @DisplayName("Получение поста по ID - пост не найден")
    void getPostById_whenPostNotFound_shouldReturnNotFound() throws Exception {
        when(postService.getPostById(TEST_POST_ID))
                .thenThrow(new PostNotFoundException("Пост не найден"));

        mockMvcUtils.performGet("/post/" + TEST_POST_ID)
                .andExpect(status().isNotFound());

        verify(postService).getPostById(TEST_POST_ID);
    }

    @Test
    @DisplayName("Получение постов пользователя - успешно")
    void getPostsByCurrentUser_whenRequestIsValid_shouldReturnOk() throws Exception {
        final PageResponse<PostResponse> pageResponse = TestDataFactory.createPageResponse(
                List.of(TestDataFactory.createPostResponse(TEST_POST_ID, TEST_USER_ID)));

        when(postService.getAllPostsByUser(eq(TEST_USER_ID), any(PageRequest.class)))
                .thenReturn(pageResponse);

        mockMvcUtils.performGet("/post/pagesPost?size=1&pageNumber=0&sortedBy=createdAt&direction=DESC",
                        "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(TEST_POST_ID.toString()));

        verify(postService).getAllPostsByUser(eq(TEST_USER_ID), any(PageRequest.class));
    }

    @Test
    @DisplayName("Получение постов пользователя - отсутствует заголовок пользователя")
    void getPostsByCurrentUser_whenUserHeaderMissing_shouldReturnBadRequest() throws Exception {
        mockMvcUtils.performGet("/post/pagesPost?size=1&pageNumber=0")
                .andExpect(status().isBadRequest());

        verify(postService, never()).getAllPostsByUser(any(), any());
    }
}