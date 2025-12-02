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
import ru.cs.vsu.social_network.upload_service.service.serviceImpl.PostImageMediaServiceImpl;
import ru.cs.vsu.social_network.upload_service.utils.TestDataFactory;
import ru.cs.vsu.social_network.upload_service.validation.MediaValidator;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostImageMediaServiceImplTest {

    private static final UUID MEDIA_ID = UUID.fromString("40d4dde4-663d-4113-bd08-25f5e1d42efe");
    private static final UUID OWNER_ID = UUID.fromString("3fc4530f-5c0c-429d-8a64-810ce3bd1415");
    private static final UUID POST_ID = UUID.fromString("8dce7d87-60d9-4b66-8851-e7dba4684538");

    @Mock
    private MediaService mediaService;

    @Mock
    private MediaValidator mediaValidator;

    @Mock
    private MediaEventPublisher eventPublisher;

    @Mock
    private MediaEntityProvider mediaEntityProvider;

    @InjectMocks
    private PostImageMediaServiceImpl postImageMediaService;

    @Test
    @DisplayName("Загрузка изображения поста - успешно")
    void uploadPostImage_whenRequestIsValid_shouldReturnResponse() {
        MockMultipartFile file = TestDataFactory.createPostImageFile(
                "post-image.jpg", MediaType.IMAGE_JPEG_VALUE, "image-messaging".getBytes());
        MediaUploadRequest request = TestDataFactory.createPostImageUploadRequest(file, "Post messaging image");
        MediaResponse expectedResponse = TestDataFactory.createPostImageResponse(MEDIA_ID, OWNER_ID);
        MediaEntity mediaEntity = TestDataFactory.createPostImageEntity(MEDIA_ID, OWNER_ID);

        doNothing().when(mediaValidator).validateFile(file);
        when(mediaService.uploadFile(request)).thenReturn(expectedResponse);
        when(mediaEntityProvider.findByMediaId(MEDIA_ID)).thenReturn(mediaEntity);

        MediaResponse actualResponse = postImageMediaService.uploadPostImage(request, POST_ID);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        verify(mediaValidator).validateFile(file);
        verify(mediaService).uploadFile(request);
        verify(mediaEntityProvider).findByMediaId(MEDIA_ID);
        verify(eventPublisher).publishPostImageUploaded(mediaEntity, POST_ID);
    }

    @Test
    @DisplayName("Загрузка изображения поста - невалидный файл")
    void uploadPostImage_whenInvalidFile_shouldThrowException() {
        MockMultipartFile file = TestDataFactory.createPostImageFile(
                "script.exe", "application/x-msdownload", "malicious".getBytes());
        MediaUploadRequest request = TestDataFactory.createPostImageUploadRequest(file, "Invalid file");

        doThrow(new InvalidFileException("Недопустимый тип файла для изображения поста"))
                .when(mediaValidator).validateFile(file);

        assertThrows(InvalidFileException.class, () -> postImageMediaService.uploadPostImage(request, POST_ID));

        verify(mediaValidator).validateFile(file);
        verify(mediaService, never()).uploadFile(any());
        verify(eventPublisher, never()).publishPostImageUploaded(any(), any());
    }

    @Test
    @DisplayName("Загрузка изображения поста - слишком большой файл")
    void uploadPostImage_whenFileTooLarge_shouldThrowException() {
        MockMultipartFile file = TestDataFactory.createPostImageFile(
                "large-image.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[20 * 1024 * 1024]);
        MediaUploadRequest request = TestDataFactory.createPostImageUploadRequest(file, "Large post image");

        doThrow(new InvalidFileException("Размер файла превышает допустимый лимит"))
                .when(mediaValidator).validateFile(file);

        assertThrows(InvalidFileException.class, () -> postImageMediaService.uploadPostImage(request, POST_ID));

        verify(mediaValidator).validateFile(file);
        verify(mediaService, never()).uploadFile(any());
        verify(eventPublisher, never()).publishPostImageUploaded(any(), any());
    }

    @Test
    @DisplayName("Загрузка изображения поста - поврежденный файл")
    void uploadPostImage_whenCorruptedFile_shouldThrowException() {
        MockMultipartFile file = TestDataFactory.createPostImageFile(
                "corrupted-image.jpg", MediaType.IMAGE_JPEG_VALUE, "corrupted".getBytes());
        MediaUploadRequest request = TestDataFactory.createPostImageUploadRequest(file, "Corrupted image");

        doThrow(new InvalidFileException("Файл поврежден или имеет неверный формат"))
                .when(mediaValidator).validateFile(file);

        assertThrows(InvalidFileException.class, () -> postImageMediaService.uploadPostImage(request, POST_ID));

        verify(mediaValidator).validateFile(file);
        verify(mediaService, never()).uploadFile(any());
        verify(eventPublisher, never()).publishPostImageUploaded(any(), any());
    }

    @Test
    @DisplayName("Загрузка изображения поста - неподдерживаемый формат")
    void uploadPostImage_whenUnsupportedFormat_shouldThrowException() {
        MockMultipartFile file = TestDataFactory.createPostImageFile(
                "document.pdf", MediaType.APPLICATION_PDF_VALUE, "pdf-messaging".getBytes());
        MediaUploadRequest request = TestDataFactory.createPostImageUploadRequest(file, "PDF document");

        doThrow(new InvalidFileException("Неподдерживаемый формат файла для изображения поста"))
                .when(mediaValidator).validateFile(file);

        assertThrows(InvalidFileException.class, () -> postImageMediaService.uploadPostImage(request, POST_ID));

        verify(mediaValidator).validateFile(file);
        verify(mediaService, never()).uploadFile(any());
        verify(eventPublisher, never()).publishPostImageUploaded(any(), any());
    }

    @Test
    @DisplayName("Загрузка изображения поста - успешно с PNG форматом")
    void uploadPostImage_whenPngFormat_shouldReturnResponse() {
        MockMultipartFile file = TestDataFactory.createPostImageFile(
                "post-image.png", MediaType.IMAGE_PNG_VALUE, "png-messaging".getBytes());
        MediaUploadRequest request = TestDataFactory.createPostImageUploadRequest(file, "PNG post image");
        MediaResponse expectedResponse = TestDataFactory.createPostImageResponse(MEDIA_ID, OWNER_ID);
        MediaEntity mediaEntity = TestDataFactory.createPostImageEntity(MEDIA_ID, OWNER_ID);

        doNothing().when(mediaValidator).validateFile(file);
        when(mediaService.uploadFile(request)).thenReturn(expectedResponse);
        when(mediaEntityProvider.findByMediaId(MEDIA_ID)).thenReturn(mediaEntity);

        MediaResponse actualResponse = postImageMediaService.uploadPostImage(request, POST_ID);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        verify(mediaValidator).validateFile(file);
        verify(mediaService).uploadFile(request);
        verify(mediaEntityProvider).findByMediaId(MEDIA_ID);
        verify(eventPublisher).publishPostImageUploaded(mediaEntity, POST_ID);
    }

    @Test
    @DisplayName("Загрузка изображения поста - успешно с WEBP форматом")
    void uploadPostImage_whenWebpFormat_shouldReturnResponse() {
        MockMultipartFile file = TestDataFactory.createPostImageFile(
                "post-image.webp", "image/webp", "webp-messaging".getBytes());
        MediaUploadRequest request = TestDataFactory.createPostImageUploadRequest(file, "WEBP post image");
        MediaResponse expectedResponse = TestDataFactory.createPostImageResponse(MEDIA_ID, OWNER_ID);
        MediaEntity mediaEntity = TestDataFactory.createPostImageEntity(MEDIA_ID, OWNER_ID);

        doNothing().when(mediaValidator).validateFile(file);
        when(mediaService.uploadFile(request)).thenReturn(expectedResponse);
        when(mediaEntityProvider.findByMediaId(MEDIA_ID)).thenReturn(mediaEntity);

        MediaResponse actualResponse = postImageMediaService.uploadPostImage(request, POST_ID);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        verify(mediaValidator).validateFile(file);
        verify(mediaService).uploadFile(request);
        verify(mediaEntityProvider).findByMediaId(MEDIA_ID);
        verify(eventPublisher).publishPostImageUploaded(mediaEntity, POST_ID);
    }
}