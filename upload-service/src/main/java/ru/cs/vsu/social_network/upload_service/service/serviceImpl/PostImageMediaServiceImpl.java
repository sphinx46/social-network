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
import ru.cs.vsu.social_network.upload_service.service.PostImageMediaService;
import ru.cs.vsu.social_network.upload_service.validation.MediaValidator;

import java.util.UUID;

/**
 * Реализация сервиса для работы с изображениями постов.
 * Использует композицию для делегирования базовых операций.
 * Добавляет специфичную логику для изображений постов.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostImageMediaServiceImpl implements PostImageMediaService {

    private final MediaService mediaService;
    private final MediaValidator mediaValidator;
    private final MediaEventPublisher eventPublisher;
    private final MediaEntityProvider mediaEntityProvider;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MediaResponse uploadPostImage(final MediaUploadRequest request, final UUID postId) {
        log.info("ИЗОБРАЖЕНИЕ_ПОСТА_ЗАГРУЗКА_НАЧАЛО: category={}, postId={}", request.getCategory(), postId);

        mediaValidator.validateFile(request.getFile());
        final MediaResponse response = mediaService.uploadFile(request);

        final MediaEntity mediaEntity = mediaEntityProvider.findByMediaId(response.getId());

        eventPublisher.publishPostImageUploaded(mediaEntity, postId);

        log.info("ИЗОБРАЖЕНИЕ_ПОСТА_ЗАГРУЗКА_УСПЕХ: mediaId={}, postId={}",
                response.getId(), postId);

        return response;
    }
}