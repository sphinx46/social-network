package ru.cs.vsu.social_network.contents_service.service.aggregator.aggregatorImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.dto.response.content.CommentDetailsResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.content.LikeCommentResponse;
import ru.cs.vsu.social_network.contents_service.entity.Comment;
import ru.cs.vsu.social_network.contents_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.contents_service.provider.LikeCommentEntityProvider;
import ru.cs.vsu.social_network.contents_service.service.aggregator.CommentDetailsAggregator;
import ru.cs.vsu.social_network.contents_service.service.batch.BatchLikeCommentService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация агрегатора данных для комментариев.
 * {@inheritDoc}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommentDetailsAggregatorImpl implements CommentDetailsAggregator {

    private final EntityMapper mapper;
    private final LikeCommentEntityProvider likeCommentEntityProvider;
    private final BatchLikeCommentService batchLikeCommentService;

    /** {@inheritDoc} */
    @Override
    public CommentDetailsResponse aggregateCommentDetails(final Comment comment,
                                                          final boolean includeLikes,
                                                          final int likesLimit) {
        log.debug("COMMENT_DETAILS_AGGREGATOR_АГРЕГАЦИЯ_КОММЕНТАРИЯ_НАЧАЛО: " +
                "для комментария ID: {}", comment.getId());

        final CommentDetailsResponse commentDetailsResponse =
                mapper.map(comment, CommentDetailsResponse.class);

        final UUID commentId = comment.getId();

        final Long likesCount = likeCommentEntityProvider.getLikesCountByComment(commentId);
        commentDetailsResponse.setLikesCount(likesCount);

        if (includeLikes) {
            final List<LikeCommentResponse> likes =
                    batchLikeCommentService.getLikesForComment(commentId, likesLimit);
            commentDetailsResponse.setLikes(likes);
        } else {
            commentDetailsResponse.setLikes(Collections.emptyList());
        }

        log.debug("COMMENT_DETAILS_AGGREGATOR_АГРЕГАЦИЯ_КОММЕНТАРИЯ_УСПЕХ: " +
                "для комментария ID: {}", comment.getId());
        return commentDetailsResponse;
    }

    /** {@inheritDoc} */
    @Override
    public Page<CommentDetailsResponse> aggregateCommentsPage(final Page<Comment> commentsPage,
                                                              final boolean includeLikes,
                                                              final int likesLimit) {
        log.debug("COMMENT_DETAILS_AGGREGATOR_АГРЕГАЦИЯ_СТРАНИЦЫ_НАЧАЛО:" +
                " для {} комментариев", commentsPage.getNumberOfElements());

        final List<Comment> comments = commentsPage.getContent();
        if (comments.isEmpty()) {
            log.debug("COMMENT_DETAILS_AGGREGATOR_АГРЕГАЦИЯ_СТРАНИЦЫ_ПУСТАЯ_СТРАНИЦА");
            return new PageImpl<>(Collections.emptyList(),
                    commentsPage.getPageable(), commentsPage.getTotalElements());
        }

        final List<UUID> commentIds = comments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());

        final Map<UUID, Long> likesCounts =
                batchLikeCommentService.getLikesCountsForComments(commentIds);

        final Map<UUID, List<LikeCommentResponse>> likesMap = includeLikes ?
                batchLikeCommentService.getLikesForComments(commentIds, likesLimit) :
                Collections.emptyMap();

        final List<CommentDetailsResponse> detailedComments = comments.stream()
                .map(comment -> buildCommentDetailsResponse(comment,
                        likesCounts, likesMap, includeLikes))
                .collect(Collectors.toList());

        log.debug("COMMENT_DETAILS_AGGREGATOR_АГРЕГАЦИЯ_СТРАНИЦЫ_УСПЕХ: " +
                "агрегировано {} комментариев", detailedComments.size());
        return new PageImpl<>(detailedComments, commentsPage.getPageable(),
                commentsPage.getTotalElements());
    }

    /**
     * Строит детальный ответ для комментария.
     *
     * @param comment сущность комментария
     * @param likesCounts маппинг количества лайков
     * @param likesMap маппинг лайков
     * @param includeLikes флаг включения лайков
     * @return детальный ответ комментария
     */
    private CommentDetailsResponse buildCommentDetailsResponse(final Comment comment,
                                                               final Map<UUID, Long> likesCounts,
                                                               final Map<UUID, List<LikeCommentResponse>> likesMap,
                                                               final boolean includeLikes) {
        final CommentDetailsResponse response = mapper.map(comment, CommentDetailsResponse.class);
        final UUID commentId = comment.getId();

        response.setLikesCount(likesCounts.getOrDefault(commentId, 0L));

        if (includeLikes) {
            response.setLikes(likesMap.getOrDefault(commentId, Collections.emptyList()));
        } else {
            response.setLikes(Collections.emptyList());
        }

        return response;
    }
}