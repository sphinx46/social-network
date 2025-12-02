package ru.cs.vsu.social_network.contents_service.service.aggregatorImpl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import ru.cs.vsu.social_network.contents_service.dto.response.content.CommentResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.content.LikePostResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.content.PostDetailsResponse;
import ru.cs.vsu.social_network.contents_service.entity.Post;
import ru.cs.vsu.social_network.contents_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.contents_service.provider.CommentEntityProvider;
import ru.cs.vsu.social_network.contents_service.provider.LikePostEntityProvider;
import ru.cs.vsu.social_network.contents_service.service.aggregator.aggregatorImpl.PostDetailsAggregatorImpl;
import ru.cs.vsu.social_network.contents_service.service.batch.BatchCommentService;
import ru.cs.vsu.social_network.contents_service.service.batch.BatchLikePostService;
import ru.cs.vsu.social_network.contents_service.utils.TestDataFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для {@link PostDetailsAggregatorImpl}.
 */
@ExtendWith(MockitoExtension.class)
class PostDetailsAggregatorImplTest {

    private static final UUID POST_ID = UUID.fromString("8dce7d87-60d9-4b66-8851-e7dba4684538");
    private static final UUID USER_ID = UUID.fromString("3fc4530f-5c0c-429d-8a64-810ce3bd1415");

    @Mock
    private EntityMapper mapper;
    @Mock
    private CommentEntityProvider commentEntityProvider;
    @Mock
    private LikePostEntityProvider likePostEntityProvider;
    @Mock
    private BatchCommentService batchCommentService;
    @Mock
    private BatchLikePostService batchLikePostService;

    @InjectMocks
    private PostDetailsAggregatorImpl postDetailsAggregator;

    @Test
    @DisplayName("Агрегация деталей поста - с комментариями и лайками")
    void aggregatePostDetails_whenIncludeCommentsAndLikes_shouldReturnResponseWithAllData() {
        Post post = TestDataFactory.createPostEntity(POST_ID, USER_ID, "Test messaging",
                "http://example.com/image.jpg");
        PostDetailsResponse baseResponse = TestDataFactory.createPostDetailsResponse(POST_ID);

        Long commentsCount = 5L;
        Long likesCount = 10L;
        List<CommentResponse> comments = List.of(
                TestDataFactory.createCommentResponse(UUID.randomUUID(), USER_ID, POST_ID)
        );
        List<LikePostResponse> likes = List.of(
                TestDataFactory.createLikePostResponse(UUID.randomUUID(), USER_ID, POST_ID)
        );

        when(mapper.map(post, PostDetailsResponse.class)).thenReturn(baseResponse);
        when(commentEntityProvider.getCommentsCountByPost(POST_ID)).thenReturn(commentsCount);
        when(likePostEntityProvider.getLikesCountByPost(POST_ID)).thenReturn(likesCount);
        when(batchCommentService.getCommentsForPost(POST_ID, 10)).thenReturn(comments);
        when(batchLikePostService.getLikesForPost(POST_ID, 5)).thenReturn(likes);

        PostDetailsResponse result = postDetailsAggregator.aggregatePostDetails(post,
                true, true, 10, 5);

        assertNotNull(result);
        assertEquals(POST_ID, result.getId());
        assertEquals(commentsCount, result.getCommentsCount());
        assertEquals(likesCount, result.getLikesCount());
        assertEquals(1, result.getComments().size());
        assertEquals(1, result.getLikes().size());

        verify(mapper).map(post, PostDetailsResponse.class);
        verify(commentEntityProvider).getCommentsCountByPost(POST_ID);
        verify(likePostEntityProvider).getLikesCountByPost(POST_ID);
        verify(batchCommentService).getCommentsForPost(POST_ID, 10);
        verify(batchLikePostService).getLikesForPost(POST_ID, 5);
    }

    @Test
    @DisplayName("Агрегация деталей поста - без комментариев и лайков")
    void aggregatePostDetails_whenExcludeCommentsAndLikes_shouldReturnResponseWithoutData() {
        Post post = TestDataFactory.createPostEntity(POST_ID, USER_ID, "Test messaging",
                "http://example.com/image.jpg");
        PostDetailsResponse baseResponse = TestDataFactory.createPostDetailsResponse(POST_ID);

        Long commentsCount = 5L;
        Long likesCount = 10L;

        when(mapper.map(post, PostDetailsResponse.class)).thenReturn(baseResponse);
        when(commentEntityProvider.getCommentsCountByPost(POST_ID)).thenReturn(commentsCount);
        when(likePostEntityProvider.getLikesCountByPost(POST_ID)).thenReturn(likesCount);

        PostDetailsResponse result = postDetailsAggregator.aggregatePostDetails(post,
                false, false, 0, 0);

        assertNotNull(result);
        assertEquals(POST_ID, result.getId());
        assertEquals(commentsCount, result.getCommentsCount());
        assertEquals(likesCount, result.getLikesCount());
        assertTrue(result.getComments().isEmpty());
        assertTrue(result.getLikes().isEmpty());

        verify(mapper).map(post, PostDetailsResponse.class);
        verify(commentEntityProvider).getCommentsCountByPost(POST_ID);
        verify(likePostEntityProvider).getLikesCountByPost(POST_ID);
        verify(batchCommentService, never()).getCommentsForPost(any(), anyInt());
        verify(batchLikePostService, never()).getLikesForPost(any(), anyInt());
    }

