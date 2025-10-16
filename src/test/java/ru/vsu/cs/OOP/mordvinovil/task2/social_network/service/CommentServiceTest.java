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
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.CommentNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.PostNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.CommentRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.PostRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.ContentFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.CommentValidator;

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
    private CommentValidator commentValidator;

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

        verify(commentValidator).validate(request, currentUser);
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

        verify(commentValidator).validateCommentUpdate(request, comment.getId(), currentUser);
        verify(commentRepository).findById(comment.getId());
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
    void deleteComment_whenUserIsCommentCreator() throws Exception {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        Post post = createTestPost(currentUser, "Test post", null);
        Comment comment = createTestComment(1L, currentUser, post, "Test comment", null);

        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        CompletableFuture<Boolean> result = commentService.deleteComment(comment.getId(), currentUser);

        assertTrue(result.get());

        verify(commentValidator).validateCommentOwnership(comment.getId(), currentUser);
        verify(commentRepository).findById(comment.getId());
        verify(commentRepository).delete(comment);
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