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
import ru.cs.vsu.social_network.upload_service.service.CommentImageMediaService;
import ru.cs.vsu.social_network.upload_service.validation.MediaValidator;

import java.util.UUID;

/**
 * Реализация сервиса для работы с изображениями комментариев.
 * Использует композицию для делегирования базовых операций.
 * Добавляет специфичную логику для изображений комментариев.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentImageMediaServiceImpl implements CommentImageMediaService {

    private final MediaService mediaService;
    private final MediaValidator mediaValidator;
    private final MediaEventPublisher eventPublisher;
    private final MediaEntityProvider mediaEntityProvider;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MediaResponse uploadCommentImage(final MediaUploadRequest request,
                                            final UUID commentId,
                                            final UUID postId) {
        log.info("ИЗОБРАЖЕНИЕ_КОММЕНТАРИЯ_ЗАГРУЗКА_НАЧАЛО: category={}, commentId={}, postId={}",
                request.getCategory(), commentId, postId);

        mediaValidator.validateFile(request.getFile());
        final MediaResponse response = mediaService.uploadFile(request);

        final MediaEntity mediaEntity = mediaEntityProvider.findByMediaId(response.getId());

        eventPublisher.publishCommentImageUploaded(mediaEntity, commentId, postId);

        log.info("ИЗОБРАЖЕНИЕ_КОММЕНТАРИЯ_ЗАГРУЗКА_УСПЕХ: mediaId={}, commentId={}, postId={}",
                response.getId(), commentId, postId);

        return response;
    }
}