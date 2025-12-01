package ru.cs.vsu.social_network.contents_service.provider.providerImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.entity.LikeComment;
import ru.cs.vsu.social_network.contents_service.exception.like.LikeNotFoundException;
import ru.cs.vsu.social_network.contents_service.provider.AbstractEntityProvider;
import ru.cs.vsu.social_network.contents_service.provider.LikeCommentEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.LikeCommentRepository;
import ru.cs.vsu.social_network.contents_service.utils.MessageConstants;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация провайдера для получения сущности LikeComment.
 * Обеспечивает доступ к данным лайков с обработкой исключительных ситуаций.
 */
@Slf4j
@Component
public final class LikeCommentEntityProviderImpl extends AbstractEntityProvider<LikeComment>
        implements LikeCommentEntityProvider {
    private static final String ENTITY_NAME = "ЛАЙК_КОММЕНТАРИЙ";
    private final LikeCommentRepository likeCommentRepository;

    public LikeCommentEntityProviderImpl(LikeCommentRepository likeCommentRepository) {
        super(likeCommentRepository, ENTITY_NAME, () ->
                new LikeNotFoundException(MessageConstants.LIKE_NOT_FOUND_FAILURE));
        this.likeCommentRepository = likeCommentRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getLikesCountByComment(UUID commentId) {
        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_НАЧАЛО:" +
                        " запрос количества лайков для комментария с ID: {}",
                ENTITY_NAME, commentId);

        final long count = likeCommentRepository.countByCommentId(commentId);

        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_УСПЕХ: " +
                        "найдено {} лайков для комментария с ID: {}",
                ENTITY_NAME, count, commentId);

        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<LikeComment> findByOwnerIdAndCommentId(UUID ownerId, UUID commentId) {
        log.info("{}_ПРОВАЙДЕР_ПОИСК_ПО_ВЛАДЕЛЬЦУ_И_КОММЕНТАРИЮ_НАЧАЛО: " +
                        "поиск лайка для пользователя: {} и комментария: {}",
                ENTITY_NAME, ownerId, commentId);

        final Optional<LikeComment> likeComment =
                likeCommentRepository.findByOwnerIdAndCommentId(ownerId, commentId);

        log.info("{}_ПРОВАЙДЕР_ПОИСК_ПО_ВЛАДЕЛЬЦУ_И_КОММЕНТАРИЮ_УСПЕХ: " +
                        "лайк {} для пользователя: {} и комментария: {}",
                ENTITY_NAME, likeComment.isPresent() ? "найден" : "не найден", ownerId, commentId);

        return likeComment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByOwnerIdAndCommentId(UUID ownerId, UUID commentId) {
        log.info("{}_ПРОВАЙДЕР_ПРОВЕРКА_СУЩЕСТВОВАНИЯ_НАЧАЛО: " +
                        "проверка существования лайка для пользователя: {} и комментария: {}",
                ENTITY_NAME, ownerId, commentId);

        final boolean exists =
                likeCommentRepository.existsByOwnerIdAndCommentId(ownerId, commentId);

        log.info("{}_ПРОВАЙДЕР_ПРОВЕРКА_СУЩЕСТВОВАНИЯ_УСПЕХ: " +
                        "лайк {} для пользователя: {} и комментария: {}",
                ENTITY_NAME, exists ? "существует" : "не существует", ownerId, commentId);

        return exists;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<UUID, Long> getLikesCountsForComments(List<UUID> commentIds) {
        log.info("{}_ПРОВАЙДЕР_ПАКЕТНОЕ_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_НАЧАЛО: " +
                "для {} комментариев", commentIds.size());

        if (commentIds.isEmpty()) {
            log.info("{}_ПРОВАЙДЕР_ПАКЕТНОЕ_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_ПУСТОЙ_СПИСОК", ENTITY_NAME);
            return Collections.emptyMap();
        }

        final List<Object[]> counts = likeCommentRepository.findLikesCountByCommentIds(commentIds);

        final Map<UUID, Long> result = counts.stream()
                .collect(Collectors.toMap(
                        tuple -> (UUID) tuple[0],
                        tuple -> (Long) tuple[1]
                ));

        commentIds.forEach(commentId -> result.putIfAbsent(commentId, 0L));

        log.info("{}_ПРОВАЙДЕР_ПАКЕТНОЕ_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_УСПЕХ: " +
                "получено количество лайков для {} комментариев", ENTITY_NAME, result.size());

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LikeComment> getRecentLikesForComment(UUID commentId, int limit) {
        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_ПОСЛЕДНИХ_ЛАЙКОВ_НАЧАЛО: " +
                "для комментария {} с лимитом {}", commentId, limit);

        final int effectiveLimit = Math.max(1, Math.min(limit, 100));
        final PageRequest pageRequest = PageRequest.of(0, effectiveLimit);

        final List<LikeComment> likes = likeCommentRepository
                .findAllByCommentId(commentId, pageRequest)
                .getContent();

        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_ПОСЛЕДНИХ_ЛАЙКОВ_УСПЕХ: " +
                "для комментария {} найдено {} лайков", commentId, likes.size());

        return likes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LikeComment> getLikesWithComments(List<UUID> commentIds, int limit) {
        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_ЛАЙКОВ_С_КОММЕНТАРИЯМИ_НАЧАЛО: " +
                "для {} комментариев с лимитом {}", commentIds.size(), limit);

        if (commentIds.isEmpty()) {
            log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_ЛАЙКОВ_С_КОММЕНТАРИЯМИ_ПУСТОЙ_СПИСОК", ENTITY_NAME);
            return Collections.emptyList();
        }

        final int effectiveLimit = Math.max(1, Math.min(limit, 50));
        final PageRequest pageRequest = PageRequest.of(0, effectiveLimit);

        final List<LikeComment> likes = likeCommentRepository
                .findLikesWithComments(commentIds, pageRequest);

        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_ЛАЙКОВ_С_КОММЕНТАРИЯМИ_УСПЕХ: " +
                "получено {} лайков с комментариями", likes.size());

        return likes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LikeComment> getRecentLikesForComments(List<UUID> commentIds, int limit) {
        log.info("{}_ПРОВАЙДЕР_ПАКЕТНОЕ_ПОЛУЧЕНИЕ_ЛАЙКОВ_НАЧАЛО: " +
                "для {} комментариев с лимитом {}", commentIds.size(), limit);

        if (commentIds.isEmpty()) {
            log.info("{}_ПРОВАЙДЕР_ПАКЕТНОЕ_ПОЛУЧЕНИЕ_ЛАЙКОВ_ПУСТОЙ_СПИСОК", ENTITY_NAME);
            return Collections.emptyList();
        }

        final int effectiveLimit = Math.max(1, Math.min(limit, 50));
        final List<LikeComment> likes = likeCommentRepository
                .findRecentLikesForComments(commentIds, effectiveLimit);

        log.info("{}_ПРОВАЙДЕР_ПАКЕТНОЕ_ПОЛУЧЕНИЕ_ЛАЙКОВ_УСПЕХ: " +
                "получено {} лайков", likes.size());

        return likes;
    }
}