package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.LikeCommentRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.LikePostRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.LikeCommentResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.LikePostResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Comment;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Like;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.custom.AccessDeniedException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.CommentNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.LikeNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.PostNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.CommentRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.LikeRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.PostRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.AccessValidator;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.LikeFactory;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory.*;

@ExtendWith(MockitoExtension.class)
public class LikeServiceTest {
    @Mock
    private LikeRepository likeRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private AccessValidator accessValidator;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private LikeFactory likeFactory;

    @InjectMocks
    private LikeService likeService;

    @Test
    void likeComment_whenRequestIsValid() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        Comment comment = createTestComment(1L, "Test comment");
        LikeCommentRequest request = createTestLikeCommentRequest(comment.getId());
        Like like = createTestLike(currentUser, null, comment);
        LikeCommentResponse expectedResponse = createTestLikeCommentResponse(like);

        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        when(likeRepository.findByUserIdAndCommentId(currentUser.getId(), comment.getId())).thenReturn(Optional.empty());
        when(likeFactory.createCommentLike(currentUser, comment)).thenReturn(like);
        when(likeRepository.save(any(Like.class))).thenReturn(like);
        when(entityMapper.map(like, LikeCommentResponse.class)).thenReturn(expectedResponse);

        LikeCommentResponse result = likeService.likeComment(currentUser, request);

        assertNotNull(result);