    @Test
    @DisplayName("Агрегация деталей поста - только комментарии")
    void aggregatePostDetails_whenIncludeCommentsOnly_shouldReturnResponseWithCommentsOnly() {
        Post post = TestDataFactory.createPostEntity(POST_ID, USER_ID, "Test messaging",
                "http://example.com/image.jpg");
        PostDetailsResponse baseResponse = TestDataFactory.createPostDetailsResponse(POST_ID);

        Long commentsCount = 5L;
        Long likesCount = 10L;
        List<CommentResponse> comments = List.of(
                TestDataFactory.createCommentResponse(UUID.randomUUID(), USER_ID, POST_ID)
        );

        when(mapper.map(post, PostDetailsResponse.class)).thenReturn(baseResponse);
        when(commentEntityProvider.getCommentsCountByPost(POST_ID)).thenReturn(commentsCount);
        when(likePostEntityProvider.getLikesCountByPost(POST_ID)).thenReturn(likesCount);
        when(batchCommentService.getCommentsForPost(POST_ID, 5)).thenReturn(comments);

        PostDetailsResponse result = postDetailsAggregator.aggregatePostDetails(post,
                true, false, 5, 0);

        assertNotNull(result);
        assertEquals(POST_ID, result.getId());
        assertEquals(commentsCount, result.getCommentsCount());
        assertEquals(likesCount, result.getLikesCount());
        assertEquals(1, result.getComments().size());
        assertTrue(result.getLikes().isEmpty());

        verify(mapper).map(post, PostDetailsResponse.class);
        verify(commentEntityProvider).getCommentsCountByPost(POST_ID);
        verify(likePostEntityProvider).getLikesCountByPost(POST_ID);
        verify(batchCommentService).getCommentsForPost(POST_ID, 5);
        verify(batchLikePostService, never()).getLikesForPost(any(), anyInt());
    }

    @Test
    @DisplayName("Агрегация страницы постов - с комментариями и лайками")
    void aggregatePostsPage_whenIncludeCommentsAndLikes_shouldReturnPageWithAllData() {
        List<UUID> postIds = TestDataFactory.createPostIds(3);
        List<Post> posts = postIds.stream()
                .map(postId -> TestDataFactory.createPostEntity(postId, USER_ID,
                        "Content " + postId, null))
                .toList();
        Page<Post> postsPage = TestDataFactory.createPage(posts);

        Map<UUID, Long> commentsCounts = TestDataFactory.createCommentsCountMap(postIds);
        Map<UUID, Long> likesCounts = TestDataFactory.createLikesCountMap(postIds);
        Map<UUID, List<CommentResponse>> commentsMap =
                TestDataFactory.createCommentsForPostsMap(postIds, 10);
        Map<UUID, List<LikePostResponse>> likesMap =
                TestDataFactory.createLikesForPostsMap(postIds, 5);

        posts.forEach(post -> {
            PostDetailsResponse response = PostDetailsResponse.builder()
                    .id(post.getId())
                    .ownerId(post.getOwnerId())
                    .content(post.getContent())
                    .imageUrl(post.getImageUrl())
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .build();
            when(mapper.map(post, PostDetailsResponse.class)).thenReturn(response);
        });

        when(batchCommentService.getCommentsCountsForPosts(postIds)).thenReturn(commentsCounts);
        when(batchLikePostService.getLikesCountsForPosts(postIds)).thenReturn(likesCounts);
        when(batchCommentService.getCommentsForPosts(postIds, 10)).thenReturn(commentsMap);
        when(batchLikePostService.getLikesForPosts(postIds, 5)).thenReturn(likesMap);

        Page<PostDetailsResponse> result = postDetailsAggregator.aggregatePostsPage(postsPage,
                true, true, 10, 5);

        assertNotNull(result);
        assertEquals(3, result.getContent().size());

        result.getContent().forEach(postDetails -> {
            assertTrue(postIds.contains(postDetails.getId()));
            assertEquals(5L, postDetails.getCommentsCount());
            assertEquals(10L, postDetails.getLikesCount());
            assertFalse(postDetails.getComments().isEmpty());
            assertFalse(postDetails.getLikes().isEmpty());
        });

        verify(batchCommentService).getCommentsCountsForPosts(postIds);
        verify(batchLikePostService).getLikesCountsForPosts(postIds);
        verify(batchCommentService).getCommentsForPosts(postIds, 10);
        verify(batchLikePostService).getLikesForPosts(postIds, 5);
    }

