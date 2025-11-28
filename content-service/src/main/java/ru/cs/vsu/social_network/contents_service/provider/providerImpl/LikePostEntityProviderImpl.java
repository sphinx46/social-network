package ru.cs.vsu.social_network.contents_service.provider.providerImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.entity.LikePost;
import ru.cs.vsu.social_network.contents_service.exception.like.LikeNotFoundException;
import ru.cs.vsu.social_network.contents_service.provider.AbstractEntityProvider;
import ru.cs.vsu.social_network.contents_service.provider.LikePostEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.LikePostRepository;
import ru.cs.vsu.social_network.contents_service.utils.MessageConstants;

import java.util.Optional;
import java.util.UUID;

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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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
}