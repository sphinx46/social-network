package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.LikeCommentRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.LikePostRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.LikeCommentResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.LikePostResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Comment;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Like;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.EventPublisherService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.like.LikeNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.LikeRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.LikeServiceImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.LikeFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.LikeValidator;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory.*;

@ExtendWith(MockitoExtension.class)
public class LikeServiceImplTest {
    @Mock
    private LikeRepository likeRepository;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private EntityUtils entityUtils;

    @Mock
    private LikeFactory likeFactory;

    @Mock
    private LikeValidator likeValidator;

    @Mock
    private EventPublisherService eventPublisherService;

    @InjectMocks
    private LikeServiceImpl likeServiceImpl;

    @Test
    void likeComment_whenRequestIsValid() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        User commentOwner = createTestUser(2L, "commentOwner", "owner@example.com");
        LikeCommentRequest request = createTestLikeCommentRequest(1L);
        Comment comment = createTestComment(1L, "Test comment");
        comment.setCreator(commentOwner);
        Like like = createTestLike(currentUser, null, comment);
        LikeCommentResponse expectedResponse = createTestLikeCommentResponse(like);

        when(entityUtils.getComment(request.getCommentId())).thenReturn(comment);
        when(likeFactory.createCommentLike(currentUser, request.getCommentId())).thenReturn(like);
        when(likeRepository.save(any(Like.class))).thenReturn(like);
        when(entityMapper.map(like, LikeCommentResponse.class)).thenReturn(expectedResponse);

        LikeCommentResponse result = likeServiceImpl.likeComment(currentUser, request);

        assertNotNull(result);

