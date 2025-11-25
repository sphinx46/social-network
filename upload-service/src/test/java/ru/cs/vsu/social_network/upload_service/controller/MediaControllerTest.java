package ru.cs.vsu.social_network.upload_service.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import ru.cs.vsu.social_network.upload_service.dto.request.MediaUploadRequest;
import ru.cs.vsu.social_network.upload_service.dto.response.MediaContentResponse;
import ru.cs.vsu.social_network.upload_service.dto.response.MediaMetadataResponse;
import ru.cs.vsu.social_network.upload_service.dto.response.MediaResponse;
import ru.cs.vsu.social_network.upload_service.exception.AccessDeniedException;
import ru.cs.vsu.social_network.upload_service.exception.InvalidFileException;
import ru.cs.vsu.social_network.upload_service.exception.MediaNotFoundException;
import ru.cs.vsu.social_network.upload_service.exception.MinioOperationException;
import ru.cs.vsu.social_network.upload_service.exception.handler.UploadExceptionHandler;
import ru.cs.vsu.social_network.upload_service.service.AvatarMediaService;
import ru.cs.vsu.social_network.upload_service.service.MediaService;
import ru.cs.vsu.social_network.upload_service.utils.MessageConstants;
import ru.cs.vsu.social_network.upload_service.utils.TestDataFactory;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тесты для контроллера работы с медиа-файлами.
 * Проверяет корректность обработки HTTP-запросов, включая специализированные операции с аватарами.
 * Охватывает позитивные и негативные сценарии для всех endpoint'ов контроллера.
 *
 */
@ExtendWith(MockitoExtension.class)
class MediaControllerTest extends BaseControllerTest {

    private static final UUID MEDIA_ID = UUID.fromString("86a29781-1d48-4fca-b9bf-6c50d71bb657");
    private static final UUID OWNER_ID = UUID.fromString("1a1b7bde-8d81-4f0d-a0e9-22a397e3db48");

    @Mock
    private MediaService mediaService;

    @Mock
    private AvatarMediaService avatarMediaService;

    @Override
    protected Object controllerUnderTest() {
        return new MediaController(mediaService, avatarMediaService);
    }

    @Override
    protected Object[] controllerAdvices() {
        return new Object[]{new UploadExceptionHandler()};
    }

