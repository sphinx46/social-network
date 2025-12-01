package ru.cs.vsu.social_network.contents_service.service.aggregatorImpl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import ru.cs.vsu.social_network.contents_service.dto.response.content.CommentDetailsResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.content.LikeCommentResponse;
import ru.cs.vsu.social_network.contents_service.entity.Comment;
import ru.cs.vsu.social_network.contents_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.contents_service.provider.LikeCommentEntityProvider;
import ru.cs.vsu.social_network.contents_service.service.aggregator.aggregatorImpl.CommentDetailsAggregatorImpl;
import ru.cs.vsu.social_network.contents_service.service.batch.BatchLikeCommentService;
import ru.cs.vsu.social_network.contents_service.utils.TestDataFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для {@link CommentDetailsAggregatorImpl}.
 */
@ExtendWith(MockitoExtension.class)
class CommentDetailsAggregatorImplTest {

    private static final UUID COMMENT_ID = UUID.fromString("40d4dde4-663d-4113-bd08-25f5e1d42efe");
    private static final UUID POST_ID = UUID.fromString("8dce7d87-60d9-4b66-8851-e7dba4684538");
    private static final UUID USER_ID = UUID.fromString("3fc4530f-5c0c-429d-8a64-810ce3bd1415");

    @Mock
    private EntityMapper mapper;
    @Mock
    private LikeCommentEntityProvider likeCommentEntityProvider;
    @Mock
    private BatchLikeCommentService batchLikeCommentService;

    @InjectMocks
    private CommentDetailsAggregatorImpl commentDetailsAggregator;

    @Test
    @DisplayName("Агрегация деталей комментария - с лайками")
    void aggregateCommentDetails_whenIncludeLikes_shouldReturnResponseWithLikes() {
        Comment comment = TestDataFactory.createCommentEntity(COMMENT_ID, USER_ID, POST_ID,
                "Test comment", null);
        CommentDetailsResponse baseResponse = TestDataFactory.createCommentDetailsResponse(COMMENT_ID);

        Long likesCount = 5L;
        List<LikeCommentResponse> likes = List.of(
                TestDataFactory.createLikeCommentResponse(UUID.randomUUID(), USER_ID, COMMENT_ID)
        );

        when(mapper.map(comment, CommentDetailsResponse.class)).thenReturn(baseResponse);
        when(likeCommentEntityProvider.getLikesCountByComment(COMMENT_ID)).thenReturn(likesCount);
        when(batchLikeCommentService.getLikesForComment(COMMENT_ID, 10)).thenReturn(likes);

        CommentDetailsResponse result = commentDetailsAggregator.aggregateCommentDetails(comment,
                true, 10);

        assertNotNull(result);
        assertEquals(COMMENT_ID, result.getId());
        assertEquals(likesCount, result.getLikesCount());
        assertEquals(1, result.getLikes().size());

        verify(mapper).map(comment, CommentDetailsResponse.class);
        verify(likeCommentEntityProvider).getLikesCountByComment(COMMENT_ID);
        verify(batchLikeCommentService).getLikesForComment(COMMENT_ID, 10);
    }

    @Test
    @DisplayName("Агрегация деталей комментария - без лайков")
    void aggregateCommentDetails_whenExcludeLikes_shouldReturnResponseWithoutLikes() {
        Comment comment = TestDataFactory.createCommentEntity(COMMENT_ID, USER_ID, POST_ID,
                "Test comment", null);
        CommentDetailsResponse baseResponse = TestDataFactory.createCommentDetailsResponse(COMMENT_ID);

        Long likesCount = 5L;

        when(mapper.map(comment, CommentDetailsResponse.class)).thenReturn(baseResponse);
        when(likeCommentEntityProvider.getLikesCountByComment(COMMENT_ID)).thenReturn(likesCount);

        CommentDetailsResponse result = commentDetailsAggregator.aggregateCommentDetails(comment,
                false, 0);

        assertNotNull(result);
        assertEquals(COMMENT_ID, result.getId());
        assertEquals(likesCount, result.getLikesCount());
        assertTrue(result.getLikes().isEmpty());

        verify(mapper).map(comment, CommentDetailsResponse.class);
        verify(likeCommentEntityProvider).getLikesCountByComment(COMMENT_ID);
        verify(batchLikeCommentService, never()).getLikesForComment(any(), anyInt());
    }

