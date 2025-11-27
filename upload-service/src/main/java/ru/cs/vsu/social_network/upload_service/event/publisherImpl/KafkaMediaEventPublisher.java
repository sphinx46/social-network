package ru.cs.vsu.social_network.upload_service.event.publisherImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.upload_service.entity.MediaEntity;
import ru.cs.vsu.social_network.upload_service.event.publisher.MediaEventPublisher;
import ru.cs.vsu.social_network.upload_service.producer.UploadProducer;

import java.util.UUID;

/**
 * Реализация публикатора событий медиа с использованием Kafka.
 * Отвечает за отправку событий, связанных с операциями над медиа-файлами,
 * в соответствующие топики Kafka для уведомления других сервисов системы.
 * Инкапсулирует детали реализации работы с Kafka брокером.
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMediaEventPublisher implements MediaEventPublisher {

    private final UploadProducer uploadProducer;

    /**
     * Публикует событие загрузки аватара пользователя в Kafka.
     * Отправляет событие с информацией о загруженном аватаре для уведомления
     * заинтересованных сервисов (например, user-profile-service).
     * Обеспечивает асинхронную отправку с обработкой ошибок на уровне продюсера.
     *
     * @param mediaEntity сущность медиа, содержащая метаданные загруженного аватара
     * @throws RuntimeException если произошла критическая ошибка при отправке события
     */
    @Override
    public void publishAvatarUploaded(final MediaEntity mediaEntity) {
        log.info("KAFKA_AVATAR_EVENT_ПУБЛИКАЦИЯ_НАЧАЛО: mediaId={} ownerId={}",
                mediaEntity.getId(), mediaEntity.getOwnerId());

        uploadProducer.sendAvatarUploadedEvent(mediaEntity);

        log.debug("KAFKA_AVATAR_EVENT_ПУБЛИКАЦИЯ_ЗАВЕРШЕНА: mediaId={}",
                mediaEntity.getId());
    }

    /**
     * Публикует событие загрузки изображения поста в Kafka.
     * Отправляет событие с информацией о загруженном изображении поста для уведомления
     * заинтересованных сервисов (например, content-service).
     * Обеспечивает асинхронную отправку с обработкой ошибок на уровне продюсера.
     *
     * @param mediaEntity сущность медиа, содержащая метаданные загруженного изображения поста
     * @param postId идентификатор поста
     * @throws RuntimeException если произошла критическая ошибка при отправке события
     */
    @Override
    public void publishPostImageUploaded(final MediaEntity mediaEntity, final UUID postId) {
        log.info("KAFKA_POST_IMAGE_EVENT_ПУБЛИКАЦИЯ_НАЧАЛО: mediaId={} ownerId={} postId={}",
                mediaEntity.getId(), mediaEntity.getOwnerId(), postId);

        uploadProducer.sendPostImageUploadedEvent(mediaEntity, postId);

        log.debug("KAFKA_POST_IMAGE_EVENT_ПУБЛИКАЦИЯ_ЗАВЕРШЕНА: mediaId={} postId={}",
                mediaEntity.getId(), postId);
    }
}