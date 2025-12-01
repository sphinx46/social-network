package ru.cs.vsu.social_network.contents_service.service.batchImpl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.cs.vsu.social_network.contents_service.dto.response.content.LikePostResponse;
import ru.cs.vsu.social_network.contents_service.entity.LikePost;
import ru.cs.vsu.social_network.contents_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.contents_service.provider.LikePostEntityProvider;
import ru.cs.vsu.social_network.contents_service.service.batch.batchImpl.BatchLikePostServiceImpl;
import ru.cs.vsu.social_network.contents_service.utils.TestDataFactory;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchLikePostServiceImplTest {

    private static final UUID POST_ID = UUID.fromString("8dce7d87-60d9-4b66-8851-e7dba4684538");
    private static final UUID LIKE_ID = UUID.fromString("5ab3c6d7-ec6f-49ad-95ac-6c752ad8172e");

    @Mock
    private LikePostEntityProvider likePostEntityProvider;
    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private BatchLikePostServiceImpl batchLikePostService;

    @Test
    @DisplayName("Получение количества лайков для постов - успешно")
    void getLikesCountsForPosts_whenPostIdsProvided_shouldReturnCountsMap() {
        List<UUID> postIds = TestDataFactory.createPostIds(5);
        Map<UUID, Long> expectedMap = TestDataFactory.createLikesCountMap(postIds);

        when(likePostEntityProvider.getLikesCountsForPosts(postIds)).thenReturn(expectedMap);

        Map<UUID, Long> actual = batchLikePostService.getLikesCountsForPosts(postIds);

        assertNotNull(actual);
        assertEquals(expectedMap.size(), actual.size());
        postIds.forEach(postId -> assertTrue(actual.containsKey(postId)));
        verify(likePostEntityProvider).getLikesCountsForPosts(postIds);
    }

    @Test
    @DisplayName("Получение количества лайков для постов - пустой список")
    void getLikesCountsForPosts_whenEmptyList_shouldReturnEmptyMap() {
        List<UUID> emptyList = List.of();

        Map<UUID, Long> actual = batchLikePostService.getLikesCountsForPosts(emptyList);

        assertNotNull(actual);
        assertTrue(actual.isEmpty());
        verify(likePostEntityProvider, never()).getLikesCountsForPosts(any());
    }

    @Test
    @DisplayName("Получение количества лайков для постов - превышение максимального размера")
    void getLikesCountsForPosts_whenExceedsMaxSize_shouldUseBatch() {
        List<UUID> postIds = TestDataFactory.createPostIds(1500);
        List<UUID> expectedBatch = postIds.subList(0, 1000);
        Map<UUID, Long> expectedMap = TestDataFactory.createLikesCountMap(expectedBatch);

        when(likePostEntityProvider.getLikesCountsForPosts(any())).thenReturn(expectedMap);

        Map<UUID, Long> actual = batchLikePostService.getLikesCountsForPosts(postIds);

        assertNotNull(actual);
        assertEquals(1000, actual.size());
        verify(likePostEntityProvider, atLeastOnce()).getLikesCountsForPosts(any());
    }

    @Test
    @DisplayName("Получение лайков для постов - успешно")
    void getLikesForPosts_whenPostIdsProvided_shouldReturnLikesMap() {
        List<UUID> postIds = TestDataFactory.createPostIds(2);
        int likesLimit = 5;

        List<LikePost> mockLikes = new ArrayList<>();
        for (UUID postId : postIds) {
            for (int i = 0; i < 2; i++) {
                LikePost like = TestDataFactory.createLikePostEntity(
                        UUID.randomUUID(),
                        i == 0 ? TestDataFactory.TEST_USER_ID : TestDataFactory.TEST_ANOTHER_USER_ID,
                        postId
                );
                mockLikes.add(like);
            }
        }

        when(likePostEntityProvider.getLikesWithPosts(postIds, likesLimit))
                .thenReturn(mockLikes);

        when(entityMapper.map(any(LikePost.class), eq(LikePostResponse.class))).thenAnswer(invocation -> {
            LikePost like = invocation.getArgument(0);
            return TestDataFactory.createLikePostResponse(
                    like.getId(),
                    like.getOwnerId(),
                    like.getPost().getId()
            );
        });

        Map<UUID, List<LikePostResponse>> actual =
                batchLikePostService.getLikesForPosts(postIds, likesLimit);

        assertNotNull(actual);
        assertEquals(postIds.size(), actual.size());
        postIds.forEach(postId -> {
            assertTrue(actual.containsKey(postId));
            assertTrue(actual.get(postId).size() <= 2);
            actual.get(postId).forEach(likeResponse ->
                    assertEquals(postId, likeResponse.getPostId()));
        });
        verify(likePostEntityProvider).getLikesWithPosts(postIds, likesLimit);
    }

    @Test
    @DisplayName("Получение лайков для постов - лимит меньше 1")
    void getLikesForPosts_whenLimitLessThanOne_shouldUseDefaultLimit() {
        List<UUID> postIds = TestDataFactory.createPostIds(2);
        int invalidLimit = 0;
        int effectiveLimit = 1;

        List<LikePost> mockLikes = new ArrayList<>();
        for (UUID postId : postIds) {
            LikePost like = TestDataFactory.createLikePostEntity(
                    UUID.randomUUID(),
                    TestDataFactory.TEST_USER_ID,
                    postId
            );
            mockLikes.add(like);
        }

        when(likePostEntityProvider.getLikesWithPosts(postIds, effectiveLimit))
                .thenReturn(mockLikes);

        when(entityMapper.map(any(LikePost.class), eq(LikePostResponse.class))).thenAnswer(invocation -> {
            LikePost like = invocation.getArgument(0);
            return TestDataFactory.createLikePostResponse(
                    like.getId(),
                    like.getOwnerId(),
                    like.getPost().getId()
            );
        });

        Map<UUID, List<LikePostResponse>> actual =
                batchLikePostService.getLikesForPosts(postIds, invalidLimit);

        assertNotNull(actual);
        assertEquals(postIds.size(), actual.size());
        postIds.forEach(postId -> {
            assertTrue(actual.containsKey(postId));
            assertEquals(1, actual.get(postId).size());
        });
        verify(likePostEntityProvider).getLikesWithPosts(postIds, effectiveLimit);
    }

    @Test
    @DisplayName("Получение лайков для поста - успешно")
    void getLikesForPost_whenPostExists_shouldReturnLikes() {
        int limit = 10;

        LikePost like = TestDataFactory.createLikePostEntity(
                LIKE_ID,
                TestDataFactory.TEST_USER_ID,
                POST_ID
        );

        List<LikePost> likes = List.of(like);

        when(likePostEntityProvider.getRecentLikesForPost(POST_ID, limit))
                .thenReturn(likes);

        LikePostResponse expectedResponse = TestDataFactory.createLikePostResponse(
                LIKE_ID,
                TestDataFactory.TEST_USER_ID,
                POST_ID
        );

        when(entityMapper.map(like, LikePostResponse.class))
                .thenReturn(expectedResponse);

        List<LikePostResponse> actual = batchLikePostService.getLikesForPost(POST_ID, limit);

        assertNotNull(actual);
        assertEquals(1, actual.size());
        assertEquals(expectedResponse.getId(), actual.get(0).getId());
        assertEquals(expectedResponse.getPostId(), actual.get(0).getPostId());
        verify(likePostEntityProvider).getRecentLikesForPost(POST_ID, limit);
    }

    @Test
    @DisplayName("Получение лайков для поста - пустой результат")
    void getLikesForPost_whenNoLikes_shouldReturnEmptyList() {
        int limit = 5;
        List<LikePost> emptyList = Collections.emptyList();

        when(likePostEntityProvider.getRecentLikesForPost(POST_ID, limit))
                .thenReturn(emptyList);

        List<LikePostResponse> actual = batchLikePostService.getLikesForPost(POST_ID, limit);

        assertNotNull(actual);
        assertTrue(actual.isEmpty());
        verify(likePostEntityProvider).getRecentLikesForPost(POST_ID, limit);
        verify(entityMapper, never()).map(any(LikePost.class), eq(LikePostResponse.class));
    }

    @Test
    @DisplayName("Получение лайков для постов - лайк без связанного поста")
    void getLikesForPosts_whenLikeWithoutPost_shouldSkipLike() {
        List<UUID> postIds = List.of(POST_ID);
        int likesLimit = 5;

        LikePost likeWithoutPost = new LikePost();
        likeWithoutPost.setId(LIKE_ID);
        likeWithoutPost.setOwnerId(TestDataFactory.TEST_USER_ID);
        likeWithoutPost.setPost(null);
        likeWithoutPost.setCreatedAt(java.time.LocalDateTime.now());

        when(likePostEntityProvider.getLikesWithPosts(postIds, likesLimit))
                .thenReturn(List.of(likeWithoutPost));

        Map<UUID, List<LikePostResponse>> actual =
                batchLikePostService.getLikesForPosts(postIds, likesLimit);

        assertNotNull(actual);
        assertTrue(actual.containsKey(POST_ID));
        assertTrue(actual.get(POST_ID).isEmpty());
        verify(likePostEntityProvider).getLikesWithPosts(postIds, likesLimit);
        verify(entityMapper, never()).map(any(LikePost.class), eq(LikePostResponse.class));
    }

    @Test
    @DisplayName("Получение статуса лайков для постов - успешно")
    void getLikesStatusForPosts_whenPostIdsProvided_shouldReturnStatusMap() {
        UUID ownerId = TestDataFactory.TEST_USER_ID;
        List<UUID> postIds = TestDataFactory.createPostIds(3);

        List<LikePost> userLikes = new ArrayList<>();
        for (UUID postId : postIds) {
            LikePost like = TestDataFactory.createLikePostEntity(
                    UUID.randomUUID(),
                    ownerId,
                    postId
            );
            userLikes.add(like);
        }

        when(likePostEntityProvider.getLikesByOwnerAndPosts(ownerId, postIds))
                .thenReturn(userLikes);

        Map<UUID, Boolean> actual = batchLikePostService.getLikesStatusForPosts(ownerId, postIds);

        assertNotNull(actual);
        assertEquals(postIds.size(), actual.size());
        postIds.forEach(postId -> assertTrue(actual.get(postId)));
        verify(likePostEntityProvider).getLikesByOwnerAndPosts(ownerId, postIds);
    }
}