    @Test
    @DisplayName("Агрегация страницы комментариев - с лайками")
    void aggregateCommentsPage_whenIncludeLikes_shouldReturnPageWithLikes() {
        List<UUID> commentIds = TestDataFactory.createCommentIds(3);
        List<Comment> comments = commentIds.stream()
                .map(commentId -> TestDataFactory.createCommentEntity(commentId, USER_ID, POST_ID,
                        "Comment " + commentId, null))
                .toList();
        Page<Comment> commentsPage = TestDataFactory.createPage(comments);

        Map<UUID, Long> likesCounts = TestDataFactory.createCommentLikesCountMap(commentIds);
        Map<UUID, List<LikeCommentResponse>> likesMap =
                TestDataFactory.createLikesForCommentsMap(commentIds, 5);

        comments.forEach(comment -> {
            CommentDetailsResponse response = CommentDetailsResponse.builder()
                    .id(comment.getId())
                    .ownerId(comment.getOwnerId())
                    .postId(comment.getPost().getId())
                    .content(comment.getContent())
                    .imageUrl(comment.getImageUrl())
                    .createdAt(comment.getCreatedAt())
                    .updatedAt(comment.getUpdatedAt())
                    .build();
            when(mapper.map(comment, CommentDetailsResponse.class)).thenReturn(response);
        });

        when(batchLikeCommentService.getLikesCountsForComments(commentIds)).thenReturn(likesCounts);
        when(batchLikeCommentService.getLikesForComments(commentIds, 5)).thenReturn(likesMap);

        Page<CommentDetailsResponse> result = commentDetailsAggregator
                .aggregateCommentsPage(commentsPage, true, 5);

        assertNotNull(result);
        assertEquals(3, result.getContent().size());

        result.getContent().forEach(commentDetails -> {
            assertTrue(commentIds.contains(commentDetails.getId()));
            assertEquals(3L, commentDetails.getLikesCount());
            assertFalse(commentDetails.getLikes().isEmpty());
        });

        verify(batchLikeCommentService).getLikesCountsForComments(commentIds);
        verify(batchLikeCommentService).getLikesForComments(commentIds, 5);
    }

    @Test
    @DisplayName("Агрегация страницы комментариев - без лайков")
    void aggregateCommentsPage_whenExcludeLikes_shouldReturnPageWithoutLikes() {
        List<UUID> commentIds = TestDataFactory.createCommentIds(2);
        List<Comment> comments = commentIds.stream()
                .map(commentId -> TestDataFactory.createCommentEntity(commentId, USER_ID, POST_ID,
                        "Comment " + commentId, null))
                .toList();
        Page<Comment> commentsPage = TestDataFactory.createPage(comments);

        Map<UUID, Long> likesCounts = TestDataFactory.createCommentLikesCountMap(commentIds);

        comments.forEach(comment -> {
            CommentDetailsResponse response = CommentDetailsResponse.builder()
                    .id(comment.getId())
                    .ownerId(comment.getOwnerId())
                    .postId(comment.getPost().getId())
                    .content(comment.getContent())
                    .imageUrl(comment.getImageUrl())
                    .createdAt(comment.getCreatedAt())
                    .updatedAt(comment.getUpdatedAt())
                    .build();
            when(mapper.map(comment, CommentDetailsResponse.class)).thenReturn(response);
        });

        when(batchLikeCommentService.getLikesCountsForComments(commentIds)).thenReturn(likesCounts);

        Page<CommentDetailsResponse> result = commentDetailsAggregator
                .aggregateCommentsPage(commentsPage, false, 0);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());

        result.getContent().forEach(commentDetails -> {
            assertTrue(commentIds.contains(commentDetails.getId()));
            assertEquals(3L, commentDetails.getLikesCount());
            assertTrue(commentDetails.getLikes().isEmpty());
        });

