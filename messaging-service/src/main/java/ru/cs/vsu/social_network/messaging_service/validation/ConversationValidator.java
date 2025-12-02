package ru.cs.vsu.social_network.messaging_service.validation;

import ru.cs.vsu.social_network.messaging_service.entity.Conversation;
import ru.cs.vsu.social_network.messaging_service.exception.conversation.ConversationSelfException;

import java.util.UUID;

/**
 * Валидатор для проверки прав доступа к переписке.
 * Обеспечивает проверку владения и прав доступа пользователей к операциям с переписками.
 */
public interface ConversationValidator extends GenericValidator<Conversation> {
    /**
     * Проверяет, что два пользователя не являются одним и тем же пользователем.
     *
     * @param user1Id идентификатор первого пользователя
     * @param user2Id идентификатор второго пользователя
     * @throws ConversationSelfException, если user1Id равен user2Id
     */
    void validateUsersNotSame(UUID user1Id,
                              UUID user2Id);

    /**
     * Проверяет, что у пользователя есть доступ к переписке.
     *
     * @param keycloakUserId идентификатор пользователя
     * @throws org.springframework.security.access.AccessDeniedException, если id пользователя не совпадает с user1Id или user2Id
     */
    void validateAccessToChat(UUID conversationId,
                              UUID keycloakUserId
    );
}
