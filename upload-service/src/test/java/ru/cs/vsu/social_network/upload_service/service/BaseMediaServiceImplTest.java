package ru.cs.vsu.social_network.upload_service.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
import ru.cs.vsu.social_network.upload_service.exception.AccessDeniedException;
import ru.cs.vsu.social_network.upload_service.exception.InvalidFileException;
import ru.cs.vsu.social_network.upload_service.exception.MediaNotFoundException;
import ru.cs.vsu.social_network.upload_service.exception.MinioOperationException;
import ru.cs.vsu.social_network.upload_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.upload_service.provider.MediaEntityProvider;
import ru.cs.vsu.social_network.upload_service.repository.MediaRepository;
import ru.cs.vsu.social_network.upload_service.service.serviceImpl.BaseMediaServiceImpl;
import ru.cs.vsu.social_network.upload_service.storage.MediaStorageClient;
import ru.cs.vsu.social_network.upload_service.utils.GeneratorObjectName;
import ru.cs.vsu.social_network.upload_service.utils.TestDataFactory;
import ru.cs.vsu.social_network.upload_service.validation.MediaValidator;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для {@link BaseMediaServiceImpl}.
 * Проверяет корректность работы сервиса загрузки файлов, работы с MiniO.
 */
@ExtendWith(MockitoExtension.class)
class BaseMediaServiceImplTest {

    private static final UUID MEDIA_ID = UUID.fromString("1be3e6d7-ec6f-49ad-95ac-6c752ad8172e");
    private static final UUID OWNER_ID = UUID.fromString("e0d8a734-6f6c-4ab4-b4fe-e93cc63d8406");

    @Mock
    private MediaRepository mediaRepository;
    @Mock
    private EntityMapper mapper;
    @Mock
    private MinioProperties minioProperties;
    @Mock
    private MediaValidator mediaValidator;
    @Mock
    private MediaEntityProvider mediaEntityProvider;
    @Mock
    private GatewayUserContext gatewayUserContext;
    @Mock
    private MediaStorageClient storageClient;
    @Mock
    private GeneratorObjectName generatorObjectName;

    @InjectMocks
    private BaseMediaServiceImpl mediaService;

    @Test
    @DisplayName("Загрузка файла - успешно")
    void uploadFile_whenRequestIsValid() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", MediaType.IMAGE_PNG_VALUE, "image".getBytes());
        MediaUploadRequest request = TestDataFactory.createUploadRequest(file, "avatar", "Profile pic");
        MediaEntity savedEntity = TestDataFactory.createMediaEntity(
                MEDIA_ID, OWNER_ID, "avatar", "Profile pic",
                "owner/avatar.png", "avatar.png", MediaType.IMAGE_PNG_VALUE, 5L,
                "media", "http://localhost:9000/media/owner/avatar.png");
        MediaResponse expectedResponse = TestDataFactory.createMediaResponse(
                MEDIA_ID, OWNER_ID, savedEntity.getPublicUrl(),
                savedEntity.getObjectName(), savedEntity.getMimeType(),
                savedEntity.getSize(), savedEntity.getCategory(),
                savedEntity.getDescription(), savedEntity.getOriginalFileName());

        when(minioProperties.getBucketName()).thenReturn("media");
        when(minioProperties.getPublicUrl()).thenReturn("http://localhost:9000/media");
        when(gatewayUserContext.requireUserId()).thenReturn(OWNER_ID);
        when(generatorObjectName.generateObjectName(file, "avatar")).thenReturn("owner/avatar.png");
        doNothing().when(mediaValidator).validateFile(file);
        when(mediaRepository.save(any(MediaEntity.class))).thenReturn(savedEntity);
        doNothing().when(storageClient).upload("owner/avatar.png", file);
        when(mapper.map(savedEntity, MediaResponse.class)).thenReturn(expectedResponse);

        MediaResponse actual = mediaService.uploadFile(request);

