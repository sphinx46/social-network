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
import ru.cs.vsu.social_network.upload_service.service.MediaService;
import ru.cs.vsu.social_network.upload_service.service.MessageImageMediaService;
import ru.cs.vsu.social_network.upload_service.validation.MediaValidator;

import java.util.UUID;

/**
 * Реализация сервиса для работы с изображениями сообщений.
 * Использует композицию для делегирования базовых операций.
 * Добавляет специфичную логику для изображений сообщений.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageImageMediaServiceImpl implements MessageImageMediaService {

    private final MediaService mediaService;
    private final MediaValidator mediaValidator;
    private final MediaEventPublisher eventPublisher;
    private final MediaEntityProvider mediaEntityProvider;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MediaResponse uploadMessageImage(final MediaUploadRequest request, final UUID messageId) {
        log.info("ИЗОБРАЖЕНИЕ_СООБЩЕНИЯ_ЗАГРУЗКА_НАЧАЛО: category={}, messageId={}",
                request.getCategory(), messageId);

        mediaValidator.validateFile(request.getFile());
        final MediaResponse response = mediaService.uploadFile(request);

        final MediaEntity mediaEntity = mediaEntityProvider.findByMediaId(response.getId());

        eventPublisher.publishMessageImageUploaded(mediaEntity, messageId);

        log.info("ИЗОБРАЖЕНИЕ_СООБЩЕНИЯ_ЗАГРУЗКА_УСПЕХ: mediaId={}, messageId={}",
                response.getId(), messageId);

        return response;
    }
}