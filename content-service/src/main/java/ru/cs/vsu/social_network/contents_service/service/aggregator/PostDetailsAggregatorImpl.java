package ru.cs.vsu.social_network.contents_service.service.aggregator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.dto.response.content.CommentResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.content.LikePostResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.content.PostDetailsResponse;
import ru.cs.vsu.social_network.contents_service.entity.Post;
import ru.cs.vsu.social_network.contents_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.contents_service.provider.CommentEntityProvider;
import ru.cs.vsu.social_network.contents_service.provider.LikePostEntityProvider;
import ru.cs.vsu.social_network.contents_service.service.batch.BatchCommentService;
import ru.cs.vsu.social_network.contents_service.service.batch.BatchLikeService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация агрегатора данных для постов.
 * {@inheritDoc}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostDetailsAggregatorImpl implements PostDetailsAggregator {

    private final EntityMapper mapper;
    private final CommentEntityProvider commentEntityProvider;
    private final LikePostEntityProvider likePostEntityProvider;
    private final BatchCommentService batchCommentService;
    private final BatchLikeService batchLikeService;

    /** {@inheritDoc} */
    @Override
    public PostDetailsResponse aggregatePostDetails(final Post post,
                                                    final boolean includeComments,
                                                    final boolean includeLikes,
                                                    final int commentsLimit,
                                                    final int likesLimit) {
        log.debug("POST_DETAILS_AGGREGATOR_АГРЕГАЦИЯ_ПОСТА_НАЧАЛО: " +
                "для поста ID: {}", post.getId());

        final PostDetailsResponse postDetailsResponse =
                mapper.map(post, PostDetailsResponse.class);

        final UUID postId = post.getId();

        final Long commentsCount = commentEntityProvider.getCommentsCountByPost(postId);
        final Long likesCount = likePostEntityProvider.getLikesCountByPost(postId);

        postDetailsResponse.setCommentsCount(commentsCount);
        postDetailsResponse.setLikesCount(likesCount);

        if (includeComments) {
            final List<CommentResponse> comments = batchCommentService.getCommentsForPost(postId, commentsLimit);
            postDetailsResponse.setComments(comments);
        } else {
            postDetailsResponse.setComments(Collections.emptyList());
        }

        if (includeLikes) {
            final List<LikePostResponse> likes =
                    batchLikeService.getLikesForPost(postId, likesLimit);
            postDetailsResponse.setLikes(likes);
        } else {
            postDetailsResponse.setLikes(Collections.emptyList());
        }

        log.debug("POST_DETAILS_AGGREGATOR_АГРЕГАЦИЯ_ПОСТА_УСПЕХ: " +
                "для поста ID: {}", post.getId());
        return postDetailsResponse;
    }

    /** {@inheritDoc} */
    @Override
    public Page<PostDetailsResponse> aggregatePostsPage(final Page<Post> postsPage,
                                                        final boolean includeComments,
                                                        final boolean includeLikes,
                                                        final int commentsLimit,
                                                        final int likesLimit) {
        log.debug("POST_DETAILS_AGGREGATOR_АГРЕГАЦИЯ_СТРАНИЦЫ_НАЧАЛО:" +
                " для {} постов", postsPage.getNumberOfElements());

        final List<Post> posts = postsPage.getContent();
        if (posts.isEmpty()) {
            log.debug("POST_DETAILS_AGGREGATOR_АГРЕГАЦИЯ_СТРАНИЦЫ_ПУСТАЯ_СТРАНИЦА");
            return new PageImpl<>(Collections.emptyList(),
                    postsPage.getPageable(), postsPage.getTotalElements());
        }

        final List<UUID> postIds = posts.stream()
                .map(Post::getId)
                .collect(Collectors.toList());

        final Map<UUID, Long> commentsCounts =
                batchCommentService.getCommentsCountsForPosts(postIds);
        final Map<UUID, Long> likesCounts =
                batchLikeService.getLikesCountsForPosts(postIds);

        final Map<UUID, List<CommentResponse>> commentsMap = includeComments ?
                batchCommentService.getCommentsForPosts(postIds, commentsLimit) :
                Collections.emptyMap();

        final Map<UUID, List<LikePostResponse>> likesMap = includeLikes ?
                batchLikeService.getLikesForPosts(postIds, likesLimit) :
                Collections.emptyMap();

        final List<PostDetailsResponse> detailedPosts = posts.stream()
                .map(post -> buildPostDetailsResponse(post, commentsCounts, likesCounts,
                        commentsMap, likesMap,
                        includeComments, includeLikes))
                .collect(Collectors.toList());

        log.debug("POST_DETAILS_AGGREGATOR_АГРЕГАЦИЯ_СТРАНИЦЫ_УСПЕХ: " +
                "агрегировано {} постов", detailedPosts.size());
        return new PageImpl<>(detailedPosts, postsPage.getPageable(),
                postsPage.getTotalElements());
    }

    /**
     * Строит детальный ответ для поста.
     *
     * @param post сущность поста
     * @param commentsCounts маппинг количества комментариев
     * @param likesCounts маппинг количества лайков
     * @param commentsMap маппинг комментариев
     * @param likesMap маппинг лайков
     * @param includeComments флаг включения комментариев
     * @param includeLikes флаг включения лайков
     * @return детальный ответ поста
     */
    private PostDetailsResponse buildPostDetailsResponse(final Post post,
                                                         final Map<UUID, Long> commentsCounts,
                                                         final Map<UUID, Long> likesCounts,
                                                         final Map<UUID, List<CommentResponse>> commentsMap,
                                                         final Map<UUID, List<LikePostResponse>> likesMap,
                                                         final boolean includeComments,
                                                         final boolean includeLikes) {
        final PostDetailsResponse response = mapper.map(post, PostDetailsResponse.class);
        final UUID postId = post.getId();

        response.setCommentsCount(commentsCounts.getOrDefault(postId, 0L));
        response.setLikesCount(likesCounts.getOrDefault(postId, 0L));

        if (includeComments) {
            response.setComments(commentsMap.getOrDefault(postId, Collections.emptyList()));
        } else {
            response.setComments(Collections.emptyList());
        }

        if (includeLikes) {
            response.setLikes(likesMap.getOrDefault(postId, Collections.emptyList()));
        } else {
            response.setLikes(Collections.emptyList());
        }

        return response;
    }
}