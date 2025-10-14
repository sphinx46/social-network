package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.CommentRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.CommentResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Comment;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Role;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.custom.AccessDeniedException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.CommentNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.PostNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.CommentRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.PostRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {
    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CommentService commentService;

    @Test
    void create_whenRequestIsValid() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        Post post = createTestPost(currentUser, "Test post", null);
        CommentRequest request = createTestCommentRequest(post.getId(), "Test comment", null);
        Comment comment = createTestComment(1L, currentUser, post, "Test comment", null);
        CommentResponse expectedResponse = createTestCommentResponse(comment);

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(modelMapper.map(comment, CommentResponse.class)).thenReturn(expectedResponse);

        CommentResponse result = commentService.create(request, currentUser);

        assertNotNull(result);

        verify(postRepository).findById(post.getId());
        verify(commentRepository).save(any(Comment.class));
        verify(modelMapper).map(comment, CommentResponse.class);
    }

    @Test
    void create_whenPostNotExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        CommentRequest request = createTestCommentRequest(1L, "Test comment", null);

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
        CommentResponse expectedResponse = createTestCommentResponse(comment);
        expectedResponse.setContent("New content");
        expectedResponse.setImageUrl("image.jpg");

        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(modelMapper.map(comment, CommentResponse.class)).thenReturn(expectedResponse);

        CommentResponse result = commentService.editComment(comment.getId(), request, currentUser);

        assertNotNull(result);
        assertEquals("New content", result.getContent());
        assertEquals("image.jpg", result.getImageUrl());

        verify(commentRepository).findById(comment.getId());
        verify(commentRepository).save(any(Comment.class));
        verify(modelMapper).map(comment, CommentResponse.class);
    }

    @Test
    void editComment_whenCommentNotExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        CommentRequest request = createTestCommentRequest(1L, "New content", null);

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
        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_whenUserIsPostOwner() throws Exception {
        User postOwner = createTestUser(1L, "postOwner", "owner@example.com");
        User commentCreator = createTestUser(2L, "commentCreator", "creator@example.com");
        Post post = createTestPost(postOwner, "Test post", null);
        post.setUser(postOwner);
        Comment comment = createTestComment(1L, commentCreator, post, "Test comment", null);

        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        CompletableFuture<Boolean> result = commentService.deleteComment(comment.getId(), postOwner);

        assertTrue(result.get());

        verify(commentRepository).findById(comment.getId());
        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_whenUserHasNoAccess() {
        User commentCreator = createTestUser(1L, "creator", "creator@example.com");
        User postOwner = createTestUser(2L, "owner", "owner@example.com");
        User otherUser = createTestUser(3L, "other", "other@example.com");
        Post post = createTestPost(commentCreator, "Test post", null);
        post.setUser(postOwner);
        Comment comment = createTestComment(1L, commentCreator, post, "Test comment", null);

        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

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
        when(modelMapper.map(comment, CommentResponse.class)).thenReturn(expectedResponse);

        CommentResponse result = commentService.getCommentById(comment.getId());

        assertNotNull(result);

        verify(commentRepository).findById(comment.getId());
        verify(modelMapper).map(comment, CommentResponse.class);
    }

    @Test
    void getCommentById_whenCommentNotExists() {
        CommentNotFoundException commentNotFoundException = assertThrows(CommentNotFoundException.class,
                () -> commentService.getCommentById(1L));

        assertEquals(ResponseMessageConstants.NOT_FOUND, commentNotFoundException.getMessage());
    }

    private User createTestUser(Long id, String username, String email) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("password");
        user.setCity("Moscow");
        user.setRole(Role.ROLE_USER);
        user.setCreatedAt(LocalDateTime.now());
        user.setOnline(false);
        return user;
    }

    private Post createTestPost(User user, String content, String imageUrl) {
        return Post.builder()
                .user(user)
                .imageUrl(imageUrl)
                .content(content)
                .build();
    }

    private Comment createTestComment(Long id, User creator, Post post, String content, String imageUrl) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setCreator(creator);
        comment.setPost(post);
        comment.setContent(content);
        comment.setImageUrl(imageUrl);
        comment.setTime(LocalDateTime.now());
        return comment;
    }

    private CommentRequest createTestCommentRequest(Long postId, String content, String imageUrl) {
        CommentRequest request = new CommentRequest();
        request.setPostId(postId);
        request.setContent(content);
        request.setImageUrl(imageUrl);
        return request;
    }

    private CommentResponse createTestCommentResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setImageUrl(comment.getImageUrl());
        response.setTime(comment.getTime());
        return response;
    }
}