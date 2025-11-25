package ru.cs.vsu.social_network.upload_service.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cs.vsu.social_network.upload_service.dto.request.MediaUploadRequest;
import ru.cs.vsu.social_network.upload_service.dto.response.MediaResponse;
import ru.cs.vsu.social_network.upload_service.entity.MediaEntity;
import ru.cs.vsu.social_network.upload_service.event.publisher.MediaEventPublisher;
import ru.cs.vsu.social_network.upload_service.provider.MediaEntityProvider;
import ru.cs.vsu.social_network.upload_service.service.AvatarMediaService;
import ru.cs.vsu.social_network.upload_service.service.MediaService;
import ru.cs.vsu.social_network.upload_service.validation.MediaValidator;

import java.util.UUID;

/**
 * Реализация сервиса для работы с аватарами пользователей.
 * Использует композицию для делегирования базовых операций.
 * Добавляет специфичную логику для аватаров и отправку событий в Kafka.
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AvatarMediaServiceImpl implements AvatarMediaService {

    private final MediaService mediaService;
    private final MediaValidator mediaValidator;
    private final MediaEntityProvider mediaEntityProvider;
    private final MediaEventPublisher eventPublisher;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MediaResponse uploadAvatar(final MediaUploadRequest request) {
        log.info("АВАТАР_ЗАГРУЗКА_НАЧАЛО: category={}", request.getCategory());

        mediaValidator.validateFile(request.getFile());
        final MediaResponse response = mediaService.uploadFile(request);

        publishAvatarUploadedEvent(response.getId());
        log.info("АВАТАР_ЗАГРУЗКА_УСПЕХ: mediaId={}", response.getId());

        return response;
    }

    /**
     * Публикует событие загрузки аватара.
     *
     * @param mediaId идентификатор загруженного медиа
     */
    private void publishAvatarUploadedEvent(final UUID mediaId) {
        try {
            final MediaEntity mediaEntity = mediaEntityProvider.findByMediaId(mediaId);
            eventPublisher.publishAvatarUploaded(mediaEntity);
            log.info("АВАТАР_СОБЫТИЕ_ОТПРАВЛЕНО: mediaId={}", mediaId);
        } catch (Exception e) {
            log.error("АВАТАР_СОБЫТИЕ_ОШИБКА: mediaId={}", mediaId, e);
        }
    }
}