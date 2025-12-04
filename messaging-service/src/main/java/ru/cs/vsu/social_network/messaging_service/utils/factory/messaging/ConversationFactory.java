package ru.cs.vsu.social_network.messaging_service.utils.factory.messaging;

import ru.cs.vsu.social_network.messaging_service.entity.Conversation;

import java.util.UUID;

/**
 * Фабрика для создания сущностей Conversation.
 * Специализированная реализация для работы с переписками.
 */
public interface ConversationFactory {

    /**
     * Создает новую сущность беседы.
     *
     * @param user1Id идентификатор первого пользователя
     * @param user2Id идентификатор второго пользователя
     * @return новая сущность беседы
     */
    Conversation buildNewConversation(UUID user1Id, UUID user2Id);
}
