package ru.cs.vsu.social_network.contents_service.validation.validationImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.entity.Post;
import ru.cs.vsu.social_network.contents_service.provider.PostEntityProvider;
import ru.cs.vsu.social_network.contents_service.utils.MessageConstants;
import ru.cs.vsu.social_network.contents_service.validation.PostValidator;

import java.util.UUID;

/**
 * Реализация валидатора для проверки прав доступа к постам.
 * Обеспечивает проверку владения постами и логирование попыток несанкционированного доступа.
 */
@Slf4j
@Component
public class PostValidatorImpl implements PostValidator {
    private final PostEntityProvider postEntityProvider;

    public PostValidatorImpl(PostEntityProvider postEntityProvider) {
        this.postEntityProvider = postEntityProvider;
    }

    /** {@inheritDoc} */
    @Override
    public void validateOwnership(UUID keycloakUserId, UUID postId) {
        log.debug("ПОСТ_ВАЛИДАТОР_ПРОВЕРКА_ВЛАДЕНИЯ_НАЧАЛО: " +
                "проверка прав доступа пользователя {} к посту {}", keycloakUserId, postId);

        Post post = postEntityProvider.getById(postId);

        if (!post.getOwnerId().equals(keycloakUserId)) {
            log.warn("ПОСТ_ВАЛИДАТОР_ОШИБКА_ДОСТУПА: " +
                            "пользователь {} пытается получить доступ " +
                            "к чужому посту {} (владелец: {})",
                    keycloakUserId, postId, post.getOwnerId());

            throw new AccessDeniedException(MessageConstants.ACCESS_DENIED);
        }

        log.debug("ПОСТ_ВАЛИДАТОР_ПРОВЕРКА_ВЛАДЕНИЯ_УСПЕХ: " +
                "пользователь {} имеет права доступа к посту {}", keycloakUserId, postId);
    }
}