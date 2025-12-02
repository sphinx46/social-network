package ru.cs.vsu.social_network.messaging_service.utils.factory.factoryImpl;


import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.messaging_service.dto.request.messaging.MessageCreateRequest;
import ru.cs.vsu.social_network.messaging_service.entity.Message;
import ru.cs.vsu.social_network.messaging_service.entity.enums.MessageStatus;
import ru.cs.vsu.social_network.messaging_service.utils.factory.messaging.AbstractFactory;
import ru.cs.vsu.social_network.messaging_service.utils.factory.messaging.MessageFactory;

import java.util.UUID;

/**
 * Реализация фабрики для создания сущностей Message.
 * Создает новые экземпляры сообщений на основе входных данных.
 */
@Component
public class MessageFactoryImpl extends AbstractFactory<Message, MessageCreateRequest>
        implements MessageFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Message buildEntity(UUID keycloakUserId, MessageCreateRequest request) {
        return Message.builder()
                .senderId(keycloakUserId)
                .receiverId(request.getReceiverId())
                .content(request.getContent())
                .conversation(null)
                .status(MessageStatus.SENT)
                .imageUrl(null)
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getFactoryName() {
        return "СООБЩЕНИЕ";
    }
}