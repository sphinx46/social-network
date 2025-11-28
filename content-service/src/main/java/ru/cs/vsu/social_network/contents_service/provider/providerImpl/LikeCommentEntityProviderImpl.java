package ru.cs.vsu.social_network.contents_service.provider.providerImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.entity.LikeComment;
import ru.cs.vsu.social_network.contents_service.exception.like.LikeNotFoundException;
import ru.cs.vsu.social_network.contents_service.provider.AbstractEntityProvider;
import ru.cs.vsu.social_network.contents_service.provider.LikeCommentEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.LikeCommentRepository;
import ru.cs.vsu.social_network.contents_service.utils.MessageConstants;

import java.util.Optional;
import java.util.UUID;

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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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
}