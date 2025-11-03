package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.post.CommentRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.post.CommentResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.common.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Comment;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.cache.CacheEventPublisherService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.notification.NotificationEventPublisherService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.comment.CommentNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.post.PostNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.CommentRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.content.CommentServiceImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.ContentFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.CommentValidator;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private ContentFactory contentFactory;

    @Mock
    private CommentValidator commentValidator;

    @Mock
    private EntityUtils entityUtils;

    @Mock
    private NotificationEventPublisherService notificationEventPublisherService;

    @Mock
    private CacheEventPublisherService cacheEventPublisherService;

    @InjectMocks
    private CommentServiceImpl commentServiceImpl;

    @Test
    void createComment_whenRequestIsValid() {
        User currentUser = createTestUser(1L, "user", "user@example.com");
        Post post = createTestPost(createTestUser(2L, "postOwner", "owner@example.com"), "Post content", null);
        CommentRequest request = createTestCommentRequest(post.getId(), "Comment content", null);
        Comment comment = createTestComment(null, currentUser, post, "Comment content", null);
        Comment savedComment = createTestComment(1L, currentUser, post, "Comment content", null);
        CommentResponse expectedResponse = createTestCommentResponse(savedComment);

        when(entityUtils.getPost(post.getId())).thenReturn(post);
        when(contentFactory.createComment(currentUser, post, request.getContent(), request.getImageUrl())).thenReturn(comment);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);
        when(entityMapper.mapWithName(savedComment, CommentResponse.class, "withLikes")).thenReturn(expectedResponse);

        CommentResponse result = commentServiceImpl.create(request, currentUser);

        assertNotNull(result);
        assertEquals(expectedResponse.getContent(), result.getContent());

        verify(commentValidator).validate(request, currentUser);
        verify(entityUtils).getPost(post.getId());
        verify(contentFactory).createComment(currentUser, post, request.getContent(), request.getImageUrl());
        verify(commentRepository).save(any(Comment.class));
        verify(notificationEventPublisherService).publishCommentAdded(any(), eq(post.getUser().getId()), eq(post.getId()), eq(savedComment.getId()));
        verify(cacheEventPublisherService).publishCommentCreated(any(), eq(savedComment), eq(post.getId()), eq(currentUser.getId()), eq(savedComment.getId()));
        verify(entityMapper).mapWithName(savedComment, CommentResponse.class, "withLikes");
    }

    @Test
    void createComment_whenPostNotExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        CommentRequest request = createTestCommentRequest(999L, "Test comment", null);

        when(entityUtils.getPost(999L)).thenThrow(new PostNotFoundException(ResponseMessageConstants.FAILURE_POST_NOT_FOUND));

        PostNotFoundException exception = assertThrows(PostNotFoundException.class,
                () -> commentServiceImpl.create(request, currentUser));

        assertEquals(ResponseMessageConstants.FAILURE_POST_NOT_FOUND, exception.getMessage());
        verify(entityUtils).getPost(999L);
        verifyNoInteractions(contentFactory, commentRepository, entityMapper, cacheEventPublisherService);
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
        when(commentRepository.save(any(Comment.class))).thenReturn(updatedComment);
        when(entityMapper.mapWithName(updatedComment, CommentResponse.class, "withLikes")).thenReturn(expectedResponse);

        CommentResponse result = commentServiceImpl.editComment(1L, request, currentUser);

        assertNotNull(result);
        assertEquals("Updated content", result.getContent());
        assertEquals("new.jpg", result.getImageUrl());

        verify(commentValidator).validateCommentUpdate(request, 1L, currentUser);
        verify(entityUtils).getComment(1L);
        verify(commentRepository).save(any(Comment.class));
        verify(cacheEventPublisherService).publishCommentEdit(any(), eq(updatedComment), eq(post.getId()), eq(updatedComment.getId()));
        verify(entityMapper).mapWithName(updatedComment, CommentResponse.class, "withLikes");
    }

    @Test
    void deleteComment_whenUserIsOwner() {
        User currentUser = createTestUser(1L, "user", "user@example.com");
        Post post = createTestPost(createTestUser(2L, "postOwner", "owner@example.com"), "Post content", null);
        Comment comment = createTestComment(1L, currentUser, post, "Comment content", null);

        when(entityUtils.getComment(1L)).thenReturn(comment);

        CompletableFuture<Boolean> result = commentServiceImpl.deleteComment(1L, currentUser);

        assertNotNull(result);
        assertTrue(result.join());

        verify(commentValidator).validateCommentOwnership(1L, currentUser);
        verify(entityUtils).getComment(1L);
        verify(commentRepository).delete(comment);
        verify(cacheEventPublisherService).publishCommentDeleted(any(), eq(comment), eq(post.getId()), eq(1L));
    }

    @Test
    void getCommentById_whenCommentExists() {
        User user = createTestUser(1L, "user", "user@example.com");
        Post post = createTestPost(createTestUser(2L, "postOwner", "owner@example.com"), "Post content", null);
        Comment comment = createTestComment(1L, user, post, "Comment content", null);
        CommentResponse expectedResponse = createTestCommentResponse(comment);

        when(entityUtils.getComment(1L)).thenReturn(comment);
        when(entityMapper.mapWithName(comment, CommentResponse.class, "withLikes")).thenReturn(expectedResponse);

        CommentResponse result = commentServiceImpl.getCommentById(1L);

        assertNotNull(result);
        assertEquals(expectedResponse.getId(), result.getId());

        verify(entityUtils).getComment(1L);
        verify(entityMapper).mapWithName(comment, CommentResponse.class, "withLikes");
    }

    @Test
    void getCommentById_whenCommentNotExists() {
        when(entityUtils.getComment(1L)).thenThrow(new CommentNotFoundException(ResponseMessageConstants.FAILURE_COMMENT_NOT_FOUND));

        CommentNotFoundException exception = assertThrows(CommentNotFoundException.class,
                () -> commentServiceImpl.getCommentById(1L));

        assertEquals(ResponseMessageConstants.FAILURE_COMMENT_NOT_FOUND, exception.getMessage());
        verify(entityUtils).getComment(1L);
        verifyNoInteractions(entityMapper);
    }

    @Test
    void getAllCommentsOnPost_whenValid() {
        Long postId = 1L;
        User user = createTestUser(1L, "user", "user@example.com");
        Post post = createTestPost(user, "Post content", null);
        Comment comment1 = createTestComment(1L, user, post, "Comment 1", null);
        Comment comment2 = createTestComment(2L, user, post, "Comment 2", null);
        List<Comment> comments = List.of(comment1, comment2);

        PageRequest pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Comment> commentPage = new PageImpl<>(comments, pageable, comments.size());

        CommentResponse response1 = createTestCommentResponse(comment1);
        CommentResponse response2 = createTestCommentResponse(comment2);

        when(entityUtils.getPost(postId)).thenReturn(post);
        when(commentRepository.findByPostIdWithLikes(eq(postId), any(org.springframework.data.domain.PageRequest.class))).thenReturn(commentPage);
        when(entityMapper.mapWithName(comment1, CommentResponse.class, "withLikes")).thenReturn(response1);
        when(entityMapper.mapWithName(comment2, CommentResponse.class, "withLikes")).thenReturn(response2);

        PageResponse<CommentResponse> result = commentServiceImpl.getAllCommentsOnPost(postId,
                ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.common.PageRequest.builder()
                        .pageNumber(0)
                        .size(10)
                        .sortBy("createdAt")
                        .direction(Sort.Direction.DESC)
                        .build());

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getCurrentPage());

        verify(entityUtils).getPost(postId);
        verify(commentRepository).findByPostIdWithLikes(eq(postId), any(org.springframework.data.domain.PageRequest.class));
        verify(entityMapper, times(2)).mapWithName(any(Comment.class), eq(CommentResponse.class), eq("withLikes"));
    }
}