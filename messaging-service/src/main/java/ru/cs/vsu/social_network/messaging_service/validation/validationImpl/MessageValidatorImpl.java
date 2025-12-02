package ru.cs.vsu.social_network.messaging_service.validation.validationImpl;

import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.messaging_service.entity.Message;
import ru.cs.vsu.social_network.messaging_service.provider.MessageEntityProvider;
import ru.cs.vsu.social_network.messaging_service.validation.AbstractGenericValidator;
import ru.cs.vsu.social_network.messaging_service.validation.MessageValidator;

import java.util.UUID;

/**
 * Реализация валидатора для проверки прав доступа к сообщениям.
 * Обеспечивает проверку владения сообщениями и логирование попыток несанкционированного доступа.
 */
@Component
public class MessageValidatorImpl extends AbstractGenericValidator<Message>
        implements MessageValidator {
    private static final String ENTITY_NAME = "СООБЩЕНИЕ";

    public MessageValidatorImpl(MessageEntityProvider provider) {
        super(provider, ENTITY_NAME);
    }

    /** {@inheritDoc} */
    @Override
    protected UUID extractOwnerId(Message entity) {
        return entity.getSenderId();
    }
}