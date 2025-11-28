package ru.cs.vsu.social_network.contents_service.service.aggregator;

import org.springframework.data.domain.Page;
import ru.cs.vsu.social_network.contents_service.dto.response.content.CommentDetailsResponse;
import ru.cs.vsu.social_network.contents_service.entity.Comment;

/**
 * Агрегатор данных для комментариев.
 * Обеспечивает сборку расширенных данных комментариев с загрузкой связанных сущностей.
 */
public interface CommentDetailsAggregator {

    /**
     * Агрегирует расширенные данные для одного комментария.
     *
     * @param comment сущность комментария
     * @param includeLikes флаг включения лайков
     * @param likesLimit ограничение количества лайков
     * @return расширенный ответ с данными комментария
     */
    CommentDetailsResponse aggregateCommentDetails(Comment comment,
                                                   boolean includeLikes,
                                                   int likesLimit);

    /**
     * Агрегирует расширенные данные для страницы комментариев.
     *
     * @param commentsPage страница с комментариями
     * @param includeLikes флаг включения лайков
     * @param likesLimit ограничение количества лайков на комментарий
     * @return страница с расширенными данными комментариев
     */
    Page<CommentDetailsResponse> aggregateCommentsPage(Page<Comment> commentsPage,
                                                       boolean includeLikes,
                                                       int likesLimit);
}