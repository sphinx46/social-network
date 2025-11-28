package ru.cs.vsu.social_network.contents_service.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cs.vsu.social_network.contents_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.content.PostDetailsResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.contents_service.entity.Post;
import ru.cs.vsu.social_network.contents_service.provider.PostEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.PostRepository;
import ru.cs.vsu.social_network.contents_service.service.PostDetailsService;
import ru.cs.vsu.social_network.contents_service.service.aggregator.PostDetailsAggregator;

import java.util.UUID;

/**
 * Реализация сервиса для работы с детальной информацией о постах.
 * Обеспечивает получение постов с связанными сущностями (лайки, комментарии).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostDetailsServiceImpl implements PostDetailsService {

    private final PostRepository postRepository;
    private final PostEntityProvider postEntityProvider;
    private final PostDetailsAggregator postDetailsAggregator;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PostDetailsResponse getPostDetails(final UUID postId,
                                              final boolean includeComments,
                                              final boolean includeLikes,
                                              final int commentsLimit,
                                              final int likesLimit) {
        log.info("ПОСТ_ДЕТАЛИ_СЕРВИС_ПОЛУЧЕНИЕ_ПО_ID_НАЧАЛО: "
                        + "запрос детальной информации поста с ID: {}, "
                        + "комментарии: {}, лайки: {}, лимит комментариев: {}, лимит лайков: {}",
                postId, includeComments, includeLikes, commentsLimit, likesLimit);

        final Post post = postEntityProvider.getById(postId);
        final PostDetailsResponse response = postDetailsAggregator
                .aggregatePostDetails(post, includeComments, includeLikes,
                        commentsLimit, likesLimit);

        log.info("ПОСТ_ДЕТАЛИ_СЕРВИС_ПОЛУЧЕНИЕ_ПО_ID_УСПЕХ: "
                        + "детальная информация поста с ID: {} собрана, "
                        + "комментариев: {}, лайков: {}",
                postId, response.getCommentsCount(), response.getLikesCount());
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<PostDetailsResponse> getUserPostsDetails(final UUID userId,
                                                                 final PageRequest pageRequest,
                                                                 final boolean includeComments,
                                                                 final boolean includeLikes,
                                                                 final int commentsLimit,
                                                                 final int likesLimit) {
        log.info("ПОСТ_ДЕТАЛИ_СЕРВИС_ПОЛУЧЕНИЕ_ПОЛЬЗОВАТЕЛЯ_НАЧАЛО: "
                        + "запрос детальной информации постов пользователя: {}, "
                        + "страница: {}, размер: {}, сортировка: {}, "
                        + "комментарии: {}, лайки: {}, лимит комментариев: {}, лимит лайков: {}",
                userId, pageRequest.getPageNumber(), pageRequest.getSize(),
                pageRequest.getSortBy(),
                includeComments, includeLikes, commentsLimit, likesLimit);

        final Page<Post> postsPage = postRepository
                .findAllByOwnerId(userId, pageRequest.toPageable());

        final Page<PostDetailsResponse> detailedPostsPage = postDetailsAggregator
                .aggregatePostsPage(postsPage, includeComments,
                        includeLikes, commentsLimit, likesLimit);

        final PageResponse<PostDetailsResponse> response = PageResponse.of(detailedPostsPage);

        log.info("ПОСТ_ДЕТАЛИ_СЕРВИС_ПОЛУЧЕНИЕ_ПОЛЬЗОВАТЕЛЯ_УСПЕХ: "
                        + "найдено {} постов с детальной информацией для пользователя: {}, "
                        + "всего страниц: {}",
                response.getContent().size(), userId, response.getTotalPages());
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<PostDetailsResponse> getAllPostsDetails(final PageRequest pageRequest,
                                                                final boolean includeComments,
                                                                final boolean includeLikes,
                                                                final int commentsLimit,
                                                                final int likesLimit) {
        log.info("ПОСТ_ДЕТАЛИ_СЕРВИС_ПОЛУЧЕНИЕ_ВСЕХ_НАЧАЛО: "
                        + "запрос детальной информации всех постов, "
                        + "страница: {}, размер: {}, сортировка: {}, "
                        + "комментарии: {}, лайки: {}, лимит комментариев: {}, лимит лайков: {}",
                pageRequest.getPageNumber(), pageRequest.getSize(), pageRequest.getSortBy(),
                includeComments, includeLikes, commentsLimit, likesLimit);

        final Page<Post> postsPage = postRepository.findAll(pageRequest.toPageable());
        final Page<PostDetailsResponse> detailedPostsPage = postDetailsAggregator
                .aggregatePostsPage(postsPage, includeComments,
                        includeLikes, commentsLimit, likesLimit);

        final PageResponse<PostDetailsResponse> response = PageResponse.of(detailedPostsPage);

        log.info("ПОСТ_ДЕТАЛИ_СЕРВИС_ПОЛУЧЕНИЕ_ВСЕХ_УСПЕХ: "
                        + "найдено {} постов с детальной информацией, всего страниц: {}",
                response.getContent().size(), response.getTotalPages());
        return response;
    }
}