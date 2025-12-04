package ru.cs.vsu.social_network.messaging_service.event.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.cs.vsu.social_network.messaging_service.dto.request.messaging.MessageUploadImageRequest;
import ru.cs.vsu.social_network.messaging_service.event.MessageImageUploadedEvent;
import ru.cs.vsu.social_network.messaging_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.messaging_service.service.MessageService;
import ru.cs.vsu.social_network.messaging_service.validation.MessageImageUploadedEventValidator;


@Slf4j
@Component
@RequiredArgsConstructor
public class MessageImageUploadedEventHandler implements EventHandler<MessageImageUploadedEvent> {

    private final MessageService messageService;
    private final MessageImageUploadedEventValidator validator;
    private final EntityMapper mapper;

    /**
     * Обрабатывает событие загрузки изображения в сообщении:
     * 1. Валидирует входящее событие
     * 2. Преобразует в DTO для сервиса
     * 3. Вызывает бизнес-логику обработки изображения сообщения
     * 4. Управляет транзакцией
     *
     * @param event событие загрузки изображения в сообщении для обработки
     */
    @Override
    @Transactional
    public void handle(final MessageImageUploadedEvent event) {
        try {
            log.info("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_СООБЩЕНИЯ_ОБРАБОТКА_НАЧАЛО: eventId={}, messageId={}, userId={}",
                    event.getEventId(), event.getMessageId(), event.getOwnerId());

            validator.validateEvent(event);
            log.debug("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_СООБЩЕНИЯ_ВАЛИДАЦИЯ_ПРОЙДЕНА: eventId={}", event.getEventId());

            final MessageUploadImageRequest request = mapper.map(event, MessageUploadImageRequest.class);
            log.debug("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_СООБЩЕНИЯ_ПРЕОБРАЗОВАНИЕ_УСПЕХ: eventId={}", event.getEventId());

            messageService.uploadImage(event.getOwnerId(), request);

            log.info("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_СООБЩЕНИЯ_ОБРАБОТКА_УСПЕХ: eventId={}, messageId={}, userId={}",
                    event.getEventId(), event.getMessageId(), event.getOwnerId());

        } catch (Exception e) {
            log.error("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_СООБЩЕНИЯ_ОБРАБОТКА_ОШИБКА: eventId={}, messageId={}, userId={}",
                    event.getEventId(), event.getMessageId(), event.getOwnerId(), e);
            throw e;
        }
    }
}