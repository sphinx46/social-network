package ru.cs.vsu.social_network.contents_service.validation.validationImpl;


import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.entity.Comment;
import ru.cs.vsu.social_network.contents_service.provider.CommentEntityProvider;
import ru.cs.vsu.social_network.contents_service.utils.MessageConstants;
import ru.cs.vsu.social_network.contents_service.validation.CommentValidator;

import java.util.UUID;

/**
 * Реализация валидатора для проверки прав доступа к комментариям.
 * Обеспечивает проверку владения комментарием и логирование попыток несанкционированного доступа.
 */
@Slf4j
@Component
public class CommentValidatorImpl implements CommentValidator {
    private final CommentEntityProvider commentEntityProvider;

    public CommentValidatorImpl(CommentEntityProvider commentEntityProvider) {
        this.commentEntityProvider = commentEntityProvider;
    }

    /** {@inheritDoc} */
    @Override
    public void validateOwnership(UUID keycloakUserId, UUID commentId) {
        log.debug("КОММЕНТАРИЙ_ВАЛИДАТОР_ПРОВЕРКА_ВЛАДЕНИЯ_НАЧАЛО: " +
                "проверка прав доступа пользователя {} к комментарию {}", keycloakUserId, commentId);

        Comment comment = commentEntityProvider.getById(commentId);

        if (!comment.getOwnerId().equals(keycloakUserId)) {
            log.warn("КОММЕНТАРИЙ_ВАЛИДАТОР_ОШИБКА_ДОСТУПА: " +
                            "пользователь {} пытается получить доступ к " +
                            "чужому посту {} (владелец: {})",
                    keycloakUserId, commentId, comment.getOwnerId());

            throw new AccessDeniedException(MessageConstants.ACCESS_DENIED);
        }

        log.debug("КОММЕНТАРИЙ_ВАЛИДАТОР_ПРОВЕРКА_ВЛАДЕНИЯ_УСПЕХ: " +
                "пользователь {} имеет права доступа к комментарию {}", keycloakUserId, commentId);
    }
}