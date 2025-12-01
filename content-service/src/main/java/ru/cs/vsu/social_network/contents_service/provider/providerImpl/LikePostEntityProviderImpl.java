package ru.cs.vsu.social_network.contents_service.provider.providerImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.entity.LikePost;
import ru.cs.vsu.social_network.contents_service.exception.like.LikeNotFoundException;
import ru.cs.vsu.social_network.contents_service.provider.AbstractEntityProvider;
import ru.cs.vsu.social_network.contents_service.provider.LikePostEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.LikePostRepository;
import ru.cs.vsu.social_network.contents_service.utils.MessageConstants;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация провайдера для получения сущности LikePost.
 * Обеспечивает доступ к данным лайков с обработкой исключительных ситуаций.
 */
@Slf4j
@Component
public final class LikePostEntityProviderImpl extends AbstractEntityProvider<LikePost>
        implements LikePostEntityProvider {
    private static final String ENTITY_NAME = "ЛАЙК_ПОСТ";
    private final LikePostRepository likePostRepository;

    public LikePostEntityProviderImpl(LikePostRepository likePostRepository) {
        super(likePostRepository, ENTITY_NAME, () ->
                new LikeNotFoundException(MessageConstants.LIKE_NOT_FOUND_FAILURE));
        this.likePostRepository = likePostRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getLikesCountByPost(UUID postId) {
        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_НАЧАЛО: " +
                        "запрос количества лайков для поста с ID: {}",
                ENTITY_NAME, postId);

        final long count = likePostRepository.countByPostId(postId);

        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_УСПЕХ: " +
                        "найдено {} лайков для поста с ID: {}",
                ENTITY_NAME, count, postId);

        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<LikePost> findByOwnerIdAndPostId(UUID ownerId, UUID postId) {
        log.info("{}_ПРОВАЙДЕР_ПОИСК_ПО_ВЛАДЕЛЬЦУ_И_ПОСТУ_НАЧАЛО: " +
                        "поиск лайка для пользователя: {} и поста: {}",
                ENTITY_NAME, ownerId, postId);

        final Optional<LikePost> likePost =
                likePostRepository.findByOwnerIdAndPostId(ownerId, postId);

        log.info("{}_ПРОВАЙДЕР_ПОИСК_ПО_ВЛАДЕЛЬЦУ_И_ПОСТУ_УСПЕХ: " +
                        "лайк {} для пользователя: {} и поста: {}",
                ENTITY_NAME, likePost.isPresent() ? "найден" : "не найден", ownerId, postId);

        return likePost;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByOwnerIdAndPostId(UUID ownerId, UUID postId) {
        log.info("{}_ПРОВАЙДЕР_ПРОВЕРКА_СУЩЕСТВОВАНИЯ_НАЧАЛО: " +
                        "проверка существования лайка для пользователя: {} и поста: {}",
                ENTITY_NAME, ownerId, postId);

        final boolean exists =
                likePostRepository.existsByOwnerIdAndPostId(ownerId, postId);

        log.info("{}_ПРОВАЙДЕР_ПРОВЕРКА_СУЩЕСТВОВАНИЯ_УСПЕХ: " +
                        "лайк {} для пользователя: {} и поста: {}",
                ENTITY_NAME, exists ? "существует" : "не существует", ownerId, postId);

        return exists;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<UUID, Long> getLikesCountsForPosts(List<UUID> postIds) {
        log.info("{}_ПРОВАЙДЕР_ПАКЕТНОЕ_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_НАЧАЛО: " +
                "для {} постов", postIds.size());

        if (postIds.isEmpty()) {
            log.info("{}_ПРОВАЙДЕР_ПАКЕТНОЕ_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_ПУСТОЙ_СПИСОК", ENTITY_NAME);
            return Collections.emptyMap();
        }

        final List<Object[]> counts = likePostRepository.findLikesCountByPostIds(postIds);

        final Map<UUID, Long> result = counts.stream()
                .collect(Collectors.toMap(
                        tuple -> (UUID) tuple[0],
                        tuple -> (Long) tuple[1]
                ));

        postIds.forEach(postId -> result.putIfAbsent(postId, 0L));

        log.info("{}_ПРОВАЙДЕР_ПАКЕТНОЕ_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_УСПЕХ: " +
                "получено количество лайков для {} постов", ENTITY_NAME, result.size());

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LikePost> getRecentLikesForPost(UUID postId, int limit) {
        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_ПОСЛЕДНИХ_ЛАЙКОВ_НАЧАЛО: " +
                "для поста {} с лимитом {}", postId, limit);

        final int effectiveLimit = Math.max(1, Math.min(limit, 100));
        final PageRequest pageRequest = PageRequest.of(0, effectiveLimit,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        final List<LikePost> likes = likePostRepository
                .findByPostIdOrderByCreatedAtDesc(postId, pageRequest);

        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_ПОСЛЕДНИХ_ЛАЙКОВ_УСПЕХ: " +
                "для поста {} найдено {} лайков", postId, likes.size());

        return likes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LikePost> getLikesByOwnerAndPosts(UUID ownerId, List<UUID> postIds) {
        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_ЛАЙКОВ_ПОЛЬЗОВАТЕЛЯ_НАЧАЛО: " +
                "для пользователя {} и {} постов", ownerId, postIds.size());

        if (postIds.isEmpty()) {
            log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_ЛАЙКОВ_ПОЛЬЗОВАТЕЛЯ_ПУСТОЙ_СПИСОК", ENTITY_NAME);
            return Collections.emptyList();
        }

        final List<LikePost> likes = likePostRepository
                .findByOwnerIdAndPostIds(ownerId, postIds);

        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_ЛАЙКОВ_ПОЛЬЗОВАТЕЛЯ_УСПЕХ: " +
                "найдено {} лайков для пользователя {}", likes.size(), ownerId);

        return likes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LikePost> getLikesWithPosts(List<UUID> postIds, int limit) {
        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_ЛАЙКОВ_С_ПОСТАМИ_НАЧАЛО: " +
                "для {} постов с лимитом {}", postIds.size(), limit);

        if (postIds.isEmpty()) {
            log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_ЛАЙКОВ_С_ПОСТАМИ_ПУСТОЙ_СПИСОК", ENTITY_NAME);
            return Collections.emptyList();
        }

        final int effectiveLimit = Math.max(1, Math.min(limit, 50));
        final PageRequest pageRequest = PageRequest.of(0, effectiveLimit);

        final List<LikePost> likes = likePostRepository
                .findLikesWithPosts(postIds, pageRequest);

        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_ЛАЙКОВ_С_ПОСТАМИ_УСПЕХ: " +
                "получено {} лайков с постами", likes.size());

        return likes;
    }
}