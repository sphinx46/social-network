package ru.cs.vsu.social_network.messaging_service.validation.validationImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.messaging_service.event.MessageImageUploadedEvent;
import ru.cs.vsu.social_network.messaging_service.validation.MessageImageUploadedEventValidator;

@Slf4j
@Component
public final class MessageImageUploadedEventValidatorImpl implements MessageImageUploadedEventValidator {

    /**
     * Выполняет комплексную валидацию события загрузки изображения в сообщении.
     * Проверяет наличие обязательных полей и корректность их значений.
     *
     * @param event событие загрузки изображения в сообщении для валидации
     * @throws IllegalArgumentException если событие содержит некорректные данные
     */
    @Override
    public void validateEvent(final MessageImageUploadedEvent event) {
        log.debug("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_СООБЩЕНИЯ_ВАЛИДАЦИЯ_НАЧАЛО: eventId={}", event.getEventId());

        if (event.getEventId() == null) {
            log.error("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_СООБЩЕНИЯ_ВАЛИДАЦИЯ_ОШИБКА: eventId не может быть пустым");
            throw new IllegalArgumentException("EventId не может быть пустым.");
        }

        if (event.getOwnerId() == null) {
            log.error("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_СООБЩЕНИЯ_ВАЛИДАЦИЯ_ОШИБКА: eventId={}, поле ownerId не может быть пустым",
                    event.getEventId());
            throw new IllegalArgumentException("OwnerId не может быть пустым.");
        }

        if (event.getMessageId() == null) {
            log.error("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_СООБЩЕНИЯ_ВАЛИДАЦИЯ_ОШИБКА: eventId={}, поле messageId не может быть пустым",
                    event.getEventId());
            throw new IllegalArgumentException("MessageId не может быть пустым.");
        }

        if (event.getPublicUrl() == null || event.getPublicUrl().trim().isEmpty()) {
            log.error("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_СООБЩЕНИЯ_ВАЛИДАЦИЯ_ОШИБКА: eventId={}, поле publicUrl не может быть пустым",
                    event.getEventId());
            throw new IllegalArgumentException("PublicUrl не может быть пустым.");
        }

        if (event.getObjectName() == null || event.getObjectName().trim().isEmpty()) {
            log.error("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_СООБЩЕНИЯ_ВАЛИДАЦИЯ_ОШИБКА: eventId={}, поле objectName не может быть пустым",
                    event.getEventId());
            throw new IllegalArgumentException("ObjectName не может быть пустым.");
        }

        log.debug("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_СООБЩЕНИЯ_ВАЛИДАЦИЯ_УСПЕХ: eventId={}", event.getEventId());
    }
}