        assertNotNull(actual);
        assertEquals(expectedResponse, actual);
        verify(mediaRepository).save(any(MediaEntity.class));
        verify(storageClient).upload(any(String.class), eq(file));
    }

    @Test
    @DisplayName("Получение метаданных - успешно")
    void getMetaData_whenMediaExists() {
        MediaEntity entity = TestDataFactory.createMediaEntity(
                MEDIA_ID, OWNER_ID, "avatar", "Profile pic",
                "owner/avatar.png", "avatar.png", MediaType.IMAGE_PNG_VALUE, 5L,
                "media", "http://localhost:9000/media/owner/avatar.png");
        MediaMetadataResponse metadataResponse = TestDataFactory.createMetadataResponse(
                MEDIA_ID, OWNER_ID, "avatar", entity.getPublicUrl());

        when(gatewayUserContext.requireUserId()).thenReturn(OWNER_ID);
        when(mediaEntityProvider.findOwnedMedia(MEDIA_ID, OWNER_ID)).thenReturn(entity);
        when(mapper.map(entity, MediaMetadataResponse.class)).thenReturn(metadataResponse);

        MediaMetadataResponse actual = mediaService.getMetaData(MEDIA_ID);

        assertEquals(metadataResponse, actual);
        verify(mediaEntityProvider).findOwnedMedia(MEDIA_ID, OWNER_ID);
    }

    @Test
    @DisplayName("Скачивание файла - успешно")
    void download_whenMediaExists() throws Exception {
        MediaEntity entity = TestDataFactory.createMediaEntity(
                MEDIA_ID, OWNER_ID, "avatar", "Profile pic",
                "owner/avatar.png", "avatar.png", MediaType.IMAGE_PNG_VALUE, 5L,
                "media", "http://localhost:9000/media/owner/avatar.png");
        byte[] payload = "file".getBytes();
        InputStream contentStream = new ByteArrayInputStream(payload);

        when(gatewayUserContext.requireUserId()).thenReturn(OWNER_ID);
        when(mediaEntityProvider.findOwnedMedia(MEDIA_ID, OWNER_ID)).thenReturn(entity);
        when(storageClient.download(entity.getObjectName())).thenReturn(contentStream);

        MediaContentResponse response = mediaService.download(
                MediaDownloadRequest.builder().mediaId(MEDIA_ID).build());

        assertEquals(entity.getMimeType(), response.getMimeType());
        assertEquals(entity.getOriginalFileName(), response.getOriginalFileName());
        assertEquals(entity.getSize(), response.getSize());
        assertArrayEquals(payload, response.getContent().readAllBytes());
    }

    @Test
    @DisplayName("Удаление файла - успешно")
    void deleteFile_whenMediaExists() {
        MediaEntity entity = TestDataFactory.createMediaEntity(
                MEDIA_ID, OWNER_ID, "avatar", "Profile pic",
                "owner/avatar.png", "avatar.png", MediaType.IMAGE_PNG_VALUE, 5L,
                "media", "http://localhost:9000/media/owner/avatar.png");

        when(gatewayUserContext.requireUserId()).thenReturn(OWNER_ID);
        when(mediaEntityProvider.findOwnedMedia(MEDIA_ID, OWNER_ID)).thenReturn(entity);

        mediaService.deleteFile(MediaDeleteRequest.builder().mediaId(MEDIA_ID).build());

        verify(storageClient).delete(entity.getObjectName());
        verify(mediaRepository).delete(entity);
    }

    @Test
    @DisplayName("Загрузка файла - неверный тип файла")
    void uploadFile_whenInvalidFileType() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "script.exe", "application/x-msdownload", "malicious".getBytes());
        MediaUploadRequest request = TestDataFactory.createUploadRequest(file, "avatar", "Profile pic");

        doThrow(new InvalidFileException("Недопустимый тип файла"))
                .when(mediaValidator).validateFile(file);

        assertThrows(InvalidFileException.class, () -> mediaService.uploadFile(request));

        verify(mediaRepository, never()).save(any(MediaEntity.class));
        verify(storageClient, never()).upload(any(String.class), any(MultipartFile.class));
    }

    @Test
    @DisplayName("Получение метаданных - медиа не найдено")
    void getMetaData_whenMediaNotFound() {
        when(gatewayUserContext.requireUserId()).thenReturn(OWNER_ID);
        when(mediaEntityProvider.findOwnedMedia(MEDIA_ID, OWNER_ID))
                .thenThrow(new MediaNotFoundException("Медиа не найдено"));

        assertThrows(MediaNotFoundException.class, () -> mediaService.getMetaData(MEDIA_ID));

        verify(mediaEntityProvider).findOwnedMedia(MEDIA_ID, OWNER_ID);
    }

    @Test
    @DisplayName("Скачивание файла - доступ запрещен")
    void download_whenAccessDenied() {
        when(gatewayUserContext.requireUserId()).thenReturn(OWNER_ID);
        when(mediaEntityProvider.findOwnedMedia(MEDIA_ID, OWNER_ID))
                .thenThrow(new AccessDeniedException("Доступ запрещен"));

        MediaDownloadRequest request = MediaDownloadRequest.builder().mediaId(MEDIA_ID).build();

        assertThrows(AccessDeniedException.class, () -> mediaService.download(request));

        verify(mediaEntityProvider).findOwnedMedia(MEDIA_ID, OWNER_ID);
        verify(storageClient, never()).download(any(String.class));
    }

    @Test
    @DisplayName("Удаление файла - ошибка при удалении из хранилища")
    void deleteFile_whenStorageDeleteFails() {
        MediaEntity entity = TestDataFactory.createMediaEntity(
                MEDIA_ID, OWNER_ID, "avatar", "Profile pic",
                "owner/avatar.png", "avatar.png", MediaType.IMAGE_PNG_VALUE, 5L,
                "media", "http://localhost:9000/media/owner/avatar.png");

        when(gatewayUserContext.requireUserId()).thenReturn(OWNER_ID);
        when(mediaEntityProvider.findOwnedMedia(MEDIA_ID, OWNER_ID)).thenReturn(entity);

        MinioOperationException minioException = new MinioOperationException(
                "Ошибка удаления файла",
                new RuntimeException("MinIO connection error")
        );
        doThrow(minioException)
                .when(storageClient).delete(entity.getObjectName());

        MediaDeleteRequest request = MediaDeleteRequest.builder().mediaId(MEDIA_ID).build();

        assertThrows(MinioOperationException.class, () -> mediaService.deleteFile(request));

        verify(storageClient).delete(entity.getObjectName());
        verify(mediaRepository, never()).delete(entity);
    }
}