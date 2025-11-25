package ru.cs.vsu.social_network.upload_service.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import ru.cs.vsu.social_network.upload_service.entity.MediaEntity;
import ru.cs.vsu.social_network.upload_service.event.AvatarUploadedEvent;
import ru.cs.vsu.social_network.upload_service.mapping.EntityMapper;

import java.util.concurrent.CompletableFuture;

/**
 * Сервис для отправки событий в Kafka.
 * Отвечает за публикацию событий, связанных с загрузкой медиа-файлов.
 * Обеспечивает асинхронную отправку с обработкой ошибок и callback'ами.
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public final class UploadProducer {

    private final KafkaTemplate<String, AvatarUploadedEvent> avatarUploadedEventKafkaTemplate;
    private final EntityMapper mapper;

    private static final String AVATAR_TOPIC = "avatar.events";

    /**
     * Отправляет событие загрузки аватара в Kafka.
     * Преобразует сущность MediaEntity в событие AvatarUploadedEvent и отправляет в топик.
     * Использует ownerId в качестве ключа для гарантии порядка сообщений одного пользователя.
     * Обрабатывает успешную отправку и ошибки через CompletableFuture.
     *
     * @param entity сущность медиа-файла, содержащая информацию о загруженном аватаре
     * @throws RuntimeException если произошла критическая ошибка при подготовке или отправке сообщения
     */
    public void sendAvatarUploadedEvent(final MediaEntity entity) {
        try {
            log.info("AVATAR_EVENT_ОТПРАВКА_НАЧАЛО: mediaId={} ownerId={}",
                    entity.getId(), entity.getOwnerId());

            final AvatarUploadedEvent event = mapper.map(entity, AvatarUploadedEvent.class);
            final String key = event.getOwnerId().toString();

            final CompletableFuture<SendResult<String, AvatarUploadedEvent>> future =
                    avatarUploadedEventKafkaTemplate.send(AVATAR_TOPIC, key, event);

            future.thenAccept(result -> {
                log.info("AVATAR_EVENT_ОТПРАВКА_УСПЕХ: mediaId={} ownerId={} partition={} offset={}",
                        entity.getId(), key,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }).exceptionally(failure -> {
                log.error("AVATAR_EVENT_ОТПРАВКА_ОШИБКА: mediaId={} ownerId={}",
                        entity.getId(), key, failure);
                return null;
            });

        } catch (Exception e) {
            log.error("AVATAR_EVENT_ОТПРАВКА_КРИТИЧЕСКАЯ_ОШИБКА: mediaId={} ownerId={}",
                    entity.getId(), entity.getOwnerId(), e);
            throw new RuntimeException("Ошибка при отправке события в Kafka", e);
        }
    }
}