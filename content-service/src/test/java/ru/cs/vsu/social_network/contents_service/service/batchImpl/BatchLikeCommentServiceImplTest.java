package ru.cs.vsu.social_network.contents_service.service.batchImpl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.cs.vsu.social_network.contents_service.dto.response.content.LikeCommentResponse;
import ru.cs.vsu.social_network.contents_service.entity.LikeComment;
import ru.cs.vsu.social_network.contents_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.contents_service.provider.LikeCommentEntityProvider;
import ru.cs.vsu.social_network.contents_service.service.batch.batchImpl.BatchLikeCommentServiceImpl;
import ru.cs.vsu.social_network.contents_service.utils.TestDataFactory;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchLikeCommentServiceImplTest {

    private static final UUID COMMENT_ID = UUID.fromString("40d4dde4-663d-4113-bd08-25f5e1d42efe");
    private static final UUID LIKE_ID = UUID.fromString("5ab3c6d7-ec6f-49ad-95ac-6c752ad8172e");

    @Mock
    private LikeCommentEntityProvider likeCommentEntityProvider;
    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private BatchLikeCommentServiceImpl batchLikeCommentService;

    @Test
    @DisplayName("Получение количества лайков для комментариев - успешно")
    void getLikesCountsForComments_whenCommentIdsProvided_shouldReturnCountsMap() {
        List<UUID> commentIds = TestDataFactory.createCommentIds(5);
        Map<UUID, Long> expectedMap = TestDataFactory.createCommentLikesCountMap(commentIds);

        when(likeCommentEntityProvider.getLikesCountsForComments(commentIds)).thenReturn(expectedMap);

        Map<UUID, Long> actual = batchLikeCommentService.getLikesCountsForComments(commentIds);

        assertNotNull(actual);
        assertEquals(expectedMap.size(), actual.size());
        commentIds.forEach(commentId -> assertTrue(actual.containsKey(commentId)));
        verify(likeCommentEntityProvider).getLikesCountsForComments(commentIds);
    }

    @Test
    @DisplayName("Получение количества лайков для комментариев - пустой список")
    void getLikesCountsForComments_whenEmptyList_shouldReturnEmptyMap() {
        List<UUID> emptyList = List.of();

        Map<UUID, Long> actual = batchLikeCommentService.getLikesCountsForComments(emptyList);

        assertNotNull(actual);
        assertTrue(actual.isEmpty());
        verify(likeCommentEntityProvider, never()).getLikesCountsForComments(any());
    }

    @Test
    @DisplayName("Получение количества лайков для комментариев - превышение максимального размера")
    void getLikesCountsForComments_whenExceedsMaxSize_shouldUseBatch() {
        List<UUID> commentIds = TestDataFactory.createCommentIds(1500);
        List<UUID> expectedBatch = commentIds.subList(0, 1000);
        Map<UUID, Long> expectedMap = TestDataFactory.createCommentLikesCountMap(expectedBatch);

        when(likeCommentEntityProvider.getLikesCountsForComments(any())).thenReturn(expectedMap);

        Map<UUID, Long> actual = batchLikeCommentService.getLikesCountsForComments(commentIds);

        assertNotNull(actual);
        assertEquals(1000, actual.size());
        verify(likeCommentEntityProvider, atLeastOnce()).getLikesCountsForComments(any());
    }

    @Test
    @DisplayName("Получение лайков для комментариев - успешно")
    void getLikesForComments_whenCommentIdsProvided_shouldReturnLikesMap() {
        List<UUID> commentIds = TestDataFactory.createCommentIds(2);
        int likesLimit = 5;

        List<LikeComment> mockLikes = new ArrayList<>();
        for (UUID commentId : commentIds) {
            for (int i = 0; i < 2; i++) {
                LikeComment like = TestDataFactory.createLikeCommentEntity(
                        UUID.randomUUID(),
                        TestDataFactory.TEST_USER_ID,
                        commentId
                );
                mockLikes.add(like);
            }
        }

        when(likeCommentEntityProvider.getRecentLikesForComments(commentIds, likesLimit))
                .thenReturn(mockLikes);

        when(entityMapper.map(any(LikeComment.class), eq(LikeCommentResponse.class)))
                .thenAnswer(invocation -> {
                    LikeComment like = invocation.getArgument(0);
                    return TestDataFactory.createLikeCommentResponse(
                            like.getId(),
                            like.getOwnerId(),
                            like.getComment().getId()
                    );
                });

        Map<UUID, List<LikeCommentResponse>> actual =
                batchLikeCommentService.getLikesForComments(commentIds, likesLimit);

        assertNotNull(actual);
        assertEquals(commentIds.size(), actual.size());
        commentIds.forEach(commentId -> {
            assertTrue(actual.containsKey(commentId));
            assertTrue(actual.get(commentId).size() <= 2);
            actual.get(commentId).forEach(likeResponse ->
                    assertEquals(commentId, likeResponse.getCommentId()));
        });
        verify(likeCommentEntityProvider).getRecentLikesForComments(commentIds, likesLimit);
    }

    @Test
    @DisplayName("Получение лайков для комментариев - лимит меньше 1")
    void getLikesForComments_whenLimitLessThanOne_shouldUseDefaultLimit() {
        List<UUID> commentIds = TestDataFactory.createCommentIds(2);
        int invalidLimit = 0;
        int effectiveLimit = 1;

        List<LikeComment> mockLikes = new ArrayList<>();
        for (UUID commentId : commentIds) {
            LikeComment like = TestDataFactory.createLikeCommentEntity(
                    UUID.randomUUID(),
                    TestDataFactory.TEST_USER_ID,
                    commentId
            );
            mockLikes.add(like);
        }

        when(likeCommentEntityProvider.getRecentLikesForComments(commentIds, effectiveLimit))
                .thenReturn(mockLikes);

        when(entityMapper.map(any(LikeComment.class), eq(LikeCommentResponse.class)))
                .thenAnswer(invocation -> {
                    LikeComment like = invocation.getArgument(0);
                    return TestDataFactory.createLikeCommentResponse(
                            like.getId(),
                            like.getOwnerId(),
                            like.getComment().getId()
                    );
                });

        Map<UUID, List<LikeCommentResponse>> actual =
                batchLikeCommentService.getLikesForComments(commentIds, invalidLimit);

        assertNotNull(actual);
        assertEquals(commentIds.size(), actual.size());
        commentIds.forEach(commentId -> {
            assertTrue(actual.containsKey(commentId));
            assertEquals(1, actual.get(commentId).size());
        });
        verify(likeCommentEntityProvider).getRecentLikesForComments(commentIds, effectiveLimit);
    }

    @Test
    @DisplayName("Получение лайков для комментария - успешно")
    void getLikesForComment_whenCommentExists_shouldReturnLikes() {
        int limit = 10;

        LikeComment like = TestDataFactory.createLikeCommentEntity(
                LIKE_ID,
                TestDataFactory.TEST_USER_ID,
                COMMENT_ID
        );

        List<LikeComment> likes = List.of(like);

        when(likeCommentEntityProvider.getRecentLikesForComment(COMMENT_ID, limit))
                .thenReturn(likes);

        LikeCommentResponse expectedResponse = TestDataFactory.createLikeCommentResponse(
                LIKE_ID,
                TestDataFactory.TEST_USER_ID,
                COMMENT_ID
        );

        when(entityMapper.map(like, LikeCommentResponse.class))
                .thenReturn(expectedResponse);

        List<LikeCommentResponse> actual = batchLikeCommentService.getLikesForComment(COMMENT_ID, limit);

        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertEquals(expectedResponse.getId(), actual.get(0).getId());
        assertEquals(expectedResponse.getCommentId(), actual.get(0).getCommentId());
        verify(likeCommentEntityProvider).getRecentLikesForComment(COMMENT_ID, limit);
    }

    @Test
    @DisplayName("Получение лайков для комментария - пустой результат")
    void getLikesForComment_whenNoLikes_shouldReturnEmptyList() {
        int limit = 5;
        List<LikeComment> emptyList = Collections.emptyList();

        when(likeCommentEntityProvider.getRecentLikesForComment(COMMENT_ID, limit))
                .thenReturn(emptyList);

        List<LikeCommentResponse> actual = batchLikeCommentService.getLikesForComment(COMMENT_ID, limit);

        assertNotNull(actual);
        assertTrue(actual.isEmpty());
        verify(likeCommentEntityProvider).getRecentLikesForComment(COMMENT_ID, limit);
        verify(entityMapper, never()).map(any(LikeComment.class), eq(LikeCommentResponse.class));
    }

    @Test
    @DisplayName("Получение лайков для комментариев - лайк без связанного комментария")
    void getLikesForComments_whenLikeWithoutComment_shouldSkipLike() {
        List<UUID> commentIds = List.of(COMMENT_ID);
        int likesLimit = 5;

        LikeComment likeWithoutComment = new LikeComment();
        likeWithoutComment.setId(LIKE_ID);
        likeWithoutComment.setOwnerId(TestDataFactory.TEST_USER_ID);
        likeWithoutComment.setComment(null);
        likeWithoutComment.setCreatedAt(java.time.LocalDateTime.now());

        when(likeCommentEntityProvider.getRecentLikesForComments(commentIds, likesLimit))
                .thenReturn(List.of(likeWithoutComment));

        Map<UUID, List<LikeCommentResponse>> actual =
                batchLikeCommentService.getLikesForComments(commentIds, likesLimit);

        assertNotNull(actual);
        assertTrue(actual.containsKey(COMMENT_ID));
        assertTrue(actual.get(COMMENT_ID).isEmpty());
        verify(likeCommentEntityProvider).getRecentLikesForComments(commentIds, likesLimit);
        verify(entityMapper, never()).map(any(LikeComment.class), eq(LikeCommentResponse.class));
    }

    @Test
    @DisplayName("Получение лайков с комментариями - успешно")
    void getLikesWithComments_whenCommentIdsProvided_shouldReturnLikes() {
        List<UUID> commentIds = TestDataFactory.createCommentIds(3);
        int limit = 5;

        List<LikeComment> mockLikes = new ArrayList<>();
        for (UUID commentId : commentIds) {
            LikeComment like = TestDataFactory.createLikeCommentEntity(
                    UUID.randomUUID(),
                    TestDataFactory.TEST_USER_ID,
                    commentId
            );
            mockLikes.add(like);
        }

        when(likeCommentEntityProvider.getLikesWithComments(commentIds, limit))
                .thenReturn(mockLikes);

        when(entityMapper.map(any(LikeComment.class), eq(LikeCommentResponse.class)))
                .thenAnswer(invocation -> {
                    LikeComment like = invocation.getArgument(0);
                    return TestDataFactory.createLikeCommentResponse(
                            like.getId(),
                            like.getOwnerId(),
                            like.getComment().getId()
                    );
                });

        List<LikeCommentResponse> actual = batchLikeCommentService.getLikesWithComments(commentIds, limit);

        assertNotNull(actual);
        assertEquals(commentIds.size(), actual.size());
        verify(likeCommentEntityProvider).getLikesWithComments(commentIds, limit);
    }

    @Test
    @DisplayName("Получение статуса лайков для комментариев - успешно")
    void getLikesStatusForComments_whenCommentIdsProvided_shouldReturnStatusMap() {
        UUID ownerId = TestDataFactory.TEST_USER_ID;
        List<UUID> commentIds = TestDataFactory.createCommentIds(3);

        when(likeCommentEntityProvider.existsByOwnerIdAndCommentId(eq(ownerId), any(UUID.class)))
                .thenReturn(true);

        Map<UUID, Boolean> actual = batchLikeCommentService.getLikesStatusForComments(ownerId, commentIds);

        assertNotNull(actual);
        assertEquals(commentIds.size(), actual.size());
        commentIds.forEach(commentId -> assertTrue(actual.get(commentId)));
        verify(likeCommentEntityProvider, times(commentIds.size())).existsByOwnerIdAndCommentId(eq(ownerId), any(UUID.class));
    }
}