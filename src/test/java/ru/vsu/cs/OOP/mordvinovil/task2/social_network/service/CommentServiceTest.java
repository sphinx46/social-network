package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.CommentRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.CommentResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Comment;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.EventPublisherService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.CommentNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.PostNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.CommentRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.ContentFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.CommentValidator;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private ContentFactory contentFactory;

    @Mock
    private CommentValidator commentValidator;

    @Mock
    private EventPublisherService eventPublisherService;

    @Mock
    private EntityUtils entityUtils;

    @InjectMocks
    private CommentService commentService;

    @Test
    void createComment_whenRequestIsValid() {
        User currentUser = createTestUser(1L, "user", "user@example.com");
        Post post = createTestPost(createTestUser(2L, "postOwner", "owner@example.com"), "Post content", null);
        CommentRequest request = createTestCommentRequest(post.getId(), "Comment content", null);
        Comment comment = createTestComment(1L, currentUser, post, "Comment content", null);
        Comment savedComment = createTestComment(1L, currentUser, post, "Comment content", null);
        CommentResponse expectedResponse = createTestCommentResponse(savedComment);

        Long postOwnerId = post.getUser().getId();

        when(entityUtils.getPost(post.getId())).thenReturn(post);
        when(contentFactory.createComment(currentUser, post, request.getContent(), request.getImageUrl())).thenReturn(comment);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);
        when(entityMapper.map(savedComment, CommentResponse.class)).thenReturn(expectedResponse);

        CommentResponse result = commentService.create(request, currentUser);

        assertNotNull(result);
        assertEquals(expectedResponse.getContent(), result.getContent());

        verify(commentValidator).validate(request, currentUser);
        verify(entityUtils).getPost(post.getId());
        verify(contentFactory).createComment(currentUser, post, request.getContent(), request.getImageUrl());
        verify(commentRepository).save(any(Comment.class));
        verify(eventPublisherService).publishCommentAdded(any(), eq(postOwnerId),
                eq(post.getId()), eq(currentUser.getId()));
        verify(entityMapper).map(savedComment, CommentResponse.class);
    }

    @Test
    void createComment_whenPostNotExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        CommentRequest request = createTestCommentRequest(999L, "Test comment", null);

        when(entityUtils.getPost(999L)).thenThrow(new PostNotFoundException(ResponseMessageConstants.NOT_FOUND));

        PostNotFoundException exception = assertThrows(PostNotFoundException.class,
                () -> commentService.create(request, currentUser));

        assertEquals(ResponseMessageConstants.NOT_FOUND, exception.getMessage());
        verify(entityUtils).getPost(999L);
        verifyNoInteractions(contentFactory, commentRepository, entityMapper);
    }

    @Test
    void editComment_whenRequestIsValid() {
        User currentUser = createTestUser(1L, "user", "user@example.com");
        Post post = createTestPost(createTestUser(2L, "postOwner", "owner@example.com"), "Post content", null);
        Comment comment = createTestComment(1L, currentUser, post, "Old content", "old.jpg");
        CommentRequest request = createTestCommentRequest(post.getId(), "Updated content", "new.jpg");
        Comment updatedComment = createTestComment(1L, currentUser, post, "Updated content", "new.jpg");
        CommentResponse expectedResponse = createTestCommentResponse(updatedComment);

        when(entityUtils.getComment(1L)).thenReturn(comment);
        doNothing().when(commentValidator).validateCommentUpdate(request, 1L, currentUser);
        when(commentRepository.save(any(Comment.class))).thenReturn(updatedComment);
        when(entityMapper.map(updatedComment, CommentResponse.class)).thenReturn(expectedResponse);

        CommentResponse result = commentService.editComment(1L, request, currentUser);

        assertNotNull(result);
        assertEquals("Updated content", result.getContent());
        assertEquals("new.jpg", result.getImageUrl());

        verify(entityUtils).getComment(1L);
        verify(commentValidator).validateCommentUpdate(request, 1L, currentUser);
        verify(commentRepository).save(any(Comment.class));
        verify(entityMapper).map(updatedComment, CommentResponse.class);
    }




    @Test
    void deleteComment_whenUserIsOwner() {
        User currentUser = createTestUser(1L, "user", "user@example.com");
        Post post = createTestPost(createTestUser(2L, "postOwner", "owner@example.com"), "Post content", null);
        Comment comment = createTestComment(null, currentUser, post, "Comment content");
        comment.setId(1L);

        when(entityUtils.getComment(1L)).thenReturn(comment);
        doNothing().when(commentValidator).validateCommentOwnership(1L, currentUser);

        CompletableFuture<Boolean> result = commentService.deleteComment(1L, currentUser);

        assertNotNull(result);
        assertTrue(result.join());

        verify(commentValidator).validateCommentOwnership(1L, currentUser);
        verify(entityUtils).getComment(1L);
        verify(commentRepository).delete(comment);
    }


    @Test
    void getCommentById_whenCommentExists() {
        User user = createTestUser(1L, "user", "user@example.com");
        Post post = createTestPost(createTestUser(2L, "postOwner", "owner@example.com"), "Post content", null);
        Comment comment = createTestComment(null, user, post, "Comment content");
        comment.setId(1L);
        CommentResponse expectedResponse = createTestCommentResponse(comment);

        when(entityUtils.getComment(1L)).thenReturn(comment);
        when(entityMapper.map(comment, CommentResponse.class)).thenReturn(expectedResponse);

        CommentResponse result = commentService.getCommentById(1L);

        assertNotNull(result);
        assertEquals(expectedResponse.getId(), result.getId());

        verify(entityUtils).getComment(1L);
        verify(entityMapper).map(comment, CommentResponse.class);
    }

    @Test
    void getCommentById_whenCommentNotExists() {
        when(entityUtils.getComment(1L)).thenThrow(new CommentNotFoundException(ResponseMessageConstants.NOT_FOUND));

        CommentNotFoundException exception = assertThrows(CommentNotFoundException.class,
                () -> commentService.getCommentById(1L));

        assertEquals(ResponseMessageConstants.NOT_FOUND, exception.getMessage());
        verify(entityUtils).getComment(1L);
        verifyNoInteractions(entityMapper);
    }

    @Test
    void editComment_whenCommentNotExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        CommentRequest request = createTestCommentRequest(1L, "New content", null);

        when(entityUtils.getComment(1L)).thenThrow(new CommentNotFoundException(ResponseMessageConstants.NOT_FOUND));

        CommentNotFoundException exception = assertThrows(CommentNotFoundException.class,
                () -> commentService.editComment(1L, request, currentUser));

        assertEquals(ResponseMessageConstants.NOT_FOUND, exception.getMessage());
        verify(entityUtils).getComment(1L);
    }

    @Test
    void deleteComment_whenCommentNotExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");

        when(entityUtils.getComment(1L)).thenThrow(new CommentNotFoundException(ResponseMessageConstants.NOT_FOUND));

        CommentNotFoundException exception = assertThrows(CommentNotFoundException.class,
                () -> commentService.deleteComment(1L, currentUser));

        assertEquals(ResponseMessageConstants.NOT_FOUND, exception.getMessage());
        verify(entityUtils).getComment(1L);
    }
}