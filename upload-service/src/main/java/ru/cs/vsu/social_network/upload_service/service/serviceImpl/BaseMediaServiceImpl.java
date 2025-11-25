package ru.cs.vsu.social_network.upload_service.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.cs.vsu.social_network.upload_service.config.properties.MinioProperties;
import ru.cs.vsu.social_network.upload_service.config.security.GatewayUserContext;
import ru.cs.vsu.social_network.upload_service.dto.request.MediaDeleteRequest;
import ru.cs.vsu.social_network.upload_service.dto.request.MediaDownloadRequest;
import ru.cs.vsu.social_network.upload_service.dto.request.MediaUploadRequest;
import ru.cs.vsu.social_network.upload_service.dto.response.MediaContentResponse;
import ru.cs.vsu.social_network.upload_service.dto.response.MediaMetadataResponse;
import ru.cs.vsu.social_network.upload_service.dto.response.MediaResponse;
import ru.cs.vsu.social_network.upload_service.entity.MediaEntity;
import ru.cs.vsu.social_network.upload_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.upload_service.provider.MediaEntityProvider;
import ru.cs.vsu.social_network.upload_service.repository.MediaRepository;
import ru.cs.vsu.social_network.upload_service.service.MediaService;
import ru.cs.vsu.social_network.upload_service.storage.MediaStorageClient;
import ru.cs.vsu.social_network.upload_service.utils.GeneratorObjectName;
import ru.cs.vsu.social_network.upload_service.validation.MediaValidator;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BaseMediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final EntityMapper mapper;
    private final GeneratorObjectName generatorObjectName;
    private final MinioProperties minioProperties;
    private final MediaValidator mediaValidator;
    private final MediaEntityProvider mediaEntityProvider;
    private final GatewayUserContext gatewayUserContext;
    private final MediaStorageClient storageClient;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MediaResponse uploadFile(final MediaUploadRequest request) {
        UUID ownerId = gatewayUserContext.requireUserId();
        MultipartFile file = request.getFile();
        mediaValidator.validateFile(file);

        String objectName = generatorObjectName.generateObjectName(file, request.getCategory());
        storageClient.upload(objectName, file);

        MediaEntity saved = mediaRepository.save(buildMediaEntity(request, ownerId, file, objectName));
        log.info("МЕДИА_ЗАГРУЗКА_УСПЕХ: mediaId={} ownerId={}", saved.getId(), ownerId);
        return mapper.map(saved, MediaResponse.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public MediaMetadataResponse getMetaData(final UUID id) {
        UUID requesterId = gatewayUserContext.requireUserId();
        MediaEntity media = mediaEntityProvider.findOwnedMedia(id, requesterId);
        log.debug("МЕДИА_МЕТАДАННЫЕ: mediaId={} ownerId={}", id, requesterId);
        return mapper.map(media, MediaMetadataResponse.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public MediaContentResponse download(final MediaDownloadRequest request) {
        UUID requesterId = gatewayUserContext.requireUserId();
        MediaEntity media = mediaEntityProvider.findOwnedMedia(request.getMediaId(), requesterId);

        log.info("МЕДИА_СКАЧИВАНИЕ_УСПЕХ: mediaId={} ownerId={}", media.getId(), requesterId);
        return MediaContentResponse.builder()
                .content(storageClient.download(media.getObjectName()))
                .mimeType(media.getMimeType())
                .originalFileName(media.getOriginalFileName())
                .size(media.getSize())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteFile(final MediaDeleteRequest request) {
        UUID requesterId = gatewayUserContext.requireUserId();
        MediaEntity media = mediaEntityProvider.findOwnedMedia(request.getMediaId(), requesterId);

        storageClient.delete(media.getObjectName());
        mediaRepository.delete(media);
        log.info("МЕДИА_УДАЛЕНИЕ_УСПЕХ: mediaId={} ownerId={}", media.getId(), requesterId);
    }

    private String buildPublicUrl(final String objectName) {
        String base = minioProperties.getPublicUrl();
        return base.endsWith("/") ? base + objectName : base + "/" + objectName;
    }

    /**
     * Формирует сущность медиа для сохранения.
     *
     * @param request    исходный запрос
     * @param ownerId    идентификатор владельца
     * @param file       загружаемый файл
     * @param objectName имя объекта в хранилище
     * @return заполненная сущность
     */
    private MediaEntity buildMediaEntity(final MediaUploadRequest request,
                                         final UUID ownerId,
                                         final MultipartFile file,
                                         final String objectName) {
        return MediaEntity.builder()
                .ownerId(ownerId)
                .category(request.getCategory())
                .description(request.getDescription())
                .objectName(objectName)
                .originalFileName(file.getOriginalFilename())
                .mimeType(file.getContentType())
                .size(file.getSize())
                .bucketName(minioProperties.getBucketName())
                .publicUrl(buildPublicUrl(objectName))
                .build();
    }
}
