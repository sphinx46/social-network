package ru.cs.vsu.social_network.upload_service.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import ru.cs.vsu.social_network.upload_service.dto.request.MediaUploadRequest;
import ru.cs.vsu.social_network.upload_service.dto.response.MediaResponse;
import ru.cs.vsu.social_network.upload_service.entity.MediaEntity;
import ru.cs.vsu.social_network.upload_service.event.publisher.MediaEventPublisher;
import ru.cs.vsu.social_network.upload_service.exception.InvalidFileException;
import ru.cs.vsu.social_network.upload_service.provider.MediaEntityProvider;
import ru.cs.vsu.social_network.upload_service.service.serviceImpl.AvatarMediaServiceImpl;
import ru.cs.vsu.social_network.upload_service.utils.TestDataFactory;
import ru.cs.vsu.social_network.upload_service.validation.MediaValidator;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для {@link AvatarMediaServiceImpl}.
 * Проверяет корректность работы сервиса загрузки аватаров, включая валидацию и публикацию событий.
 */
@ExtendWith(MockitoExtension.class)
class AvatarMediaServiceImplTest {

    private static final UUID MEDIA_ID = UUID.fromString("1be3e6d7-ec6f-49ad-95ac-6c752ad8172e");
    private static final UUID OWNER_ID = UUID.fromString("e0d8a734-6f6c-4ab4-b4fe-e93cc63d8406");

    @Mock
    private MediaService mediaService;

    @Mock
    private MediaValidator mediaValidator;

    @Mock
    private MediaEntityProvider mediaEntityProvider;

    @Mock
    private MediaEventPublisher eventPublisher;

    @InjectMocks
    private AvatarMediaServiceImpl avatarMediaService;

    @Test
    @DisplayName("Загрузка аватара - успешно")
    void uploadAvatar_whenRequestIsValid_shouldReturnResponseAndPublishEvent() {
        MockMultipartFile file = TestDataFactory.createAvatarFile(
                "avatar.png", MediaType.IMAGE_PNG_VALUE, "image".getBytes());
        MediaUploadRequest request = TestDataFactory.createAvatarUploadRequest(file, "Profile avatar");
        MediaResponse expectedResponse = TestDataFactory.createAvatarResponse(MEDIA_ID, OWNER_ID);
        MediaEntity mediaEntity = TestDataFactory.createAvatarEntity(MEDIA_ID, OWNER_ID);

        doNothing().when(mediaValidator).validateFile(file);
        when(mediaService.uploadFile(request)).thenReturn(expectedResponse);
        when(mediaEntityProvider.findByMediaId(MEDIA_ID)).thenReturn(mediaEntity);
        doNothing().when(eventPublisher).publishAvatarUploaded(mediaEntity);

        MediaResponse actualResponse = avatarMediaService.uploadAvatar(request);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        verify(mediaValidator).validateFile(file);
        verify(mediaService).uploadFile(request);
        verify(mediaEntityProvider).findByMediaId(MEDIA_ID);
        verify(eventPublisher).publishAvatarUploaded(mediaEntity);
    }

    @Test
    @DisplayName("Загрузка аватара - невалидный файл")
    void uploadAvatar_whenInvalidFile_shouldThrowException() {
        MockMultipartFile file = TestDataFactory.createAvatarFile(
                "script.exe", "application/x-msdownload", "malicious".getBytes());
        MediaUploadRequest request = TestDataFactory.createAvatarUploadRequest(file, "Invalid file");

        doThrow(new InvalidFileException("Недопустимый тип файла"))
                .when(mediaValidator).validateFile(file);

        assertThrows(InvalidFileException.class, () -> avatarMediaService.uploadAvatar(request));

        verify(mediaValidator).validateFile(file);
        verify(mediaService, never()).uploadFile(any());
        verify(mediaEntityProvider, never()).findByMediaId(any());
        verify(eventPublisher, never()).publishAvatarUploaded(any());
    }

    @Test
    @DisplayName("Загрузка аватара - ошибка публикации события")
    void uploadAvatar_whenEventPublishingFails_shouldReturnResponse() {
        MockMultipartFile file = TestDataFactory.createAvatarFile(
                "avatar.png", MediaType.IMAGE_PNG_VALUE, "image".getBytes());
        MediaUploadRequest request = TestDataFactory.createAvatarUploadRequest(file, "Profile avatar");
        MediaResponse expectedResponse = TestDataFactory.createAvatarResponse(MEDIA_ID, OWNER_ID);

        doNothing().when(mediaValidator).validateFile(file);
        when(mediaService.uploadFile(request)).thenReturn(expectedResponse);
        when(mediaEntityProvider.findByMediaId(MEDIA_ID))
                .thenThrow(new RuntimeException("Ошибка получения медиа"));

        MediaResponse actualResponse = avatarMediaService.uploadAvatar(request);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        verify(mediaValidator).validateFile(file);
        verify(mediaService).uploadFile(request);
        verify(mediaEntityProvider).findByMediaId(MEDIA_ID);
        verify(eventPublisher, never()).publishAvatarUploaded(any());
    }
}