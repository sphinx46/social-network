package ru.cs.vsu.social_network.contents_service.service.aggregator;

import org.springframework.data.domain.Page;
import ru.cs.vsu.social_network.contents_service.dto.response.content.PostDetailsResponse;
import ru.cs.vsu.social_network.contents_service.entity.Post;

/**
 * Агрегатор данных для постов.
 * Обеспечивает сборку расширенных данных постов с загрузкой связанных сущностей.
 */
public interface PostDetailsAggregator {

    /**
     * Агрегирует расширенные данные для одного поста.
     *
     * @param post сущность поста
     * @param includeComments флаг включения комментариев
     * @param includeLikes флаг включения лайков
     * @param commentsLimit ограничение количества комментариев
     * @param likesLimit ограничение количества лайков
     * @return расширенный ответ с данными поста
     */
    PostDetailsResponse aggregatePostDetails(Post post,
                                             boolean includeComments,
                                             boolean includeLikes,
                                             int commentsLimit,
                                             int likesLimit);

    /**
     * Агрегирует расширенные данные для страницы постов.
     *
     * @param postsPage страница с постами
     * @param includeComments флаг включения комментариев
     * @param includeLikes флаг включения лайков
     * @param commentsLimit ограничение количества комментариев на пост
     * @param likesLimit ограничение количества лайков на пост
     * @return страница с расширенными данными постов
     */
    Page<PostDetailsResponse> aggregatePostsPage(Page<Post> postsPage,
                                                 boolean includeComments,
                                                 boolean includeLikes,
                                                 int commentsLimit,
                                                 int likesLimit);
}