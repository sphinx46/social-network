package ru.cs.vsu.social_network.contents_service.service.batchImpl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.cs.vsu.social_network.contents_service.dto.response.content.CommentResponse;
import ru.cs.vsu.social_network.contents_service.entity.Comment;
import ru.cs.vsu.social_network.contents_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.contents_service.provider.CommentEntityProvider;
import ru.cs.vsu.social_network.contents_service.service.batch.batchImpl.BatchCommentServiceImpl;
import ru.cs.vsu.social_network.contents_service.utils.TestDataFactory;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchCommentServiceImplTest {

    private static final UUID POST_ID = UUID.fromString("8dce7d87-60d9-4b66-8851-e7dba4684538");
    private static final UUID COMMENT_ID = UUID.fromString("40d4dde4-663d-4113-bd08-25f5e1d42efe");

    @Mock
    private CommentEntityProvider commentEntityProvider;
    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private BatchCommentServiceImpl batchCommentService;

    @Test
    @DisplayName("Получение количества комментариев для постов - успешно")
    void getCommentsCountsForPosts_whenPostIdsProvided_shouldReturnCountsMap() {
        List<UUID> postIds = TestDataFactory.createPostIds(5);
        Map<UUID, Long> expectedMap = TestDataFactory.createCommentsCountMap(postIds);

        when(commentEntityProvider.getCommentsCountsForPosts(postIds)).thenReturn(expectedMap);

        Map<UUID, Long> actual = batchCommentService.getCommentsCountsForPosts(postIds);

        assertNotNull(actual);
        assertEquals(expectedMap.size(), actual.size());
        postIds.forEach(postId -> assertTrue(actual.containsKey(postId)));
        verify(commentEntityProvider).getCommentsCountsForPosts(postIds);
    }

    @Test
    @DisplayName("Получение количества комментариев для постов - пустой список")
    void getCommentsCountsForPosts_whenEmptyList_shouldReturnEmptyMap() {
        List<UUID> emptyList = List.of();

        Map<UUID, Long> actual = batchCommentService.getCommentsCountsForPosts(emptyList);

        assertNotNull(actual);
        assertTrue(actual.isEmpty());
        verify(commentEntityProvider, never()).getCommentsCountsForPosts(any());
    }

    @Test
    @DisplayName("Получение количества комментариев для постов - превышение максимального размера")
    void getCommentsCountsForPosts_whenExceedsMaxSize_shouldUseBatch() {
        List<UUID> postIds = TestDataFactory.createPostIds(1500);
        List<UUID> expectedBatch = postIds.subList(0, 1000);
        Map<UUID, Long> expectedMap = TestDataFactory.createCommentsCountMap(expectedBatch);

        when(commentEntityProvider.getCommentsCountsForPosts(any())).thenReturn(expectedMap);

        Map<UUID, Long> actual = batchCommentService.getCommentsCountsForPosts(postIds);

        assertNotNull(actual);
        assertEquals(1000, actual.size());
        verify(commentEntityProvider, atLeastOnce()).getCommentsCountsForPosts(any());
    }

    @Test
    @DisplayName("Получение комментариев для постов - успешно")
    void getCommentsForPosts_whenPostIdsProvided_shouldReturnCommentsMap() {
        List<UUID> postIds = TestDataFactory.createPostIds(2);
        int commentsLimit = 5;

        List<Comment> mockComments = new ArrayList<>();
        for (UUID postId : postIds) {
            for (int i = 0; i < 2; i++) {
                Comment comment = TestDataFactory.createCommentEntity(
                        UUID.randomUUID(),
                        TestDataFactory.TEST_USER_ID,
                        postId,
                        "Comment " + (i + 1) + " for post " + postId,
                        null
                );
                mockComments.add(comment);
            }
        }

        when(commentEntityProvider.getRecentCommentsForPosts(postIds, commentsLimit))
                .thenReturn(mockComments);

        when(entityMapper.map(any(Comment.class), eq(CommentResponse.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            return TestDataFactory.createCommentResponse(
                    comment.getId(),
                    comment.getOwnerId(),
                    comment.getPost().getId()
            );
        });

        Map<UUID, List<CommentResponse>> actual =
                batchCommentService.getCommentsForPosts(postIds, commentsLimit);

        assertNotNull(actual);
        assertEquals(postIds.size(), actual.size());
        postIds.forEach(postId -> {
            assertTrue(actual.containsKey(postId));
            assertTrue(actual.get(postId).size() <= 2);
            actual.get(postId).forEach(commentResponse ->
                    assertEquals(postId, commentResponse.getPostId()));
        });
        verify(commentEntityProvider).getRecentCommentsForPosts(postIds, commentsLimit);
    }

    @Test
    @DisplayName("Получение комментариев для постов - лимит меньше 1")
    void getCommentsForPosts_whenLimitLessThanOne_shouldUseDefaultLimit() {
        List<UUID> postIds = TestDataFactory.createPostIds(2);
        int invalidLimit = 0;
        int effectiveLimit = 1;

        List<Comment> mockComments = new ArrayList<>();
        for (UUID postId : postIds) {
            Comment comment = TestDataFactory.createCommentEntity(
                    UUID.randomUUID(),
                    TestDataFactory.TEST_USER_ID,
                    postId,
                    "Comment for post " + postId,
                    null
            );
            mockComments.add(comment);
        }

        when(commentEntityProvider.getRecentCommentsForPosts(postIds, effectiveLimit))
                .thenReturn(mockComments);

        when(entityMapper.map(any(Comment.class), eq(CommentResponse.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            return TestDataFactory.createCommentResponse(
                    comment.getId(),
                    comment.getOwnerId(),
                    comment.getPost().getId()
            );
        });

        Map<UUID, List<CommentResponse>> actual =
                batchCommentService.getCommentsForPosts(postIds, invalidLimit);

        assertNotNull(actual);
        assertEquals(postIds.size(), actual.size());
        postIds.forEach(postId -> {
            assertTrue(actual.containsKey(postId));
            assertEquals(1, actual.get(postId).size());
        });
        verify(commentEntityProvider).getRecentCommentsForPosts(postIds, effectiveLimit);
    }

    @Test
    @DisplayName("Получение комментариев для поста - успешно")
    void getCommentsForPost_whenPostExists_shouldReturnComments() {
        int limit = 10;

        Comment comment = TestDataFactory.createCommentEntity(
                COMMENT_ID,
                TestDataFactory.TEST_USER_ID,
                POST_ID,
                "Test comment",
                null
        );

        List<Comment> comments = List.of(comment);

        when(commentEntityProvider.getRecentCommentsForPost(POST_ID, limit))
                .thenReturn(comments);

        CommentResponse expectedResponse = TestDataFactory.createCommentResponse(
                COMMENT_ID,
                TestDataFactory.TEST_USER_ID,
                POST_ID
        );

        when(entityMapper.map(comment, CommentResponse.class))
                .thenReturn(expectedResponse);

        List<CommentResponse> actual = batchCommentService.getCommentsForPost(POST_ID, limit);

        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertEquals(expectedResponse.getId(), actual.get(0).getId());
        assertEquals(expectedResponse.getPostId(), actual.get(0).getPostId());
        verify(commentEntityProvider).getRecentCommentsForPost(POST_ID, limit);
    }

    @Test
    @DisplayName("Получение комментариев для поста - пустой результат")
    void getCommentsForPost_whenNoComments_shouldReturnEmptyList() {
        int limit = 5;
        List<Comment> emptyList = Collections.emptyList();

        when(commentEntityProvider.getRecentCommentsForPost(POST_ID, limit))
                .thenReturn(emptyList);

        List<CommentResponse> actual = batchCommentService.getCommentsForPost(POST_ID, limit);

        assertNotNull(actual);
        assertTrue(actual.isEmpty());
        verify(commentEntityProvider).getRecentCommentsForPost(POST_ID, limit);
        verify(entityMapper, never()).map(any(Comment.class), eq(CommentResponse.class));
    }

    @Test
    @DisplayName("Получение комментариев для постов - комментарий без связанного поста")
    void getCommentsForPosts_whenCommentWithoutPost_shouldSkipComment() {
        List<UUID> postIds = List.of(POST_ID);
        int commentsLimit = 5;

        Comment commentWithoutPost = new Comment();
        commentWithoutPost.setId(COMMENT_ID);
        commentWithoutPost.setOwnerId(TestDataFactory.TEST_USER_ID);
        commentWithoutPost.setPost(null);
        commentWithoutPost.setContent("Test comment");
        commentWithoutPost.setCreatedAt(java.time.LocalDateTime.now());
        commentWithoutPost.setUpdatedAt(java.time.LocalDateTime.now());

        when(commentEntityProvider.getRecentCommentsForPosts(postIds, commentsLimit))
                .thenReturn(List.of(commentWithoutPost));

        Map<UUID, List<CommentResponse>> actual =
                batchCommentService.getCommentsForPosts(postIds, commentsLimit);

        assertNotNull(actual);
        assertTrue(actual.containsKey(POST_ID));
        assertTrue(actual.get(POST_ID).isEmpty());
        verify(commentEntityProvider).getRecentCommentsForPosts(postIds, commentsLimit);
        verify(entityMapper, never()).map(any(Comment.class), eq(CommentResponse.class));
    }

    @Test
    @DisplayName("Получение комментариев с постами - успешно")
    void getCommentsWithPosts_whenCommentIdsProvided_shouldReturnComments() {
        List<UUID> commentIds = TestDataFactory.createCommentIds(3);
        int limit = 5;

        List<Comment> mockComments = new ArrayList<>();
        for (UUID commentId : commentIds) {
            Comment comment = TestDataFactory.createCommentEntity(
                    commentId,
                    TestDataFactory.TEST_USER_ID,
                    POST_ID,
                    "Test comment",
                    null
            );
            mockComments.add(comment);
        }

        when(commentEntityProvider.getCommentsWithPosts(commentIds, limit))
                .thenReturn(mockComments);

        when(entityMapper.map(any(Comment.class), eq(CommentResponse.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            return TestDataFactory.createCommentResponse(
                    comment.getId(),
                    comment.getOwnerId(),
                    comment.getPost().getId()
            );
        });

        List<CommentResponse> actual = batchCommentService.getCommentsWithPosts(commentIds, limit);

        assertNotNull(actual);
        assertEquals(commentIds.size(), actual.size());
        verify(commentEntityProvider).getCommentsWithPosts(commentIds, limit);
    }
}