        verify(commentRepository).findById(comment.getId());
        verify(likeRepository).findByUserIdAndCommentId(currentUser.getId(), comment.getId());
        verify(likeFactory).createCommentLike(currentUser, comment);
        verify(likeRepository).save(any(Like.class));
        verify(entityMapper).map(like, LikeCommentResponse.class);
    }

    @Test
    void likeComment_whenCommentNotExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        LikeCommentRequest request = createTestLikeCommentRequest(1L);

        when(commentRepository.findById(request.getCommentId())).thenReturn(Optional.empty());

        CommentNotFoundException commentNotFoundException = assertThrows(CommentNotFoundException.class,
                () -> likeService.likeComment(currentUser, request));

        assertEquals(ResponseMessageConstants.NOT_FOUND, commentNotFoundException.getMessage());
    }

    @Test
    void likeComment_whenLikeAlreadyExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        Comment comment = createTestComment(1L, "Test comment");
        LikeCommentRequest request = createTestLikeCommentRequest(comment.getId());
        Like existingLike = createTestLike(currentUser, null, comment);
        LikeCommentResponse expectedResponse = createTestLikeCommentResponse(existingLike);

        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        when(likeRepository.findByUserIdAndCommentId(currentUser.getId(), comment.getId())).thenReturn(Optional.of(existingLike));
        when(entityMapper.map(existingLike, LikeCommentResponse.class)).thenReturn(expectedResponse);

        LikeCommentResponse result = likeService.likeComment(currentUser, request);

        assertNotNull(result);

        verify(commentRepository).findById(comment.getId());
        verify(likeRepository).findByUserIdAndCommentId(currentUser.getId(), comment.getId());
        verify(likeFactory, never()).createCommentLike(any(), any());
        verify(likeRepository, never()).save(any(Like.class));
        verify(entityMapper).map(existingLike, LikeCommentResponse.class);
    }

    @Test
    void likePost_whenRequestIsValid() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        Post post = createTestPost(currentUser, "Test post", null);
        LikePostRequest request = createTestLikePostRequest(post.getId());
        Like like = createTestLike(currentUser, post, null);
        LikePostResponse expectedResponse = createTestLikePostResponse(like);

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(likeRepository.findByUserIdAndPostId(currentUser.getId(), post.getId())).thenReturn(Optional.empty());
        when(likeFactory.createPostLike(currentUser, post)).thenReturn(like);
        when(likeRepository.save(any(Like.class))).thenReturn(like);
        when(entityMapper.map(like, LikePostResponse.class)).thenReturn(expectedResponse);

        LikePostResponse result = likeService.likePost(currentUser, request);

        assertNotNull(result);

        verify(postRepository).findById(post.getId());
        verify(likeRepository).findByUserIdAndPostId(currentUser.getId(), post.getId());
        verify(likeFactory).createPostLike(currentUser, post);
        verify(likeRepository).save(any(Like.class));
        verify(entityMapper).map(like, LikePostResponse.class);
    }

    @Test
    void likePost_whenPostNotExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        LikePostRequest request = createTestLikePostRequest(1L);

        when(postRepository.findById(request.getPostId())).thenReturn(Optional.empty());

        PostNotFoundException postNotFoundException = assertThrows(PostNotFoundException.class,
                () -> likeService.likePost(currentUser, request));

        assertEquals(ResponseMessageConstants.NOT_FOUND, postNotFoundException.getMessage());
    }

    @Test
    void getLikesByPost() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        Post post = createTestPost(currentUser, "Test post", null);
        List<Like> likes = List.of(
                createTestLike(createTestUser(1L, "user1", "user1@example.com"), post, null),
                createTestLike(createTestUser(2L, "user2", "user2@example.com"), post, null)
        );
        List<LikePostResponse> expectedResponses = likes.stream()
                .map(TestDataFactory::createTestLikePostResponse)
                .toList();

        when(likeRepository.findByPostId(post.getId())).thenReturn(likes);
        when(entityMapper.mapList(likes, LikePostResponse.class)).thenReturn(expectedResponses);

        List<LikePostResponse> result = likeService.getLikesByPost(post.getId());

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(likeRepository).findByPostId(post.getId());
        verify(entityMapper).mapList(likes, LikePostResponse.class);
    }

    @Test
    void getLikesByComment() {
        Comment comment = createTestComment(1L, "Test comment");
        List<Like> likes = List.of(
                createTestLike(createTestUser(1L, "user1", "user1@example.com"), null, comment),
                createTestLike(createTestUser(2L, "user2", "user2@example.com"), null, comment)
        );
        List<LikeCommentResponse> expectedResponses = likes.stream()
                .map(TestDataFactory::createTestLikeCommentResponse)
                .toList();

        when(likeRepository.findByCommentId(comment.getId())).thenReturn(likes);
        when(entityMapper.mapList(likes, LikeCommentResponse.class)).thenReturn(expectedResponses);

        List<LikeCommentResponse> result = likeService.getLikesByComment(comment.getId());

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(likeRepository).findByCommentId(comment.getId());
        verify(entityMapper).mapList(likes, LikeCommentResponse.class);
    }

    @Test
    void deleteLikeByComment_whenLikeExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        Comment comment = createTestComment(1L, "Test comment");
        Like like = createTestLike(currentUser, null, comment);
        LikeCommentResponse expectedResponse = createTestLikeCommentResponse(like);

        when(likeRepository.findByUserIdAndCommentId(currentUser.getId(), comment.getId())).thenReturn(Optional.of(like));
        when(entityMapper.map(like, LikeCommentResponse.class)).thenReturn(expectedResponse);
        doNothing().when(accessValidator).validateOwnership(currentUser, currentUser);

        LikeCommentResponse result = likeService.deleteLikeByComment(currentUser, comment.getId());

        assertNotNull(result);

        verify(likeRepository).findByUserIdAndCommentId(currentUser.getId(), comment.getId());
        verify(accessValidator).validateOwnership(currentUser, currentUser);
        verify(likeRepository).delete(like);
        verify(entityMapper).map(like, LikeCommentResponse.class);
    }

    @Test
    void deleteLikeByComment_whenLikeNotExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");

        when(likeRepository.findByUserIdAndCommentId(currentUser.getId(), 1L)).thenReturn(Optional.empty());

        LikeNotFoundException likeNotFoundException = assertThrows(LikeNotFoundException.class,
                () -> likeService.deleteLikeByComment(currentUser, 1L));

        assertEquals(ResponseMessageConstants.NOT_FOUND, likeNotFoundException.getMessage());

        verify(accessValidator, never()).validateOwnership(any(), any());
    }

    @Test
    void deleteLikeByPost_whenLikeExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        Post post = createTestPost(currentUser, "Test post", null);
        Like like = createTestLike(currentUser, post, null);
        LikePostResponse expectedResponse = createTestLikePostResponse(like);

        when(likeRepository.findByUserIdAndPostId(currentUser.getId(), post.getId())).thenReturn(Optional.of(like));
        when(entityMapper.map(like, LikePostResponse.class)).thenReturn(expectedResponse);
        doNothing().when(accessValidator).validateOwnership(currentUser, currentUser);

        LikePostResponse result = likeService.deleteLikeByPost(currentUser, post.getId());

        assertNotNull(result);

        verify(likeRepository).findByUserIdAndPostId(currentUser.getId(), post.getId());
        verify(accessValidator).validateOwnership(currentUser, currentUser);
        verify(likeRepository).delete(like);
        verify(entityMapper).map(like, LikePostResponse.class);
    }

    @Test
    void deleteLikeByComment_whenAccessDenied() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        User otherUser = createTestUser(2L, "other", "other@example.com");
        Comment comment = createTestComment(1L, "Test comment");
        Like like = createTestLike(otherUser, null, comment);

        when(likeRepository.findByUserIdAndCommentId(currentUser.getId(), comment.getId())).thenReturn(Optional.of(like));
        doThrow(new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED))
                .when(accessValidator).validateOwnership(currentUser, otherUser);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> likeService.deleteLikeByComment(currentUser, comment.getId()));

        assertEquals(ResponseMessageConstants.ACCESS_DENIED, exception.getMessage());

        verify(likeRepository).findByUserIdAndCommentId(currentUser.getId(), comment.getId());
        verify(accessValidator).validateOwnership(currentUser, otherUser);
        verify(likeRepository, never()).delete(any());
    }
}
