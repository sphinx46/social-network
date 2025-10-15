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
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.custom.AccessDeniedException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.CommentNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.PostNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.CommentRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.PostRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.AccessValidator;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.ContentFactory;

import java.util.Optional;
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
    private PostRepository postRepository;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private ContentFactory contentFactory;

    @Mock
    private AccessValidator accessValidator;

    @InjectMocks
    private CommentService commentService;

    @Test
    void create_whenRequestIsValid() {
        User currentUser = createTestUser(1L, "user");
        Post post = createTestPost(currentUser, "Test post", null);
        CommentRequest request = createTestCommentRequest(post.getId(), "Test comment", null);
        Comment comment = createTestComment(1L, currentUser, post, "Test comment", null);
        CommentResponse expectedResponse = createTestCommentResponse(comment);

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(contentFactory.createComment(currentUser, post, request.getContent(), request.getImageUrl())).thenReturn(comment);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(entityMapper.map(comment, CommentResponse.class)).thenReturn(expectedResponse);

        CommentResponse result = commentService.create(request, currentUser);

        assertNotNull(result);

        verify(postRepository).findById(post.getId());
        verify(contentFactory).createComment(currentUser, post, request.getContent(), request.getImageUrl());
        verify(commentRepository).save(any(Comment.class));
        verify(entityMapper).map(comment, CommentResponse.class);
    }

    @Test
    void create_whenPostNotExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        CommentRequest request = createTestCommentRequest(1L, "Test comment", null);

        when(postRepository.findById(request.getPostId())).thenReturn(Optional.empty());

        PostNotFoundException postNotFoundException = assertThrows(PostNotFoundException.class,
                () -> commentService.create(request, currentUser));

        assertEquals(ResponseMessageConstants.NOT_FOUND, postNotFoundException.getMessage());
    }

    @Test
    void editComment_whenRequestIsValid() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        Post post = createTestPost(currentUser, "Test post", null);
        Comment comment = createTestComment(1L, currentUser, post, "Old content", null);
        CommentRequest request = createTestCommentRequest(post.getId(), "New content", "image.jpg");
        Comment updatedComment = createTestComment(1L, currentUser, post, "New content", "image.jpg");
        CommentResponse expectedResponse = createTestCommentResponse(updatedComment);

        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(updatedComment);
        when(entityMapper.map(updatedComment, CommentResponse.class)).thenReturn(expectedResponse);

        CommentResponse result = commentService.editComment(comment.getId(), request, currentUser);

        assertNotNull(result);
        assertEquals("New content", result.getContent());
        assertEquals("image.jpg", result.getImageUrl());

        verify(commentRepository).findById(comment.getId());
        verify(accessValidator).validateCommentOwnership(currentUser, comment);
        verify(commentRepository).save(any(Comment.class));
        verify(entityMapper).map(updatedComment, CommentResponse.class);
    }

    @Test
    void editComment_whenCommentNotExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        CommentRequest request = createTestCommentRequest(1L, "New content", null);

        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        CommentNotFoundException commentNotFoundException = assertThrows(CommentNotFoundException.class,
                () -> commentService.editComment(1L, request, currentUser));

        assertEquals(ResponseMessageConstants.NOT_FOUND, commentNotFoundException.getMessage());
    }

    @Test
    void editComment_whenUserIsNotOwner() {
        User commentOwner = createTestUser(1L, "owner", "owner@example.com");
        User currentUser = createTestUser(2L, "user", "user@example.com");
        Post post = createTestPost(commentOwner, "Test post", null);
        Comment comment = createTestComment(1L, commentOwner, post, "Old content", null);
        CommentRequest request = createTestCommentRequest(post.getId(), "New content", null);

        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        doThrow(new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED))
                .when(accessValidator).validateCommentOwnership(currentUser, comment);

        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class,
                () -> commentService.editComment(comment.getId(), request, currentUser));

        assertEquals(ResponseMessageConstants.ACCESS_DENIED, accessDeniedException.getMessage());
    }

    @Test
    void deleteComment_whenUserIsCommentCreator() throws Exception {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        Post post = createTestPost(currentUser, "Test post", null);
        Comment comment = createTestComment(1L, currentUser, post, "Test comment", null);

        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        CompletableFuture<Boolean> result = commentService.deleteComment(comment.getId(), currentUser);

        assertTrue(result.get());

        verify(commentRepository).findById(comment.getId());
        verify(accessValidator).validateCommentOwnership(currentUser, comment);
        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_whenUserIsPostOwner() throws Exception {
        User postOwner = createTestUser(1L, "postOwner", "owner@example.com");
        User commentCreator = createTestUser(2L, "commentCreator", "creator@example.com");
        Post post = createTestPost(postOwner, "Test post", null);
        Comment comment = createTestComment(1L, commentCreator, post, "Test comment", null);

        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        CompletableFuture<Boolean> result = commentService.deleteComment(comment.getId(), postOwner);

        assertTrue(result.get());

        verify(commentRepository).findById(comment.getId());
        verify(accessValidator).validateCommentOwnership(postOwner, comment);
        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_whenUserHasNoAccess() {
        User commentCreator = createTestUser(1L, "creator", "creator@example.com");
        User postOwner = createTestUser(2L, "owner", "owner@example.com");
        User otherUser = createTestUser(3L, "other", "other@example.com");
        Post post = createTestPost(postOwner, "Test post", null);
        Comment comment = createTestComment(1L, commentCreator, post, "Test comment", null);

        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        doThrow(new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED))
                .when(accessValidator).validateCommentOwnership(otherUser, comment);

        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class,
                () -> commentService.deleteComment(comment.getId(), otherUser));

        assertEquals(ResponseMessageConstants.ACCESS_DENIED, accessDeniedException.getMessage());
    }

    @Test
    void deleteComment_whenCommentNotExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");

        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        CommentNotFoundException commentNotFoundException = assertThrows(CommentNotFoundException.class,
                () -> commentService.deleteComment(1L, currentUser));

        assertEquals(ResponseMessageConstants.NOT_FOUND, commentNotFoundException.getMessage());
    }

    @Test
    void getCommentById_whenCommentExists() {
        User user = createTestUser(1L, "user", "example@example.com");
        Post post = createTestPost(user, "Test post", null);
        Comment comment = createTestComment(1L, user, post, "Test comment", null);
        CommentResponse expectedResponse = createTestCommentResponse(comment);

        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        when(entityMapper.map(comment, CommentResponse.class)).thenReturn(expectedResponse);

        CommentResponse result = commentService.getCommentById(comment.getId());

        assertNotNull(result);

        verify(commentRepository).findById(comment.getId());
        verify(entityMapper).map(comment, CommentResponse.class);
    }

    @Test
    void getCommentById_whenCommentNotExists() {
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        CommentNotFoundException commentNotFoundException = assertThrows(CommentNotFoundException.class,
                () -> commentService.getCommentById(1L));

        assertEquals(ResponseMessageConstants.NOT_FOUND, commentNotFoundException.getMessage());
    }
}