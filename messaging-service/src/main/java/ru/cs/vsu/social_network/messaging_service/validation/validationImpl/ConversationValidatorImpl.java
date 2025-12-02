package ru.cs.vsu.social_network.messaging_service.validation.validationImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.messaging_service.entity.Conversation;
import ru.cs.vsu.social_network.messaging_service.exception.ConversationSelfException;
import ru.cs.vsu.social_network.messaging_service.provider.ConversationEntityProvider;
import ru.cs.vsu.social_network.messaging_service.utils.MessageConstants;
import ru.cs.vsu.social_network.messaging_service.validation.AbstractGenericValidator;
import ru.cs.vsu.social_network.messaging_service.validation.ConversationValidator;

import java.util.UUID;

/**
 * Реализация валидатора для проверки прав доступа к переписке.
 * Обеспечивает проверку владения перепиской и логирование попыток несанкционированного доступа.
 */
@Component
@Slf4j
public class ConversationValidatorImpl extends AbstractGenericValidator<Conversation>
        implements ConversationValidator {
    private final ConversationEntityProvider conversationEntityProvider;
    private static final String ENTITY_NAME = "ПЕРЕПИСКА";

    public ConversationValidatorImpl(ConversationEntityProvider provider, ConversationEntityProvider conversationEntityProvider) {
        super(provider, ENTITY_NAME);
        this.conversationEntityProvider = conversationEntityProvider;
    }

    @Override
    protected UUID extractOwnerId(Conversation entity) {
        return entity.getUser1Id();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateUsersNotSame(UUID user1Id, UUID user2Id) {
        if (user1Id.equals(user2Id)) {
            log.error("{}_ОШИБКА: попытка создания беседы пользователя с самим собой");
            throw new ConversationSelfException(MessageConstants.CONVERSATION_TO_SELF_FAILURE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateAccessToChat(UUID conversationId,
                                     UUID keycloakUserId) {
        Conversation conversation = conversationEntityProvider.getById(conversationId);
        if (!conversation.getUser1Id().equals(keycloakUserId) && !conversation.getUser2Id().equals(keycloakUserId)) {
            throw new AccessDeniedException(MessageConstants.ACCESS_DENIED);
        }
    }
}
