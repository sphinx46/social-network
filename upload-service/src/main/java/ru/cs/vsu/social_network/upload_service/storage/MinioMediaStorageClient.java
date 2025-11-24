package ru.cs.vsu.social_network.upload_service.storage;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.cs.vsu.social_network.upload_service.config.properties.MinioProperties;
import ru.cs.vsu.social_network.upload_service.exception.MinioOperationException;
import ru.cs.vsu.social_network.upload_service.utils.MessageConstants;

import java.io.InputStream;

/**
 * Реализация {@link MediaStorageClient} для MinIO.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public final class MinioMediaStorageClient implements MediaStorageClient {

    private static final long UNKNOWN_PART_SIZE = -1L;

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    /**
     * {@inheritDoc}
     */
    @Override
    public void upload(final String objectName, final MultipartFile file) {
        try (InputStream stream = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .contentType(file.getContentType())
                    .stream(stream, file.getSize(), UNKNOWN_PART_SIZE)
                    .build());
            log.debug("MINIO_STORAGE_UPLOAD: objectName={}", objectName);
        } catch (Exception e) {
            log.error("MINIO_STORAGE_UPLOAD_ERROR: objectName={}", objectName, e);
            throw new MinioOperationException(MessageConstants.MEDIA_UPLOAD_EXCEPTION, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream download(final String objectName) {
        try {
            log.debug("MINIO_STORAGE_DOWNLOAD: objectName={}", objectName);
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioProperties.getBucketName())
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            log.error("MINIO_STORAGE_DOWNLOAD_ERROR: objectName={}", objectName, e);
            throw new MinioOperationException(MessageConstants.FILE_DOWNLOAD_EXCEPTION, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .build());
            log.debug("MINIO_STORAGE_DELETE: objectName={}", objectName);
        } catch (Exception e) {
            log.error("MINIO_STORAGE_DELETE_ERROR: objectName={}", objectName, e);
            throw new MinioOperationException(MessageConstants.FILE_DELETE_EXCEPTION, e);
        }
    }
}