        verify(batchLikeCommentService).getLikesCountsForComments(commentIds);
        verify(batchLikeCommentService, never()).getLikesForComments(any(), anyInt());
    }

    @Test
    @DisplayName("Агрегация страницы комментариев - пустая страница")
    void aggregateCommentsPage_whenEmptyPage_shouldReturnEmptyPage() {
        Page<Comment> emptyPage = Page.empty();

        Page<CommentDetailsResponse> result = commentDetailsAggregator
                .aggregateCommentsPage(emptyPage, true, 5);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(batchLikeCommentService, never()).getLikesCountsForComments(any());
        verify(batchLikeCommentService, never()).getLikesForComments(any(), anyInt());
    }

    @Test
    @DisplayName("Агрегация деталей комментария - нулевое количество лайков")
    void aggregateCommentDetails_whenZeroLikes_shouldReturnResponseWithZeroLikes() {
        Comment comment = TestDataFactory.createCommentEntity(COMMENT_ID, USER_ID, POST_ID,
                "Test comment", null);
        CommentDetailsResponse baseResponse = TestDataFactory.createCommentDetailsResponse(COMMENT_ID);

        Long likesCount = 0L;

        when(mapper.map(comment, CommentDetailsResponse.class)).thenReturn(baseResponse);
        when(likeCommentEntityProvider.getLikesCountByComment(COMMENT_ID)).thenReturn(likesCount);
        when(batchLikeCommentService.getLikesForComment(COMMENT_ID, 5)).thenReturn(List.of());

        CommentDetailsResponse result = commentDetailsAggregator.aggregateCommentDetails(comment,
                true, 5);

        assertNotNull(result);
        assertEquals(COMMENT_ID, result.getId());
        assertEquals(likesCount, result.getLikesCount());
        assertTrue(result.getLikes().isEmpty());

        verify(mapper).map(comment, CommentDetailsResponse.class);
        verify(likeCommentEntityProvider).getLikesCountByComment(COMMENT_ID);
        verify(batchLikeCommentService).getLikesForComment(COMMENT_ID, 5);
    }

    @Test
    @DisplayName("Агрегация страницы комментариев - комментарий без лайков")
    void aggregateCommentsPage_whenSomeCommentsHaveNoLikes_shouldHandleCorrectly() {
        UUID commentIdWithLikes = UUID.randomUUID();
        UUID commentIdWithoutLikes = UUID.randomUUID();
        List<UUID> commentIds = List.of(commentIdWithLikes, commentIdWithoutLikes);

        List<Comment> comments = commentIds.stream()
                .map(commentId -> TestDataFactory.createCommentEntity(commentId, USER_ID, POST_ID,
                        "Comment " + commentId, null))
                .toList();
        Page<Comment> commentsPage = TestDataFactory.createPage(comments);

        Map<UUID, Long> likesCounts = Map.of(
                commentIdWithLikes, 3L,
                commentIdWithoutLikes, 0L
        );

        Map<UUID, List<LikeCommentResponse>> likesMap = Map.of(
                commentIdWithLikes, List.of(
                        TestDataFactory.createLikeCommentResponse(UUID.randomUUID(), USER_ID, commentIdWithLikes)
                ),
                commentIdWithoutLikes, List.of()
        );

        comments.forEach(comment -> {
            CommentDetailsResponse response = CommentDetailsResponse.builder()
                    .id(comment.getId())
                    .ownerId(comment.getOwnerId())
                    .postId(comment.getPost().getId())
                    .content(comment.getContent())
                    .imageUrl(comment.getImageUrl())
                    .createdAt(comment.getCreatedAt())
                    .updatedAt(comment.getUpdatedAt())
                    .build();
            when(mapper.map(comment, CommentDetailsResponse.class)).thenReturn(response);
        });

        when(batchLikeCommentService.getLikesCountsForComments(commentIds)).thenReturn(likesCounts);
        when(batchLikeCommentService.getLikesForComments(commentIds, 5)).thenReturn(likesMap);

        Page<CommentDetailsResponse> result = commentDetailsAggregator
                .aggregateCommentsPage(commentsPage, true, 5);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());

        result.getContent().forEach(commentDetails -> {
            if (commentDetails.getId().equals(commentIdWithLikes)) {
                assertEquals(3L, commentDetails.getLikesCount());
                assertFalse(commentDetails.getLikes().isEmpty());
            } else {
                assertEquals(0L, commentDetails.getLikesCount());
                assertTrue(commentDetails.getLikes().isEmpty());
            }
        });

        verify(batchLikeCommentService).getLikesCountsForComments(commentIds);
        verify(batchLikeCommentService).getLikesForComments(commentIds, 5);
    }
}