package ru.cs.vsu.social_network.messaging_service.utils.factory.factoryImpl;

import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.messaging_service.entity.Conversation;
import ru.cs.vsu.social_network.messaging_service.utils.factory.messaging.ConversationFactory;

import java.util.UUID;

/**
 * Реализация фабрики для создания сущностей Conversation.
 * Создает новые экземпляры переписки на основе входных данных.
 */
@Component
public class ConversationFactoryImpl implements ConversationFactory {
    /**
     * {@inheritDoc}
     */

    @Override
    public Conversation buildNewConversation(final UUID user1Id, final UUID user2Id) {
        return Conversation.builder()
                .user1Id(user1Id)
                .user2Id(user2Id)
                .messageCount(0L)
                .build();
    }
}
