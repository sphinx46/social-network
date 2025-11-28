package ru.cs.vsu.social_network.contents_service.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cs.vsu.social_network.contents_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.content.CommentDetailsResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.contents_service.entity.Comment;
import ru.cs.vsu.social_network.contents_service.provider.CommentEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.CommentRepository;
import ru.cs.vsu.social_network.contents_service.service.CommentDetailsService;
import ru.cs.vsu.social_network.contents_service.service.aggregator.CommentDetailsAggregator;

import java.util.UUID;

/**
 * Реализация сервиса для работы с детальной информацией о комментариях.
 * Обеспечивает получение комментариев с связанными сущностями (лайки).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentDetailsServiceImpl implements CommentDetailsService {

    private final CommentRepository commentRepository;
    private final CommentEntityProvider commentEntityProvider;
    private final CommentDetailsAggregator commentDetailsAggregator;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public CommentDetailsResponse getCommentDetails(final UUID commentId,
                                                    final boolean includeLikes,
                                                    final int likesLimit) {
        log.info("КОММЕНТАРИЙ_ДЕТАЛИ_СЕРВИС_ПОЛУЧЕНИЕ_ПО_ID_НАЧАЛО: "
                        + "запрос детальной информации комментария с ID: {}, "
                        + "лайки: {}, лимит лайков: {}",
                commentId, includeLikes, likesLimit);

        final Comment comment = commentEntityProvider.getById(commentId);
        final CommentDetailsResponse response = commentDetailsAggregator
                .aggregateCommentDetails(comment, includeLikes, likesLimit);

        log.info("КОММЕНТАРИЙ_ДЕТАЛИ_СЕРВИС_ПОЛУЧЕНИЕ_ПО_ID_УСПЕХ: "
                        + "детальная информация комментария с ID: {} собрана, "
                        + "лайков: {}",
                commentId, response.getLikesCount());
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommentDetailsResponse> getPostCommentsDetails(final UUID postId,
                                                                       final PageRequest pageRequest,
                                                                       final boolean includeLikes,
                                                                       final int likesLimit) {
        log.info("КОММЕНТАРИЙ_ДЕТАЛИ_СЕРВИС_ПОЛУЧЕНИЕ_ПОСТА_НАЧАЛО: "
                        + "запрос детальной информации комментариев поста: {}, "
                        + "страница: {}, размер: {}, сортировка: {}, "
                        + "лайки: {}, лимит лайков: {}",
                postId, pageRequest.getPageNumber(), pageRequest.getSize(),
                pageRequest.getSortBy(),
                includeLikes, likesLimit);

        final Page<Comment> commentsPage = commentRepository
                .findByPostIdOrderByCreatedAtDesc(postId, pageRequest.toPageable());

        final Page<CommentDetailsResponse> detailedCommentsPage = commentDetailsAggregator
                .aggregateCommentsPage(commentsPage, includeLikes, likesLimit);

        final PageResponse<CommentDetailsResponse> response = PageResponse.of(detailedCommentsPage);

        log.info("КОММЕНТАРИЙ_ДЕТАЛИ_СЕРВИС_ПОЛУЧЕНИЕ_ПОСТА_УСПЕХ: "
                        + "найдено {} комментариев с детальной информацией для поста: {}, "
                        + "всего страниц: {}",
                response.getContent().size(), postId, response.getTotalPages());
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommentDetailsResponse> getUserCommentsDetails(final UUID userId,
                                                                       final PageRequest pageRequest,
                                                                       final boolean includeLikes,
                                                                       final int likesLimit) {
        log.info("КОММЕНТАРИЙ_ДЕТАЛИ_СЕРВИС_ПОЛУЧЕНИЕ_ПОЛЬЗОВАТЕЛЯ_НАЧАЛО: "
                        + "запрос детальной информации комментариев пользователя: {}, "
                        + "страница: {}, размер: {}, сортировка: {}, "
                        + "лайки: {}, лимит лайков: {}",
                userId, pageRequest.getPageNumber(), pageRequest.getSize(),
                pageRequest.getSortBy(),
                includeLikes, likesLimit);

        final Page<Comment> commentsPage = commentRepository
                .findByOwnerIdOrderByCreatedAtDesc(userId, pageRequest.toPageable());

        final Page<CommentDetailsResponse> detailedCommentsPage = commentDetailsAggregator
                .aggregateCommentsPage(commentsPage, includeLikes, likesLimit);

        final PageResponse<CommentDetailsResponse> response = PageResponse.of(detailedCommentsPage);

        log.info("КОММЕНТАРИЙ_ДЕТАЛИ_СЕРВИС_ПОЛУЧЕНИЕ_ПОЛЬЗОВАТЕЛЯ_УСПЕХ: "
                        + "найдено {} комментариев с детальной информацией для пользователя: {}, "
                        + "всего страниц: {}",
                response.getContent().size(), userId, response.getTotalPages());
        return response;
    }
}