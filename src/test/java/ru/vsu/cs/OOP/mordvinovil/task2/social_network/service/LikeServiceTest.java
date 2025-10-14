package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.LikeCommentRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.LikePostRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.LikeCommentResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.LikePostResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Comment;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Like;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Role;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.CommentNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.LikeNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.PostNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.CommentRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.LikeRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.PostRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LikeServiceTest {
    @Mock
    private LikeRepository likeRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ModelMapper modelMapper;

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
        when(likeRepository.save(any(Like.class))).thenReturn(like);
        when(modelMapper.map(like, LikeCommentResponse.class)).thenReturn(expectedResponse);

        LikeCommentResponse result = likeService.likeComment(currentUser, request);

        assertNotNull(result);

        verify(commentRepository).findById(comment.getId());
        verify(likeRepository).findByUserIdAndCommentId(currentUser.getId(), comment.getId());
        verify(likeRepository).save(any(Like.class));
        verify(modelMapper).map(like, LikeCommentResponse.class);
    }

    @Test
    void likeComment_whenCommentNotExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        LikeCommentRequest request = createTestLikeCommentRequest(1L);

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
        when(modelMapper.map(existingLike, LikeCommentResponse.class)).thenReturn(expectedResponse);

        LikeCommentResponse result = likeService.likeComment(currentUser, request);

        assertNotNull(result);

        verify(commentRepository).findById(comment.getId());
        verify(likeRepository).findByUserIdAndCommentId(currentUser.getId(), comment.getId());
        verify(likeRepository, never()).save(any(Like.class));
        verify(modelMapper).map(existingLike, LikeCommentResponse.class);
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
        when(likeRepository.save(any(Like.class))).thenReturn(like);
        when(modelMapper.map(like, LikePostResponse.class)).thenReturn(expectedResponse);

        LikePostResponse result = likeService.likePost(currentUser, request);

        assertNotNull(result);

        verify(postRepository).findById(post.getId());
        verify(likeRepository).findByUserIdAndPostId(currentUser.getId(), post.getId());
        verify(likeRepository).save(any(Like.class));
        verify(modelMapper).map(like, LikePostResponse.class);
    }

    @Test
    void likePost_whenPostNotExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        LikePostRequest request = createTestLikePostRequest(1L);

        PostNotFoundException postNotFoundException = assertThrows(PostNotFoundException.class,
                () -> likeService.likePost(currentUser, request));

        assertEquals(ResponseMessageConstants.NOT_FOUND, postNotFoundException.getMessage());
    }

    @Test
    void deleteLikeByComment_whenLikeExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        Comment comment = createTestComment(1L, "Test comment");
        Like like = createTestLike(currentUser, null, comment);
        LikeCommentResponse expectedResponse = createTestLikeCommentResponse(like);

        when(likeRepository.findByUserIdAndCommentId(currentUser.getId(), comment.getId())).thenReturn(Optional.of(like));
        when(modelMapper.map(like, LikeCommentResponse.class)).thenReturn(expectedResponse);

        LikeCommentResponse result = likeService.deleteLikeByComment(currentUser, comment.getId());

        assertNotNull(result);

        verify(likeRepository).findByUserIdAndCommentId(currentUser.getId(), comment.getId());
        verify(likeRepository).delete(like);
        verify(modelMapper).map(like, LikeCommentResponse.class);
    }

    @Test
    void deleteLikeByComment_whenLikeNotExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");

        when(likeRepository.findByUserIdAndCommentId(currentUser.getId(), 1L)).thenReturn(Optional.empty());

        LikeNotFoundException likeNotFoundException = assertThrows(LikeNotFoundException.class,
                () -> likeService.deleteLikeByComment(currentUser, 1L));

        assertEquals(ResponseMessageConstants.NOT_FOUND, likeNotFoundException.getMessage());
    }

    @Test
    void deleteLikeByPost_whenLikeExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        Post post = createTestPost(currentUser, "Test post", null);
        Like like = createTestLike(currentUser, post, null);
        LikePostResponse expectedResponse = createTestLikePostResponse(like);

        when(likeRepository.findByUserIdAndPostId(currentUser.getId(), post.getId())).thenReturn(Optional.of(like));
        when(modelMapper.map(like, LikePostResponse.class)).thenReturn(expectedResponse);

        LikePostResponse result = likeService.deleteLikeByPost(currentUser, post.getId());

        assertNotNull(result);

        verify(likeRepository).findByUserIdAndPostId(currentUser.getId(), post.getId());
        verify(likeRepository).delete(like);
        verify(modelMapper).map(like, LikePostResponse.class);
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
                .map(this::createTestLikePostResponse)
                .toList();

        when(likeRepository.findByPostId(post.getId())).thenReturn(likes);
        when(modelMapper.map(any(Like.class), eq(LikePostResponse.class)))
                .thenReturn(expectedResponses.get(0), expectedResponses.get(1));

        List<LikePostResponse> result = likeService.getLikesByPost(post.getId());

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(likeRepository).findByPostId(post.getId());
        verify(modelMapper, times(2)).map(any(Like.class), eq(LikePostResponse.class));
    }

    @Test
    void getLikesByComment() {
        Comment comment = createTestComment(1L, "Test comment");
        List<Like> likes = List.of(
                createTestLike(createTestUser(1L, "user1", "user1@example.com"), null, comment),
                createTestLike(createTestUser(2L, "user2", "user2@example.com"), null, comment)
        );
        List<LikeCommentResponse> expectedResponses = likes.stream()
                .map(this::createTestLikeCommentResponse)
                .toList();

        when(likeRepository.findByCommentId(comment.getId())).thenReturn(likes);
        when(modelMapper.map(any(Like.class), eq(LikeCommentResponse.class)))
                .thenReturn(expectedResponses.get(0), expectedResponses.get(1));

        List<LikeCommentResponse> result = likeService.getLikesByComment(comment.getId());

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(likeRepository).findByCommentId(comment.getId());
        verify(modelMapper, times(2)).map(any(Like.class), eq(LikeCommentResponse.class));
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

    private Comment createTestComment(Long id, String content) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setContent(content);
        comment.setCreator(createTestUser(1L, "commentCreator", "creator@example.com"));
        comment.setTime(LocalDateTime.now());
        return comment;
    }

    private Like createTestLike(User user, Post post, Comment comment) {
        return Like.builder()
                .user(user)
                .post(post)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private LikeCommentRequest createTestLikeCommentRequest(Long commentId) {
        LikeCommentRequest request = new LikeCommentRequest();
        request.setCommentId(commentId);
        return request;
    }

    private LikePostRequest createTestLikePostRequest(Long postId) {
        LikePostRequest request = new LikePostRequest();
        request.setPostId(postId);
        return request;
    }

    private LikeCommentResponse createTestLikeCommentResponse(Like like) {
        LikeCommentResponse response = new LikeCommentResponse();
        response.setId(like.getId());
        response.setUserId(like.getUser().getId());
        response.setUsername(like.getUser().getUsername());
        response.setCommentId(like.getComment().getId());
        response.setCreatedAt(like.getCreatedAt());
        return response;
    }

    private LikePostResponse createTestLikePostResponse(Like like) {
        LikePostResponse response = new LikePostResponse();
        response.setId(like.getId());
        response.setUserId(like.getUser().getId());
        response.setUsername(like.getUser().getUsername());
        response.setPostId(like.getPost().getId());
        response.setCreatedAt(like.getCreatedAt());
        return response;
    }
}