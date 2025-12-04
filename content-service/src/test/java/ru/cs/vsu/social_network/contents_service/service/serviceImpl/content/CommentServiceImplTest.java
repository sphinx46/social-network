package ru.cs.vsu.social_network.contents_service.service.serviceImpl.content;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import ru.cs.vsu.social_network.contents_service.dto.request.comment.*;
import ru.cs.vsu.social_network.contents_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.content.CommentResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.contents_service.entity.Comment;
import ru.cs.vsu.social_network.contents_service.exception.post.PostUploadImageException;
import ru.cs.vsu.social_network.contents_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.contents_service.provider.CommentEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.CommentRepository;
import ru.cs.vsu.social_network.contents_service.service.cache.CacheEventPublisherService;
import ru.cs.vsu.social_network.contents_service.utils.MessageConstants;
import ru.cs.vsu.social_network.contents_service.utils.TestDataFactory;
import ru.cs.vsu.social_network.contents_service.utils.factory.content.CommentFactory;
import ru.cs.vsu.social_network.contents_service.validation.CommentValidator;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для {@link CommentServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    private static final UUID USER_ID = UUID.fromString("3fc4530f-5c0c-429d-8a64-810ce3bd1415");
    private static final UUID POST_ID = UUID.fromString("8dce7d87-60d9-4b66-8851-e7dba4684538");
    private static final UUID COMMENT_ID = UUID.fromString("40d4dde4-663d-4113-bd08-25f5e1d42efe");
    private static final UUID ANOTHER_USER_ID = UUID.fromString("e0d8a734-6f6c-4ab4-b4fe-e93cc63d8406");

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private EntityMapper entityMapper;
    @Mock
    private CommentFactory commentFactory;
    @Mock
    private CommentEntityProvider commentEntityProvider;
    @Mock
    private CommentValidator commentValidator;
    @Mock
    private CacheEventPublisherService cacheEventPublisherService;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    @DisplayName("Создание комментария - успешно")
    void createComment_whenRequestIsValid_shouldReturnResponse() {
        CommentCreateRequest request = TestDataFactory.createCommentCreateRequest(POST_ID, "Test comment");
        Comment comment = TestDataFactory.createCommentEntity(COMMENT_ID, USER_ID, POST_ID, "Test comment", null);
        Comment savedComment = TestDataFactory.createCommentEntity(COMMENT_ID);
        CommentResponse expectedResponse = TestDataFactory.createCommentResponse(COMMENT_ID, USER_ID, POST_ID);

        when(commentFactory.create(USER_ID, request)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(savedComment);
        when(entityMapper.map(savedComment, CommentResponse.class)).thenReturn(expectedResponse);
        doNothing().when(cacheEventPublisherService).publishCommentCreated(any(), any(), any(), any(), any());

        CommentResponse actual = commentService.createComment(USER_ID, request);

        assertNotNull(actual);
        assertEquals(expectedResponse, actual);
        verify(commentRepository).save(comment);
    }

    @Test
    @DisplayName("Редактирование комментария - успешно")
    void editComment_whenRequestIsValid_shouldReturnResponse() {
        CommentEditRequest request = TestDataFactory.createCommentEditRequest(COMMENT_ID, "Updated comment");
        Comment comment = TestDataFactory.createCommentEntity(COMMENT_ID, USER_ID, POST_ID, "Old comment", null);
        Comment updatedComment = TestDataFactory.createCommentEntity(COMMENT_ID, USER_ID, POST_ID, "Updated comment", null);
        CommentResponse expectedResponse = TestDataFactory.createCommentResponse(COMMENT_ID, USER_ID, POST_ID);

        doNothing().when(commentValidator).validateOwnership(USER_ID, COMMENT_ID);
        when(commentEntityProvider.getById(COMMENT_ID)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(updatedComment);
        when(entityMapper.map(updatedComment, CommentResponse.class)).thenReturn(expectedResponse);
        doNothing().when(cacheEventPublisherService).publishCommentUpdated(any(), any(), any(), any(), any());

        CommentResponse actual = commentService.editComment(USER_ID, request);

        assertEquals("Updated comment", comment.getContent());
        assertEquals(expectedResponse, actual);
        verify(commentValidator).validateOwnership(USER_ID, COMMENT_ID);
    }

    @Test
    @DisplayName("Редактирование комментария - доступ запрещен")
    void editComment_whenUserNotOwner_shouldThrowException() {
        CommentEditRequest request = TestDataFactory.createCommentEditRequest(COMMENT_ID,
                "Updated comment");

        doThrow(new AccessDeniedException(MessageConstants.ACCESS_DENIED))
                .when(commentValidator).validateOwnership(ANOTHER_USER_ID, COMMENT_ID);

        assertThrows(AccessDeniedException.class, () -> commentService.editComment(ANOTHER_USER_ID, request));

        verify(commentValidator).validateOwnership(ANOTHER_USER_ID, COMMENT_ID);
        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Удаление комментария - успешно")
    void deleteComment_whenRequestIsValid_shouldReturnResponse() {
        CommentDeleteRequest request = TestDataFactory.createCommentDeleteRequest(COMMENT_ID);
        Comment comment = TestDataFactory.createCommentEntity(COMMENT_ID, USER_ID, POST_ID,
                "Test comment", null);
        CommentResponse expectedResponse = TestDataFactory.createCommentResponse(COMMENT_ID, USER_ID, POST_ID);

        doNothing().when(commentValidator).validateOwnership(USER_ID, COMMENT_ID);
        when(commentEntityProvider.getById(COMMENT_ID)).thenReturn(comment);
        when(entityMapper.map(comment, CommentResponse.class)).thenReturn(expectedResponse);
        doNothing().when(cacheEventPublisherService).publishCommentDeleted(any(), any(), any(), any(), any());

        CommentResponse actual = commentService.deleteComment(USER_ID, request);

        assertEquals(expectedResponse, actual);
        verify(commentValidator).validateOwnership(USER_ID, COMMENT_ID);
        verify(commentRepository).delete(comment);
    }

    @Test
    @DisplayName("Загрузка изображения комментария - успешно")
    void uploadImage_whenRequestIsValid_shouldReturnResponse() {
        String imageUrl = "http://example.com/comment-image.jpg";
        CommentUploadImageRequest request = TestDataFactory.createCommentUploadImageRequest(COMMENT_ID, imageUrl);
        Comment comment = TestDataFactory.createCommentEntity(COMMENT_ID, USER_ID, POST_ID,
                "Comment", null);
        Comment updatedComment = TestDataFactory.createCommentEntity(COMMENT_ID, USER_ID, POST_ID,
                "Comment", imageUrl);
        CommentResponse expectedResponse = TestDataFactory.createCommentResponse(COMMENT_ID, USER_ID, POST_ID);

        doNothing().when(commentValidator).validateOwnership(USER_ID, COMMENT_ID);
        when(commentEntityProvider.getById(COMMENT_ID)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(updatedComment);
        when(entityMapper.map(updatedComment, CommentResponse.class)).thenReturn(expectedResponse);
        doNothing().when(cacheEventPublisherService).publishCommentUpdated(any(), any(), any(), any(), any());

        CommentResponse actual = commentService.uploadImage(USER_ID, request);

        assertEquals(imageUrl, comment.getImageUrl());
        assertEquals(expectedResponse, actual);
        verify(commentValidator).validateOwnership(USER_ID, COMMENT_ID);
    }

    @Test
    @DisplayName("Загрузка изображения комментария - пустой URL")
    void uploadImage_whenImageUrlEmpty_shouldThrowException() {
        CommentUploadImageRequest request =
                TestDataFactory.createCommentUploadImageRequest(COMMENT_ID, " ");
        Comment comment = TestDataFactory.createCommentEntity(COMMENT_ID, USER_ID, POST_ID,
                "Comment", null);

        doNothing().when(commentValidator).validateOwnership(USER_ID, COMMENT_ID);
        when(commentEntityProvider.getById(COMMENT_ID)).thenReturn(comment);

        assertThrows(PostUploadImageException.class, () -> commentService.uploadImage(USER_ID, request));

        verify(commentValidator).validateOwnership(USER_ID, COMMENT_ID);
        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Удаление изображения комментария - успешно")
    void removeImage_whenRequestIsValid_shouldReturnResponse() {
        CommentRemoveImageRequest request = TestDataFactory.createCommentRemoveImageRequest(COMMENT_ID);
        Comment comment = TestDataFactory.createCommentEntity(COMMENT_ID, USER_ID, POST_ID,
                "Comment", "http://example.com/image.jpg");
        Comment updatedComment = TestDataFactory.createCommentEntity(COMMENT_ID, USER_ID, POST_ID,
                "Comment", null);
        CommentResponse expectedResponse = TestDataFactory.createCommentResponse(COMMENT_ID, USER_ID, POST_ID);

        doNothing().when(commentValidator).validateOwnership(USER_ID, COMMENT_ID);
        when(commentEntityProvider.getById(COMMENT_ID)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(updatedComment);
        when(entityMapper.map(updatedComment, CommentResponse.class)).thenReturn(expectedResponse);
        doNothing().when(cacheEventPublisherService).publishCommentUpdated(any(), any(), any(), any(), any());

        CommentResponse actual = commentService.removeImage(USER_ID, request);

        assertNull(comment.getImageUrl());
        assertEquals(expectedResponse, actual);
        verify(commentValidator).validateOwnership(USER_ID, COMMENT_ID);
    }

    @Test
    @DisplayName("Получение комментария по ID - успешно")
    void getCommentById_whenCommentExists_shouldReturnResponse() {
        Comment comment = TestDataFactory.createCommentEntity(COMMENT_ID);
        CommentResponse expectedResponse = TestDataFactory.createCommentResponse(COMMENT_ID, USER_ID, POST_ID);

        when(commentEntityProvider.getById(COMMENT_ID)).thenReturn(comment);
        when(entityMapper.map(comment, CommentResponse.class)).thenReturn(expectedResponse);

        CommentResponse actual = commentService.getCommentById(COMMENT_ID);

        assertEquals(expectedResponse, actual);
        verify(commentEntityProvider).getById(COMMENT_ID);
    }

    @Test
    @DisplayName("Получение комментариев поста - успешно")
    void getCommentsByPost_whenPostExists_shouldReturnPageResponse() {
        PageRequest pageRequest = TestDataFactory.createPageRequest(0, 10);
        Comment comment = TestDataFactory.createCommentEntity(COMMENT_ID);
        List<Comment> comments = List.of(comment);
        org.springframework.data.domain.Page<Comment> page = TestDataFactory.createPage(comments);
        CommentResponse commentResponse = TestDataFactory.createCommentResponse(COMMENT_ID, USER_ID, POST_ID);
        PageResponse<CommentResponse> expectedResponse =
                TestDataFactory.createPageResponse(List.of(commentResponse));

        when(commentRepository.findAllByPostId(POST_ID, pageRequest.toPageable())).thenReturn(page);
        when(entityMapper.map(comment, CommentResponse.class)).thenReturn(commentResponse);

        PageResponse<CommentResponse> actual = commentService.getCommentsByPost(POST_ID, pageRequest);

        assertNotNull(actual);
        assertEquals(1, actual.getContent().size());
        verify(commentRepository).findAllByPostId(POST_ID, pageRequest.toPageable());
    }
}