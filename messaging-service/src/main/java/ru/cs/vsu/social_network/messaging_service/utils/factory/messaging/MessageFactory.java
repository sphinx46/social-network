package ru.cs.vsu.social_network.messaging_service.utils.factory.messaging;

import ru.cs.vsu.social_network.messaging_service.dto.request.messaging.MessageCreateRequest;
import ru.cs.vsu.social_network.messaging_service.entity.Message;

/**
 * Фабрика для создания сущностей Message.
 * Специализированная реализация для работы с сообщениями.
 */
public interface MessageFactory extends GenericFactory<Message, MessageCreateRequest> {
}