    @Test
    @DisplayName("Загрузка аватара - успешно")
    void uploadAvatar_whenRequestIsValid_shouldReturnCreated() throws Exception {
        final MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", MediaType.IMAGE_PNG_VALUE, "avatar-image".getBytes());
        final MediaResponse response = TestDataFactory.createMediaResponse(
                MEDIA_ID, OWNER_ID, "http://localhost/media/avatar.png",
                "avatar.png", MediaType.IMAGE_PNG_VALUE, 1024L,
                "AVATAR", "User avatar", "avatar.png");

        when(avatarMediaService.uploadAvatar(any(MediaUploadRequest.class))).thenReturn(response);

        mockMvcUtils.performMultipart("/media/avatars", file,
                        Map.of("category", "AVATAR", "description", "User avatar"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(MEDIA_ID.toString()))
                .andExpect(jsonPath("$.publicUrl").value("http://localhost/media/avatar.png"))
                .andExpect(jsonPath("$.category").value("AVATAR"));

        verify(avatarMediaService).uploadAvatar(any(MediaUploadRequest.class));
    }

    @Test
    @DisplayName("Загрузка аватара - неверный тип файла")
    void uploadAvatar_whenInvalidFileType_shouldReturnBadRequest() throws Exception {
        final MockMultipartFile file = new MockMultipartFile(
                "file", "script.exe", "application/x-msdownload", "malicious".getBytes());

        when(avatarMediaService.uploadAvatar(any(MediaUploadRequest.class)))
                .thenThrow(new InvalidFileException("Недопустимый тип файла для аватара"));

        mockMvcUtils.performMultipart("/media/avatars", file,
                        Map.of("category", "AVATAR", "description", "User avatar"))
                .andExpect(status().isBadRequest());

        verify(avatarMediaService).uploadAvatar(any(MediaUploadRequest.class));
    }

    @Test
    @DisplayName("Загрузка аватара - обязательная категория отсутствует")
    void uploadAvatar_whenCategoryMissing_shouldReturnBadRequest() throws Exception {
        final MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", MediaType.IMAGE_PNG_VALUE, "avatar-image".getBytes());

        mockMvcUtils.performMultipart("/media/avatars", file, Map.of())
                .andExpect(status().isBadRequest());

        verify(avatarMediaService, never()).uploadAvatar(any(MediaUploadRequest.class));
    }

    @Test
    @DisplayName("Загрузка файла - успешно")
    void uploadFile_whenRequestIsValid_shouldReturnCreated() throws Exception {
        final MockMultipartFile file = new MockMultipartFile(
                "file", "document.pdf", MediaType.APPLICATION_PDF_VALUE, "pdf-content".getBytes());
        final MediaResponse response = TestDataFactory.createMediaResponse(
                MEDIA_ID, OWNER_ID, "http://localhost/media/document.pdf",
                "document.pdf", MediaType.APPLICATION_PDF_VALUE, 2048L,
                "DOCUMENT", "Important document", "document.pdf");

        when(mediaService.uploadFile(any(MediaUploadRequest.class))).thenReturn(response);

        mockMvcUtils.performMultipart("/media", file,
                        Map.of("category", "DOCUMENT", "description", "Important document"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(MEDIA_ID.toString()))
                .andExpect(jsonPath("$.publicUrl").value("http://localhost/media/document.pdf"));

        verify(mediaService).uploadFile(any(MediaUploadRequest.class));
    }

    @Test
    @DisplayName("Загрузка файла - обязательная категория отсутствует")
    void uploadFile_whenCategoryMissing_shouldReturnBadRequest() throws Exception {
        final MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", MediaType.IMAGE_PNG_VALUE, "image".getBytes());

        mockMvcUtils.performMultipart("/media", file, Map.of())
                .andExpect(status().isBadRequest());

        verify(mediaService, never()).uploadFile(any(MediaUploadRequest.class));
    }

    @Test
    @DisplayName("Получение метаданных - успешно")
    void getMetadata_whenMediaExists_shouldReturnOk() throws Exception {
        final MediaMetadataResponse metadataResponse = TestDataFactory.createMetadataResponse(
                MEDIA_ID, OWNER_ID, "AVATAR", "http://localhost/media/avatar.png");

        when(mediaService.getMetaData(MEDIA_ID)).thenReturn(metadataResponse);

        mockMvcUtils.performGet("/media/" + MEDIA_ID)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaId").value(MEDIA_ID.toString()))
                .andExpect(jsonPath("$.publicUrl").value("http://localhost/media/avatar.png"));

        verify(mediaService).getMetaData(MEDIA_ID);
    }

    @Test
    @DisplayName("Скачивание контента - успешно")
    void download_whenMediaExists_shouldReturnOk() throws Exception {
        final byte[] payload = "file-data".getBytes();
        final MediaContentResponse contentResponse = TestDataFactory.createContentResponse(
                new ByteArrayInputStream(payload),
                MediaType.IMAGE_PNG_VALUE,
                "avatar.png",
                payload.length);

        when(mediaService.download(any())).thenReturn(contentResponse);

        mockMvcUtils.performGet("/media/" + MEDIA_ID + "/content")
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("avatar.png")))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE))
                .andExpect(content().bytes(payload));

        verify(mediaService).download(any());
    }

    @Test
    @DisplayName("Удаление медиа - успешно")
    void delete_whenMediaExists_shouldReturnNoContent() throws Exception {
        mockMvcUtils.performDelete("/media/" + MEDIA_ID)
                .andExpect(status().isNoContent());

        verify(mediaService).deleteFile(any());
    }

    @Test
    @DisplayName("Загрузка файла - неверный тип файла")
    void uploadFile_whenInvalidFileType_shouldReturnBadRequest() throws Exception {
        final MockMultipartFile file = new MockMultipartFile(
                "file", "script.exe", "application/x-msdownload", "malicious".getBytes());

        when(mediaService.uploadFile(any(MediaUploadRequest.class)))
                .thenThrow(new InvalidFileException("Недопустимый тип файла"));

        mockMvcUtils.performMultipart("/media", file,
                        Map.of("category", "avatar", "description", "Profile image"))
                .andExpect(status().isBadRequest());

        verify(mediaService).uploadFile(any(MediaUploadRequest.class));
    }

    @Test
    @DisplayName("Получение метаданных - медиа не найдено")
    void getMetadata_whenMediaNotFound_shouldReturnNotFound() throws Exception {
        when(mediaService.getMetaData(MEDIA_ID))
                .thenThrow(new MediaNotFoundException("Медиа не найдено"));

        mockMvcUtils.performGet("/media/" + MEDIA_ID)
                .andExpect(status().isNotFound());

        verify(mediaService).getMetaData(MEDIA_ID);
    }

    @Test
    @DisplayName("Скачивание контента - доступ запрещен")
    void download_whenAccessDenied_shouldReturnForbidden() throws Exception {
        when(mediaService.download(any()))
                .thenThrow(new AccessDeniedException("Доступ запрещен"));

        mockMvcUtils.performGet("/media/" + MEDIA_ID + "/content")
                .andExpect(status().isForbidden());

        verify(mediaService).download(any());
    }

    @Test
    @DisplayName("Удаление медиа - операция в MinIO завершилась ошибкой")
    void delete_whenMinioOperationFails_shouldReturnBadRequest() throws Exception {
        final MinioOperationException minioException = new MinioOperationException(
                MessageConstants.FILE_DELETE_EXCEPTION,
                new RuntimeException(MessageConstants.FILE_DELETE_EXCEPTION)
        );

        doThrow(minioException)
                .when(mediaService).deleteFile(any());

        mockMvcUtils.performDelete("/media/" + MEDIA_ID)
                .andExpect(status().isBadRequest());

        verify(mediaService).deleteFile(any());
    }
}