        verify(likeValidator).validate(request, currentUser);
        verify(entityUtils).getComment(request.getCommentId());
        verify(likeFactory).createCommentLike(currentUser, request.getCommentId());
        verify(likeRepository).save(any(Like.class));
        verify(eventPublisherService).publishCommentLiked(any(), eq(commentOwner.getId()),
                eq(comment.getId()), eq(currentUser.getId()));
        verify(entityMapper).map(like, LikeCommentResponse.class);
    }

    @Test
    void likePost_whenRequestIsValid() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        User postOwner = createTestUser(2L, "postOwner", "owner@example.com");
        LikePostRequest request = createTestLikePostRequest(1L);
        Post post = createTestPost(postOwner, "Test post", null);
        Like like = createTestLike(currentUser, post, null);
        LikePostResponse expectedResponse = createTestLikePostResponse(like);

        when(entityUtils.getPost(request.getPostId())).thenReturn(post);
        when(likeFactory.createPostLike(currentUser, request.getPostId())).thenReturn(like);
        when(likeRepository.save(any(Like.class))).thenReturn(like);
        when(entityMapper.map(like, LikePostResponse.class)).thenReturn(expectedResponse);

        LikePostResponse result = likeServiceImpl.likePost(currentUser, request);

        assertNotNull(result);

        verify(likeValidator).validate(request, currentUser);
        verify(entityUtils).getPost(request.getPostId());
        verify(likeFactory).createPostLike(currentUser, request.getPostId());
        verify(likeRepository).save(any(Like.class));
        verify(eventPublisherService).publishPostLiked(any(), eq(postOwner.getId()),
                eq(post.getId()), eq(currentUser.getId()));
        verify(entityMapper).map(like, LikePostResponse.class);
    }

    @Test
    void getLikesByPost() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        Post post = createTestPost(currentUser, "Test post", null);
        List<Like> likes = List.of(
                createTestLike(createTestUser(1L, "user1", "user1@example.com"), post, null),
                createTestLike(createTestUser(2L, "user2", "user2@example.com"), post, null)
        );
        List<LikePostResponse> expectedResponses = List.of(
                createTestLikePostResponse(likes.get(0)),
                createTestLikePostResponse(likes.get(1))
        );
        Page<Like> likePage = new PageImpl<>(likes);
        PageRequest pageRequest = PageRequest.builder()
                .pageNumber(0)
                .size(10)
                .direction(Sort.Direction.DESC)
                .sortBy("createdAt")
                .build();

        when(likeRepository.findByPostId(eq(1L), any(org.springframework.data.domain.PageRequest.class))).thenReturn(likePage);

        PageResponse<LikePostResponse> result = likeServiceImpl.getLikesByPost(1L, pageRequest);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());

        verify(likeRepository).findByPostId(eq(1L), any(org.springframework.data.domain.PageRequest.class));
    }

    @Test
    void getLikesByComment() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        Comment comment = createTestComment(1L, "Test comment");
        List<Like> likes = List.of(
                createTestLike(createTestUser(1L, "user1", "user1@example.com"), null, comment),
                createTestLike(createTestUser(2L, "user2", "user2@example.com"), null, comment)
        );
        List<LikeCommentResponse> expectedResponses = List.of(
                createTestLikeCommentResponse(likes.get(0)),
                createTestLikeCommentResponse(likes.get(1))
        );
        Page<Like> likePage = new PageImpl<>(likes);
        PageRequest pageRequest = PageRequest.builder()
                .pageNumber(0)
                .size(10)
                .direction(Sort.Direction.DESC)
                .sortBy("createdAt")
                .build();

        when(likeRepository.findByCommentId(eq(1L), any(org.springframework.data.domain.PageRequest.class))).thenReturn(likePage);

        PageResponse<LikeCommentResponse> result = likeServiceImpl.getLikesByComment(1L, pageRequest);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());

        verify(likeRepository).findByCommentId(eq(1L), any(org.springframework.data.domain.PageRequest.class));
    }

    @Test
    void deleteLikeByComment_whenLikeExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        Comment comment = createTestComment(1L, "Test comment");
        Like like = createTestLike(currentUser, null, comment);
        LikeCommentResponse expectedResponse = createTestLikeCommentResponse(like);

        when(likeRepository.findByUserIdAndCommentId(currentUser.getId(), 1L)).thenReturn(Optional.of(like));
        when(entityMapper.map(like, LikeCommentResponse.class)).thenReturn(expectedResponse);

        LikeCommentResponse result = likeServiceImpl.deleteLikeByComment(currentUser, 1L);

        assertNotNull(result);

        verify(likeValidator).validateLikeDeletion(1L, "comment", currentUser);
        verify(likeRepository).findByUserIdAndCommentId(currentUser.getId(), 1L);
        verify(likeRepository).delete(like);
        verify(entityMapper).map(like, LikeCommentResponse.class);
    }

    @Test
    void deleteLikeByComment_whenLikeNotExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");

        when(likeRepository.findByUserIdAndCommentId(currentUser.getId(), 1L)).thenReturn(Optional.empty());

        LikeNotFoundException likeNotFoundException = assertThrows(LikeNotFoundException.class,
                () -> likeServiceImpl.deleteLikeByComment(currentUser, 1L));

        assertEquals(ResponseMessageConstants.FAILURE_LIKE_NOT_FOUND, likeNotFoundException.getMessage());

        verify(likeValidator).validateLikeDeletion(1L, "comment", currentUser);
        verify(likeRepository).findByUserIdAndCommentId(currentUser.getId(), 1L);
    }

    @Test
    void deleteLikeByPost_whenLikeExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        Post post = createTestPost(currentUser, "Test post", null);
        Like like = createTestLike(currentUser, post, null);
        LikePostResponse expectedResponse = createTestLikePostResponse(like);

        when(likeRepository.findByUserIdAndPostId(currentUser.getId(), 1L)).thenReturn(Optional.of(like));
        when(entityMapper.map(like, LikePostResponse.class)).thenReturn(expectedResponse);

        LikePostResponse result = likeServiceImpl.deleteLikeByPost(currentUser, 1L);

        assertNotNull(result);

        verify(likeValidator).validateLikeDeletion(1L, "post", currentUser);
        verify(likeRepository).findByUserIdAndPostId(currentUser.getId(), 1L);
        verify(likeRepository).delete(like);
        verify(entityMapper).map(like, LikePostResponse.class);
    }

    @Test
    void deleteLikeByPost_whenLikeNotExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");

        when(likeRepository.findByUserIdAndPostId(currentUser.getId(), 1L)).thenReturn(Optional.empty());

        LikeNotFoundException likeNotFoundException = assertThrows(LikeNotFoundException.class,
                () -> likeServiceImpl.deleteLikeByPost(currentUser, 1L));

        assertEquals(ResponseMessageConstants.FAILURE_LIKE_NOT_FOUND, likeNotFoundException.getMessage());

        verify(likeValidator).validateLikeDeletion(1L, "post", currentUser);
        verify(likeRepository).findByUserIdAndPostId(currentUser.getId(), 1L);
    }
}