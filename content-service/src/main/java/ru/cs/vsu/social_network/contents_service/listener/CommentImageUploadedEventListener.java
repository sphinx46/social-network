package ru.cs.vsu.social_network.contents_service.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.events.CommentImageUploadedEvent;
import ru.cs.vsu.social_network.contents_service.events.handler.CommentImageUploadedEventHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public final class CommentImageUploadedEventListener {

    private final CommentImageUploadedEventHandler eventHandler;

    /**
     * Обрабатывает входящие события загрузки изображений комментариев из топика Kafka.
     * Ловит сообщения из топика 'comment-image-uploaded' и передает их на обработку.
     *
     * @param event событие загрузки изображения комментария
     */
    @KafkaListener(
            topics = "comment-image-uploaded",
            groupId = "content-service-group",
            containerFactory = "kafkaListenerContainerFactoryCommentImageUpload"
    )
    public void onCommentImageUploaded(final CommentImageUploadedEvent event) {
        try {
            log.info("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_КОММЕНТАРИЯ_ПОЛУЧЕНО: eventId={}, userId={}",
                    event.getEventId(), event.getOwnerId());

            eventHandler.handle(event);

            log.debug("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_КОММЕНТАРИЯ_ОБРАБОТКА_ЗАВЕРШЕНА: " +
                    "eventId={}", event.getEventId());

        } catch (Exception e) {
            log.error("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_КОММЕНТАРИЯ_ОБРАБОТКА_ПРЕРВАНА: " +
                            "eventId={}, userId={}",
                    event.getEventId(), event.getOwnerId(), e);
            throw e;
        }
    }
}