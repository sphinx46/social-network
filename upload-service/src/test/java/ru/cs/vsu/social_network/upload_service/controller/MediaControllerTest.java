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
import ru.cs.vsu.social_network.upload_service.service.CommentImageMediaService;
import ru.cs.vsu.social_network.upload_service.service.MediaService;
import ru.cs.vsu.social_network.upload_service.service.PostImageMediaService;
import ru.cs.vsu.social_network.upload_service.utils.MessageConstants;
import ru.cs.vsu.social_network.upload_service.utils.TestDataFactory;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MediaControllerTest extends BaseControllerTest {

    private static final UUID MEDIA_ID = UUID.fromString("a111da2d-c9ec-4705-85bb-28accf5b17b9");
    private static final UUID OWNER_ID = UUID.fromString("5c308f33-5bf1-4e35-ad87-0c87f74bb89c");
    private static final UUID POST_ID = UUID.fromString("38908454-6f03-45d4-9fd7-8fe6cfad9768");
    private static final UUID COMMENT_ID = UUID.fromString("33eae3d1-26ea-4a63-8c6a-50ce04f0546f");

    @Mock
    private MediaService mediaService;

    @Mock
    private AvatarMediaService avatarMediaService;

    @Mock
    private CommentImageMediaService commentImageMediaService;

    @Mock
    private PostImageMediaService postImageMediaService;

    @Override
    protected Object controllerUnderTest() {
        return new MediaController(mediaService, avatarMediaService,
                postImageMediaService, commentImageMediaService);
    }

    @Override
    protected Object[] controllerAdvices() {
        return new Object[]{new UploadExceptionHandler()};
    }

    @Test
    @DisplayName("Загрузка аватара - успешно")
    void uploadAvatar_whenRequestIsValid_shouldReturnCreated() throws Exception {
        final MockMultipartFile file = TestDataFactory.createAvatarFile(
                "avatar.png", MediaType.IMAGE_PNG_VALUE, "avatar-image".getBytes());
        final MediaResponse response = TestDataFactory.createAvatarResponse(MEDIA_ID, OWNER_ID);

        when(avatarMediaService.uploadAvatar(any(MediaUploadRequest.class))).thenReturn(response);

        mockMvcUtils.performMultipart("/media/avatars", file,
                        Map.of("category", "AVATAR", "description", "Profile avatar"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(MEDIA_ID.toString()))
                .andExpect(jsonPath("$.publicUrl").value("http://localhost/media/avatar.png"))
                .andExpect(jsonPath("$.category").value("AVATAR"));

        verify(avatarMediaService).uploadAvatar(any(MediaUploadRequest.class));
    }

    @Test
    @DisplayName("Загрузка аватара - неверный тип файла")
    void uploadAvatar_whenInvalidFileType_shouldReturnBadRequest() throws Exception {
        final MockMultipartFile file = TestDataFactory.createAvatarFile(
                "script.exe", "application/x-msdownload", "malicious".getBytes());

        when(avatarMediaService.uploadAvatar(any(MediaUploadRequest.class)))
                .thenThrow(new InvalidFileException("Недопустимый тип файла для аватара"));

        mockMvcUtils.performMultipart("/media/avatars", file,
                        Map.of("category", "AVATAR", "description", "Profile avatar"))
                .andExpect(status().isBadRequest());

        verify(avatarMediaService).uploadAvatar(any(MediaUploadRequest.class));
    }

    @Test
    @DisplayName("Загрузка аватара - обязательная категория отсутствует")
    void uploadAvatar_whenCategoryMissing_shouldReturnBadRequest() throws Exception {
        final MockMultipartFile file = TestDataFactory.createAvatarFile(
                "avatar.png", MediaType.IMAGE_PNG_VALUE, "avatar-image".getBytes());

        mockMvcUtils.performMultipart("/media/avatars", file, Map.of())
                .andExpect(status().isBadRequest());

        verify(avatarMediaService, never()).uploadAvatar(any(MediaUploadRequest.class));
    }

    @Test
    @DisplayName("Загрузка файла - успешно")
    void uploadFile_whenRequestIsValid_shouldReturnCreated() throws Exception {
        final MockMultipartFile file = new MockMultipartFile(
                "file", "document.pdf", MediaType.APPLICATION_PDF_VALUE, "pdf-messaging".getBytes());
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

        mockMvcUtils.performGet("/media/" + MEDIA_ID + "/messaging")
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

        mockMvcUtils.performGet("/media/" + MEDIA_ID + "/messaging")
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

    @Test
    @DisplayName("Загрузка изображения поста - успешно")
    void uploadPostImage_whenRequestIsValid_shouldReturnCreated() throws Exception {
        final MockMultipartFile file = TestDataFactory.createPostImageFile(
                "post-image.jpg", MediaType.IMAGE_JPEG_VALUE, "post-image-messaging".getBytes());
        final MediaResponse response = TestDataFactory.createPostImageResponse(MEDIA_ID, OWNER_ID);

        when(postImageMediaService.uploadPostImage(any(MediaUploadRequest.class), eq(POST_ID))).thenReturn(response);

        mockMvcUtils.performMultipart("/media/post-images/" + POST_ID, file,
                        Map.of("category", "POST_IMAGE", "description", "Post messaging image"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(MEDIA_ID.toString()))
                .andExpect(jsonPath("$.publicUrl").value("http://localhost/media/post-image.jpg"))
                .andExpect(jsonPath("$.category").value("POST_IMAGE"));

        verify(postImageMediaService).uploadPostImage(any(MediaUploadRequest.class), eq(POST_ID));
    }

    @Test
    @DisplayName("Загрузка изображения поста - неверный тип файла")
    void uploadPostImage_whenInvalidFileType_shouldReturnBadRequest() throws Exception {
        final MockMultipartFile file = TestDataFactory.createPostImageFile(
                "script.exe", "application/x-msdownload", "malicious".getBytes());

        when(postImageMediaService.uploadPostImage(any(MediaUploadRequest.class), eq(POST_ID)))
                .thenThrow(new InvalidFileException("Недопустимый тип файла для изображения поста"));

        mockMvcUtils.performMultipart("/media/post-images/" + POST_ID, file,
                        Map.of("category", "POST_IMAGE", "description", "Post messaging image"))
                .andExpect(status().isBadRequest());

        verify(postImageMediaService).uploadPostImage(any(MediaUploadRequest.class), eq(POST_ID));
    }

    @Test
    @DisplayName("Загрузка изображения поста - обязательная категория отсутствует")
    void uploadPostImage_whenCategoryMissing_shouldReturnBadRequest() throws Exception {
        final MockMultipartFile file = TestDataFactory.createPostImageFile(
                "post-image.jpg", MediaType.IMAGE_JPEG_VALUE, "image-messaging".getBytes());

        mockMvcUtils.performMultipart("/media/post-images/" + POST_ID, file, Map.of())
                .andExpect(status().isBadRequest());

        verify(postImageMediaService, never()).uploadPostImage(any(MediaUploadRequest.class), any());
    }

    @Test
    @DisplayName("Загрузка изображения поста - слишком большой файл")
    void uploadPostImage_whenFileTooLarge_shouldReturnBadRequest() throws Exception {
        final MockMultipartFile file = TestDataFactory.createPostImageFile(
                "large-image.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[20 * 1024 * 1024]);

        when(postImageMediaService.uploadPostImage(any(MediaUploadRequest.class), eq(POST_ID)))
                .thenThrow(new InvalidFileException("Размер файла превышает допустимый лимит"));

        mockMvcUtils.performMultipart("/media/post-images/" + POST_ID, file,
                        Map.of("category", "POST_IMAGE", "description", "Large post image"))
                .andExpect(status().isBadRequest());

        verify(postImageMediaService).uploadPostImage(any(MediaUploadRequest.class), eq(POST_ID));
    }

    @Test
    @DisplayName("Загрузка изображения поста - ошибка валидации файла")
    void uploadPostImage_whenFileValidationFails_shouldReturnBadRequest() throws Exception {
        final MockMultipartFile file = TestDataFactory.createPostImageFile(
                "corrupted-image.jpg", MediaType.IMAGE_JPEG_VALUE, "corrupted".getBytes());

        when(postImageMediaService.uploadPostImage(any(MediaUploadRequest.class), eq(POST_ID)))
                .thenThrow(new InvalidFileException("Файл поврежден или имеет неверный формат"));

        mockMvcUtils.performMultipart("/media/post-images/" + POST_ID, file,
                        Map.of("category", "POST_IMAGE", "description", "Corrupted image"))
                .andExpect(status().isBadRequest());

        verify(postImageMediaService).uploadPostImage(any(MediaUploadRequest.class), eq(POST_ID));
    }

    @Test
    @DisplayName("Загрузка изображения комментария - успешно")
    void uploadCommentImage_whenRequestIsValid_shouldReturnCreated() throws Exception {
        final MockMultipartFile file = TestDataFactory.createCommentImageFile(
                "comment-image.jpg", MediaType.IMAGE_JPEG_VALUE, "comment-image-messaging".getBytes());
        final MediaResponse response = TestDataFactory.createCommentImageResponse(MEDIA_ID, OWNER_ID);

        when(commentImageMediaService.uploadCommentImage(any(MediaUploadRequest.class), eq(COMMENT_ID), eq(POST_ID)))
                .thenReturn(response);

        mockMvcUtils.performMultipart("/media/comment-images/" + COMMENT_ID + "/post/" + POST_ID, file,
                        Map.of("category", "COMMENT_IMAGE", "description", "Comment messaging image"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(MEDIA_ID.toString()))
                .andExpect(jsonPath("$.publicUrl").value("http://localhost/media/comment-image.jpg"))
                .andExpect(jsonPath("$.category").value("COMMENT_IMAGE"));

        verify(commentImageMediaService).uploadCommentImage(any(MediaUploadRequest.class), eq(COMMENT_ID), eq(POST_ID));
    }

    @Test
    @DisplayName("Загрузка изображения комментария - неверный тип файла")
    void uploadCommentImage_whenInvalidFileType_shouldReturnBadRequest() throws Exception {
        final MockMultipartFile file = TestDataFactory.createCommentImageFile(
                "script.exe", "application/x-msdownload", "malicious".getBytes());

        when(commentImageMediaService.uploadCommentImage(any(MediaUploadRequest.class), eq(COMMENT_ID), eq(POST_ID)))
                .thenThrow(new InvalidFileException("Недопустимый тип файла для изображения комментария"));

        mockMvcUtils.performMultipart("/media/comment-images/" + COMMENT_ID + "/post/" + POST_ID, file,
                        Map.of("category", "COMMENT_IMAGE", "description", "Comment messaging image"))
                .andExpect(status().isBadRequest());

        verify(commentImageMediaService).uploadCommentImage(any(MediaUploadRequest.class), eq(COMMENT_ID), eq(POST_ID));
    }

    @Test
    @DisplayName("Загрузка изображения комментария - обязательная категория отсутствует")
    void uploadCommentImage_whenCategoryMissing_shouldReturnBadRequest() throws Exception {
        final MockMultipartFile file = TestDataFactory.createCommentImageFile(
                "comment-image.jpg", MediaType.IMAGE_JPEG_VALUE, "image-messaging".getBytes());

        mockMvcUtils.performMultipart("/media/comment-images/" + COMMENT_ID + "/post/" + POST_ID, file, Map.of())
                .andExpect(status().isBadRequest());

        verify(commentImageMediaService, never()).uploadCommentImage(any(MediaUploadRequest.class), any(), any());
    }

    @Test
    @DisplayName("Загрузка изображения комментария - слишком большой файл")
    void uploadCommentImage_whenFileTooLarge_shouldReturnBadRequest() throws Exception {
        final MockMultipartFile file = TestDataFactory.createCommentImageFile(
                "large-image.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[20 * 1024 * 1024]);

        when(commentImageMediaService.uploadCommentImage(any(MediaUploadRequest.class), eq(COMMENT_ID), eq(POST_ID)))
                .thenThrow(new InvalidFileException("Размер файла превышает допустимый лимит"));

        mockMvcUtils.performMultipart("/media/comment-images/" + COMMENT_ID + "/post/" + POST_ID, file,
                        Map.of("category", "COMMENT_IMAGE", "description", "Large comment image"))
                .andExpect(status().isBadRequest());

        verify(commentImageMediaService).uploadCommentImage(any(MediaUploadRequest.class), eq(COMMENT_ID), eq(POST_ID));
    }

    @Test
    @DisplayName("Загрузка изображения комментария - ошибка валидации файла")
    void uploadCommentImage_whenFileValidationFails_shouldReturnBadRequest() throws Exception {
        final MockMultipartFile file = TestDataFactory.createCommentImageFile(
                "corrupted-image.jpg", MediaType.IMAGE_JPEG_VALUE, "corrupted".getBytes());

        when(commentImageMediaService.uploadCommentImage(any(MediaUploadRequest.class), eq(COMMENT_ID), eq(POST_ID)))
                .thenThrow(new InvalidFileException("Файл поврежден или имеет неверный формат"));

        mockMvcUtils.performMultipart("/media/comment-images/" + COMMENT_ID + "/post/" + POST_ID, file,
                        Map.of("category", "COMMENT_IMAGE", "description", "Corrupted image"))
                .andExpect(status().isBadRequest());

        verify(commentImageMediaService).uploadCommentImage(any(MediaUploadRequest.class), eq(COMMENT_ID), eq(POST_ID));
    }

    @Test
    @DisplayName("Загрузка изображения комментария - успешно с PNG форматом")
    void uploadCommentImage_whenPngFormat_shouldReturnCreated() throws Exception {
        final MockMultipartFile file = TestDataFactory.createCommentImageFile(
                "comment-image.png", MediaType.IMAGE_PNG_VALUE, "png-messaging".getBytes());
        final MediaResponse response = TestDataFactory.createCommentImageResponse(MEDIA_ID, OWNER_ID);

        when(commentImageMediaService.uploadCommentImage(any(MediaUploadRequest.class), eq(COMMENT_ID), eq(POST_ID)))
                .thenReturn(response);

        mockMvcUtils.performMultipart("/media/comment-images/" + COMMENT_ID + "/post/" + POST_ID, file,
                        Map.of("category", "COMMENT_IMAGE", "description", "PNG comment image"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(MEDIA_ID.toString()))
                .andExpect(jsonPath("$.publicUrl").value("http://localhost/media/comment-image.jpg"))
                .andExpect(jsonPath("$.category").value("COMMENT_IMAGE"));

        verify(commentImageMediaService).uploadCommentImage(any(MediaUploadRequest.class), eq(COMMENT_ID), eq(POST_ID));
    }

    @Test
    @DisplayName("Загрузка изображения комментария - успешно с WEBP форматом")
    void uploadCommentImage_whenWebpFormat_shouldReturnCreated() throws Exception {
        final MockMultipartFile file = TestDataFactory.createCommentImageFile(
                "comment-image.webp", "image/webp", "webp-messaging".getBytes());
        final MediaResponse response = TestDataFactory.createCommentImageResponse(MEDIA_ID, OWNER_ID);

        when(commentImageMediaService.uploadCommentImage(any(MediaUploadRequest.class), eq(COMMENT_ID), eq(POST_ID)))
                .thenReturn(response);

        mockMvcUtils.performMultipart("/media/comment-images/" + COMMENT_ID + "/post/" + POST_ID, file,
                        Map.of("category", "COMMENT_IMAGE", "description", "WEBP comment image"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(MEDIA_ID.toString()))
                .andExpect(jsonPath("$.publicUrl").value("http://localhost/media/comment-image.jpg"))
                .andExpect(jsonPath("$.category").value("COMMENT_IMAGE"));

        verify(commentImageMediaService).uploadCommentImage(any(MediaUploadRequest.class), eq(COMMENT_ID), eq(POST_ID));
    }
}