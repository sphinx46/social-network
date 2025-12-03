package ru.cs.vsu.social_network.messaging_service.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.messaging_service.event.MessageImageUploadedEvent;
import ru.cs.vsu.social_network.messaging_service.event.handler.MessageImageUploadedEventHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public final class MessageImageEventListener {

    private final MessageImageUploadedEventHandler eventHandler;

    /**
     * Обрабатывает входящие события загрузки изображений в сообщениях из топика Kafka.
     * Ловит сообщения из топика 'messaging-image-uploaded' и передает их на обработку.
     *
     * @param event событие загрузки изображения в сообщении
     */
    @KafkaListener(
            topics = "messaging-image-uploaded",
            groupId = "messaging-service-group",
            containerFactory = "kafkaListenerContainerFactoryMessageImageUpload"
    )
    public void onMessageImageUploaded(final MessageImageUploadedEvent event) {
        try {
            log.info("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_СООБЩЕНИЯ_ПОЛУЧЕНО: eventId={}, messageId={}, userId={}",
                    event.getEventId(), event.getMessageId(), event.getOwnerId());

            eventHandler.handle(event);

            log.debug("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_СООБЩЕНИЯ_ОБРАБОТКА_ЗАВЕРШЕНА: eventId={}", event.getEventId());

        } catch (Exception e) {
            log.error("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_СООБЩЕНИЯ_ОБРАБОТКА_ПРЕРВАНА: eventId={}, messageId={}",
                    event.getEventId(), event.getMessageId(), e);
            throw e;
        }
    }
}