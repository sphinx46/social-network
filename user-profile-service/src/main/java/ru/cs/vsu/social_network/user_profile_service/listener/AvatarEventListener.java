package ru.cs.vsu.social_network.user_profile_service.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.user_profile_service.events.AvatarUploadedEvent;
import ru.cs.vsu.social_network.user_profile_service.events.handler.AvatarUploadedEventHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public final class AvatarEventListener {

    private final AvatarUploadedEventHandler eventHandler;

    /**
     * Обрабатывает входящие события загрузки аватаров из топика Kafka.
     * Ловит сообщения из топика 'avatar-uploaded' и передает их на обработку.
     *
     * @param event событие загрузки аватара
     */
    @KafkaListener(
            topics = "avatar-uploaded",
            groupId = "user-profile-service-group",
            containerFactory = "kafkaListenerContainerFactoryAvatarUpload"
    )
    public void onAvatarUploaded(final AvatarUploadedEvent event) {
        try {
            log.info("СОБЫТИЕ_АВАТАРА_ПОЛУЧЕНО: eventId={}, userId={}",
                    event.getEventId(), event.getOwnerId());

            eventHandler.handle(event);

            log.debug("СОБЫТИЕ_АВАТАРА_ОБРАБОТКА_ЗАВЕРШЕНА: eventId={}", event.getEventId());

        } catch (Exception e) {
            log.error("СОБЫТИЕ_АВАТАРА_ОБРАБОТКА_ПРЕРВАНА: eventId={}, userId={}",
                    event.getEventId(), event.getOwnerId(), e);
            throw e;
        }
    }
}