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
import ru.cs.vsu.social_network.upload_service.service.serviceImpl.CommentImageMediaServiceImpl;
import ru.cs.vsu.social_network.upload_service.utils.TestDataFactory;
import ru.cs.vsu.social_network.upload_service.validation.MediaValidator;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentMediaServiceImplTest {

    private static final UUID MEDIA_ID = UUID.fromString("40d4dde4-663d-4113-bd08-25f5e1d42efe");
    private static final UUID OWNER_ID = UUID.fromString("3fc4530f-5c0c-429d-8a64-810ce3bd1415");
    private static final UUID COMMENT_ID = UUID.fromString("8dce7d87-60d9-4b66-8851-e7dba4684538");
    private static final UUID POST_ID = UUID.fromString("9ecf5d87-70e9-5c77-9962-f8ecb5795649");

    @Mock
    private MediaService mediaService;

    @Mock
    private MediaValidator mediaValidator;

    @Mock
    private MediaEventPublisher eventPublisher;

    @Mock
    private MediaEntityProvider mediaEntityProvider;

    @InjectMocks
    private CommentImageMediaServiceImpl commentImageMediaService;

    @Test
    @DisplayName("Загрузка изображения комментария - успешно")
    void uploadCommentImage_whenRequestIsValid_shouldReturnResponse() {
        MockMultipartFile file = TestDataFactory.createCommentImageFile(
                "comment-image.jpg", MediaType.IMAGE_JPEG_VALUE, "image-content".getBytes());
        MediaUploadRequest request = TestDataFactory.createCommentImageUploadRequest(file, "Comment content image");
        MediaResponse expectedResponse = TestDataFactory.createCommentImageResponse(MEDIA_ID, OWNER_ID);
        MediaEntity mediaEntity = TestDataFactory.createCommentImageEntity(MEDIA_ID, OWNER_ID);

        doNothing().when(mediaValidator).validateFile(file);
        when(mediaService.uploadFile(request)).thenReturn(expectedResponse);
        when(mediaEntityProvider.findByMediaId(MEDIA_ID)).thenReturn(mediaEntity);

        MediaResponse actualResponse = commentImageMediaService.uploadCommentImage(request, COMMENT_ID, POST_ID);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        verify(mediaValidator).validateFile(file);
        verify(mediaService).uploadFile(request);
        verify(mediaEntityProvider).findByMediaId(MEDIA_ID);
        verify(eventPublisher).publishCommentImageUploaded(mediaEntity, COMMENT_ID, POST_ID);
    }

    @Test
    @DisplayName("Загрузка изображения комментария - невалидный файл")
    void uploadCommentImage_whenInvalidFile_shouldThrowException() {
        MockMultipartFile file = TestDataFactory.createCommentImageFile(
                "script.exe", "application/x-msdownload", "malicious".getBytes());
        MediaUploadRequest request = TestDataFactory.createCommentImageUploadRequest(file, "Invalid file");

        doThrow(new InvalidFileException("Недопустимый тип файла для изображения комментария"))
                .when(mediaValidator).validateFile(file);

        assertThrows(InvalidFileException.class, () -> commentImageMediaService.uploadCommentImage(request, COMMENT_ID, POST_ID));

        verify(mediaValidator).validateFile(file);
        verify(mediaService, never()).uploadFile(any());
        verify(eventPublisher, never()).publishCommentImageUploaded(any(), any(), any());
    }

    @Test
    @DisplayName("Загрузка изображения комментария - слишком большой файл")
    void uploadCommentImage_whenFileTooLarge_shouldThrowException() {
        MockMultipartFile file = TestDataFactory.createCommentImageFile(
                "large-image.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[20 * 1024 * 1024]);
        MediaUploadRequest request = TestDataFactory.createCommentImageUploadRequest(file, "Large comment image");

        doThrow(new InvalidFileException("Размер файла превышает допустимый лимит"))
                .when(mediaValidator).validateFile(file);

        assertThrows(InvalidFileException.class, () -> commentImageMediaService.uploadCommentImage(request, COMMENT_ID, POST_ID));

        verify(mediaValidator).validateFile(file);
        verify(mediaService, never()).uploadFile(any());
        verify(eventPublisher, never()).publishCommentImageUploaded(any(), any(), any());
    }

    @Test
    @DisplayName("Загрузка изображения комментария - поврежденный файл")
    void uploadCommentImage_whenCorruptedFile_shouldThrowException() {
        MockMultipartFile file = TestDataFactory.createCommentImageFile(
                "corrupted-image.jpg", MediaType.IMAGE_JPEG_VALUE, "corrupted".getBytes());
        MediaUploadRequest request = TestDataFactory.createCommentImageUploadRequest(file, "Corrupted image");

        doThrow(new InvalidFileException("Файл поврежден или имеет неверный формат"))
                .when(mediaValidator).validateFile(file);

        assertThrows(InvalidFileException.class, () -> commentImageMediaService.uploadCommentImage(request, COMMENT_ID, POST_ID));

        verify(mediaValidator).validateFile(file);
        verify(mediaService, never()).uploadFile(any());
        verify(eventPublisher, never()).publishCommentImageUploaded(any(), any(), any());
    }

    @Test
    @DisplayName("Загрузка изображения комментария - неподдерживаемый формат")
    void uploadCommentImage_whenUnsupportedFormat_shouldThrowException() {
        MockMultipartFile file = TestDataFactory.createCommentImageFile(
                "document.pdf", MediaType.APPLICATION_PDF_VALUE, "pdf-content".getBytes());
        MediaUploadRequest request = TestDataFactory.createCommentImageUploadRequest(file, "PDF document");

        doThrow(new InvalidFileException("Неподдерживаемый формат файла для изображения комментария"))
                .when(mediaValidator).validateFile(file);

        assertThrows(InvalidFileException.class, () -> commentImageMediaService.uploadCommentImage(request, COMMENT_ID, POST_ID));

        verify(mediaValidator).validateFile(file);
        verify(mediaService, never()).uploadFile(any());
        verify(eventPublisher, never()).publishCommentImageUploaded(any(), any(), any());
    }

    @Test
    @DisplayName("Загрузка изображения комментария - успешно с PNG форматом")
    void uploadCommentImage_whenPngFormat_shouldReturnResponse() {
        MockMultipartFile file = TestDataFactory.createCommentImageFile(
                "comment-image.png", MediaType.IMAGE_PNG_VALUE, "png-content".getBytes());
        MediaUploadRequest request = TestDataFactory.createCommentImageUploadRequest(file, "PNG comment image");
        MediaResponse expectedResponse = TestDataFactory.createCommentImageResponse(MEDIA_ID, OWNER_ID);
        MediaEntity mediaEntity = TestDataFactory.createCommentImageEntity(MEDIA_ID, OWNER_ID);

        doNothing().when(mediaValidator).validateFile(file);
        when(mediaService.uploadFile(request)).thenReturn(expectedResponse);
        when(mediaEntityProvider.findByMediaId(MEDIA_ID)).thenReturn(mediaEntity);

        MediaResponse actualResponse = commentImageMediaService.uploadCommentImage(request, COMMENT_ID, POST_ID);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        verify(mediaValidator).validateFile(file);
        verify(mediaService).uploadFile(request);
        verify(mediaEntityProvider).findByMediaId(MEDIA_ID);
        verify(eventPublisher).publishCommentImageUploaded(mediaEntity, COMMENT_ID, POST_ID);
    }

    @Test
    @DisplayName("Загрузка изображения комментария - успешно с WEBP форматом")
    void uploadCommentImage_whenWebpFormat_shouldReturnResponse() {
        MockMultipartFile file = TestDataFactory.createCommentImageFile(
                "comment-image.webp", "image/webp", "webp-content".getBytes());
        MediaUploadRequest request = TestDataFactory.createCommentImageUploadRequest(file, "WEBP comment image");
        MediaResponse expectedResponse = TestDataFactory.createCommentImageResponse(MEDIA_ID, OWNER_ID);
        MediaEntity mediaEntity = TestDataFactory.createCommentImageEntity(MEDIA_ID, OWNER_ID);

        doNothing().when(mediaValidator).validateFile(file);
        when(mediaService.uploadFile(request)).thenReturn(expectedResponse);
        when(mediaEntityProvider.findByMediaId(MEDIA_ID)).thenReturn(mediaEntity);

        MediaResponse actualResponse = commentImageMediaService.uploadCommentImage(request, COMMENT_ID, POST_ID);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
        verify(mediaValidator).validateFile(file);
        verify(mediaService).uploadFile(request);
        verify(mediaEntityProvider).findByMediaId(MEDIA_ID);
        verify(eventPublisher).publishCommentImageUploaded(mediaEntity, COMMENT_ID, POST_ID);
    }
}