    @Test
    @DisplayName("Агрегация страницы постов - без комментариев и лайков")
    void aggregatePostsPage_whenExcludeCommentsAndLikes_shouldReturnPageWithoutData() {
        List<UUID> postIds = TestDataFactory.createPostIds(2);
        List<Post> posts = postIds.stream()
                .map(postId -> TestDataFactory.createPostEntity(postId, USER_ID,
                        "Content " + postId, null))
                .toList();
        Page<Post> postsPage = TestDataFactory.createPage(posts);

        Map<UUID, Long> commentsCounts = TestDataFactory.createCommentsCountMap(postIds);
        Map<UUID, Long> likesCounts = TestDataFactory.createLikesCountMap(postIds);

        posts.forEach(post -> {
            PostDetailsResponse response = PostDetailsResponse.builder()
                    .id(post.getId())
                    .ownerId(post.getOwnerId())
                    .content(post.getContent())
                    .imageUrl(post.getImageUrl())
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .build();
            when(mapper.map(post, PostDetailsResponse.class)).thenReturn(response);
        });

        when(batchCommentService.getCommentsCountsForPosts(postIds)).thenReturn(commentsCounts);
        when(batchLikePostService.getLikesCountsForPosts(postIds)).thenReturn(likesCounts);

        Page<PostDetailsResponse> result =
                postDetailsAggregator.aggregatePostsPage(postsPage, false,
                        false, 0, 0);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());

        result.getContent().forEach(postDetails -> {
            assertTrue(postIds.contains(postDetails.getId()));
            assertEquals(5L, postDetails.getCommentsCount());
            assertEquals(10L, postDetails.getLikesCount());
            assertTrue(postDetails.getComments().isEmpty());
            assertTrue(postDetails.getLikes().isEmpty());
        });

        verify(batchCommentService).getCommentsCountsForPosts(postIds);
        verify(batchLikePostService).getLikesCountsForPosts(postIds);
        verify(batchCommentService, never()).getCommentsForPosts(any(), anyInt());
        verify(batchLikePostService, never()).getLikesForPosts(any(), anyInt());
    }

    @Test
    @DisplayName("Агрегация страницы постов - пустая страница")
    void aggregatePostsPage_whenEmptyPage_shouldReturnEmptyPage() {
        Page<Post> emptyPage = Page.empty();

        Page<PostDetailsResponse> result =
                postDetailsAggregator.aggregatePostsPage(emptyPage, true, true,
                        10, 5);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(batchCommentService, never()).getCommentsCountsForPosts(any());
        verify(batchLikePostService, never()).getLikesCountsForPosts(any());
        verify(batchCommentService, never()).getCommentsForPosts(any(), anyInt());
        verify(batchLikePostService, never()).getLikesForPosts(any(), anyInt());
    }

    @Test
    @DisplayName("Агрегация страницы постов - только лайки")
    void aggregatePostsPage_whenIncludeLikesOnly_shouldReturnPageWithLikesOnly() {
        List<UUID> postIds = TestDataFactory.createPostIds(2);
        List<Post> posts = postIds.stream()
                .map(postId -> TestDataFactory.createPostEntity(postId,
                        USER_ID, "Content " + postId, null))
                .toList();
        Page<Post> postsPage = TestDataFactory.createPage(posts);

        Map<UUID, Long> commentsCounts = TestDataFactory.createCommentsCountMap(postIds);
        Map<UUID, Long> likesCounts = TestDataFactory.createLikesCountMap(postIds);
        Map<UUID, List<LikePostResponse>> likesMap = TestDataFactory.createLikesForPostsMap(postIds, 3);

        posts.forEach(post -> {
            PostDetailsResponse response = PostDetailsResponse.builder()
                    .id(post.getId())
                    .ownerId(post.getOwnerId())
                    .content(post.getContent())
                    .imageUrl(post.getImageUrl())
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .build();
            when(mapper.map(post, PostDetailsResponse.class)).thenReturn(response);
        });

        when(batchCommentService.getCommentsCountsForPosts(postIds)).thenReturn(commentsCounts);
        when(batchLikePostService.getLikesCountsForPosts(postIds)).thenReturn(likesCounts);
        when(batchLikePostService.getLikesForPosts(postIds, 3)).thenReturn(likesMap);

        Page<PostDetailsResponse> result = postDetailsAggregator
                .aggregatePostsPage(postsPage, false, true, 0, 3);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());

        result.getContent().forEach(postDetails -> {
            assertTrue(postIds.contains(postDetails.getId()));
            assertEquals(5L, postDetails.getCommentsCount());
            assertEquals(10L, postDetails.getLikesCount());
            assertTrue(postDetails.getComments().isEmpty());
            assertFalse(postDetails.getLikes().isEmpty());
        });

        verify(batchCommentService).getCommentsCountsForPosts(postIds);
        verify(batchLikePostService).getLikesCountsForPosts(postIds);
        verify(batchCommentService, never()).getCommentsForPosts(any(), anyInt());
        verify(batchLikePostService).getLikesForPosts(postIds, 3);
    }
}