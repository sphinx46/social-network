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

@ExtendWith(MockitoExtension.class)
class AvatarMediaServiceImplTest {

    private static final UUID MEDIA_ID = UUID.fromString("ad8809a2-a0e0-422b-bcc9-368f0f4d4a1c");
    private static final UUID OWNER_ID = UUID.fromString("510b39ce-fff5-4c87-96d0-8e55758018bb");

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
    void uploadAvatar_whenRequestIsValid_shouldReturnResponse() {
        MockMultipartFile file = TestDataFactory.createAvatarFile(
                "avatar.png", MediaType.IMAGE_PNG_VALUE, "image".getBytes());
        MediaUploadRequest request = TestDataFactory.createAvatarUploadRequest(file, "Profile avatar");
        MediaResponse expectedResponse = TestDataFactory.createAvatarResponse(MEDIA_ID, OWNER_ID);
        MediaEntity mediaEntity = TestDataFactory.createAvatarEntity(MEDIA_ID, OWNER_ID);

        doNothing().when(mediaValidator).validateFile(file);
        when(mediaService.uploadFile(request)).thenReturn(expectedResponse);
        when(mediaEntityProvider.findByMediaId(MEDIA_ID)).thenReturn(mediaEntity);

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
        verify(eventPublisher, never()).publishAvatarUploaded(any());
    }

    @Test
    @DisplayName("Загрузка аватара - слишком большой файл")
    void uploadAvatar_whenFileTooLarge_shouldThrowException() {
        MockMultipartFile file = TestDataFactory.createAvatarFile(
                "large-avatar.png", MediaType.IMAGE_PNG_VALUE, new byte[20 * 1024 * 1024]);
        MediaUploadRequest request = TestDataFactory.createAvatarUploadRequest(file, "Large avatar");

        doThrow(new InvalidFileException("Размер файла превышает допустимый лимит"))
                .when(mediaValidator).validateFile(file);

        assertThrows(InvalidFileException.class, () -> avatarMediaService.uploadAvatar(request));

        verify(mediaValidator).validateFile(file);
        verify(mediaService, never()).uploadFile(any());
        verify(eventPublisher, never()).publishAvatarUploaded(any());
    }

    @Test
    @DisplayName("Загрузка аватара - поврежденный файл")
    void uploadAvatar_whenCorruptedFile_shouldThrowException() {
        MockMultipartFile file = TestDataFactory.createAvatarFile(
                "corrupted-avatar.png", MediaType.IMAGE_PNG_VALUE, "corrupted".getBytes());
        MediaUploadRequest request = TestDataFactory.createAvatarUploadRequest(file, "Corrupted avatar");

        doThrow(new InvalidFileException("Файл поврежден или имеет неверный формат"))
                .when(mediaValidator).validateFile(file);

        assertThrows(InvalidFileException.class, () -> avatarMediaService.uploadAvatar(request));

        verify(mediaValidator).validateFile(file);
        verify(mediaService, never()).uploadFile(any());
        verify(eventPublisher, never()).publishAvatarUploaded(any());
    }

    @Test
    @DisplayName("Загрузка аватара - неподдерживаемый формат")
    void uploadAvatar_whenUnsupportedFormat_shouldThrowException() {
        MockMultipartFile file = TestDataFactory.createAvatarFile(
                "document.pdf", MediaType.APPLICATION_PDF_VALUE, "pdf-messaging".getBytes());
        MediaUploadRequest request = TestDataFactory.createAvatarUploadRequest(file, "PDF document");

        doThrow(new InvalidFileException("Неподдерживаемый формат файла для аватара"))
                .when(mediaValidator).validateFile(file);

        assertThrows(InvalidFileException.class, () -> avatarMediaService.uploadAvatar(request));

        verify(mediaValidator).validateFile(file);
        verify(mediaService, never()).uploadFile(any());
        verify(eventPublisher, never()).publishAvatarUploaded(any());
    }

    @Test
    @DisplayName("Загрузка аватара - успешно с JPEG форматом")
    void uploadAvatar_whenJpegFormat_shouldReturnResponse() {
        MockMultipartFile file = TestDataFactory.createAvatarFile(
                "avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "jpeg-messaging".getBytes());
        MediaUploadRequest request = TestDataFactory.createAvatarUploadRequest(file, "JPEG avatar");
        MediaResponse expectedResponse = TestDataFactory.createAvatarResponse(MEDIA_ID, OWNER_ID);
        MediaEntity mediaEntity = TestDataFactory.createAvatarEntity(MEDIA_ID, OWNER_ID);

        doNothing().when(mediaValidator).validateFile(file);
        when(mediaService.uploadFile(request)).thenReturn(expectedResponse);
        when(mediaEntityProvider.findByMediaId(MEDIA_ID)).thenReturn(mediaEntity);

        MediaResponse actualResponse = avatarMediaService.uploadAvatar(request);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        verify(mediaValidator).validateFile(file);
        verify(mediaService).uploadFile(request);
        verify(mediaEntityProvider).findByMediaId(MEDIA_ID);
        verify(eventPublisher).publishAvatarUploaded(mediaEntity);
    }
}