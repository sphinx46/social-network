package ru.cs.vsu.social_network.contents_service.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.events.PostImageUploadedEvent;
import ru.cs.vsu.social_network.contents_service.events.handler.PostImageUploadedEventHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public final class PostImageUploadedEventListener {

    private final PostImageUploadedEventHandler eventHandler;

    /**
     * Обрабатывает входящие события загрузки изображений постов из топика Kafka.
     * Ловит сообщения из топика 'post-image-uploaded' и передает их на обработку.
     *
     * @param event событие загрузки изображения поста
     */
    @KafkaListener(
            topics = "post-image-uploaded",
            groupId = "messaging-service-group",
            containerFactory = "kafkaListenerContainerFactoryPostImageUpload"
    )
    public void onPostImageUploaded(final PostImageUploadedEvent event) {
        try {
            log.info("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_ПОСТА_ПОЛУЧЕНО: eventId={}, userId={}",
                    event.getEventId(), event.getOwnerId());

            eventHandler.handle(event);

            log.debug("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_ПОСТА_ОБРАБОТКА_ЗАВЕРШЕНА: eventId={}", event.getEventId());

        } catch (Exception e) {
            log.error("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_ПОСТА_ОБРАБОТКА_ПРЕРВАНА: eventId={}, userId={}",
                    event.getEventId(), event.getOwnerId(), e);
            throw e;
        }
    }
}