package ru.cs.vsu.social_network.upload_service.storage;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import ru.cs.vsu.social_network.upload_service.config.properties.MinioProperties;
import ru.cs.vsu.social_network.upload_service.exception.MinioOperationException;
import ru.cs.vsu.social_network.upload_service.utils.MessageConstants;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioMediaStorageClientTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private MinioProperties minioProperties;

    @InjectMocks
    private MinioMediaStorageClient storageClient;

    @Test
    @DisplayName("Загрузка файла - успешно")
    void upload_whenRequestIsValid() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", MediaType.IMAGE_PNG_VALUE, "test".getBytes());

        when(minioProperties.getBucketName()).thenReturn("test-bucket");

        storageClient.upload("test-object", file);

        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("Скачивание файла - успешно")
    void download_whenRequestIsValid() throws Exception {
        GetObjectResponse response = mock(GetObjectResponse.class);

        when(minioProperties.getBucketName()).thenReturn("test-bucket");
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(response);

        assertNotNull(storageClient.download("test-object"));
        verify(minioClient).getObject(any(GetObjectArgs.class));
    }

    @Test
    @DisplayName("Удаление файла - успешно")
    void delete_whenRequestIsValid() throws Exception {
        when(minioProperties.getBucketName()).thenReturn("test-bucket");

        storageClient.delete("test-object");

        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    @DisplayName("Загрузка файла - ошибка MinIO")
    void upload_whenMinioFails() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", "test".getBytes());

        when(minioProperties.getBucketName()).thenReturn("test-bucket");
        RuntimeException minioError = new RuntimeException("MinIO error");
        when(minioClient.putObject(any(PutObjectArgs.class))).thenThrow(minioError);

        MinioOperationException exception = assertThrows(MinioOperationException.class,
                () -> storageClient.upload("test-object", file));

        assertEquals(MessageConstants.MEDIA_UPLOAD_EXCEPTION, exception.getMessage());
        assertEquals(minioError, exception.getCause());
    }

    @Test
    @DisplayName("Скачивание файла - файл не найден в MinIO")
    void download_whenFileNotFound() throws Exception {
        when(minioProperties.getBucketName()).thenReturn("test-bucket");
        RuntimeException minioError = new RuntimeException("Object not found");
        when(minioClient.getObject(any(GetObjectArgs.class))).thenThrow(minioError);

        MinioOperationException exception = assertThrows(MinioOperationException.class,
                () -> storageClient.download("non-existent-object"));

        assertEquals(MessageConstants.FILE_DOWNLOAD_EXCEPTION, exception.getMessage());
        assertEquals(minioError, exception.getCause());
    }

    @Test
    @DisplayName("Удаление файла - ошибка при удалении")
    void delete_whenMinioFails() throws Exception {
        when(minioProperties.getBucketName()).thenReturn("test-bucket");
        RuntimeException minioError = new RuntimeException("Delete error");
        doThrow(minioError).when(minioClient).removeObject(any(RemoveObjectArgs.class));

        MinioOperationException exception = assertThrows(MinioOperationException.class,
                () -> storageClient.delete("test-object"));

        assertEquals(MessageConstants.FILE_DELETE_EXCEPTION, exception.getMessage());
        assertEquals(minioError, exception.getCause());
    }
}