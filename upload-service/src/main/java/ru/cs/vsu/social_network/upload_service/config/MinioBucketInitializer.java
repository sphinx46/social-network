package ru.cs.vsu.social_network.upload_service.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.upload_service.config.properties.MinioProperties;
import ru.cs.vsu.social_network.upload_service.exception.MinioOperationException;
import ru.cs.vsu.social_network.upload_service.utils.MessageConstants;

import jakarta.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor
public final class MinioBucketInitializer {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    /**
     * Создаёт хранилище в MinIO при необходимости.
     */
    @PostConstruct
    public void ensureBucket() {
        if (!minioProperties.isAutoCreateBucket()) {
            log.info("MINIO_ИНИЦИАЛИЗАЦИЯ: создание бакета отключено, пропуск");
            return;
        }
        try {
            String bucketName = minioProperties.getBucketName();
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("MINIO_ИНИЦИАЛИЗАЦИЯ: создан бакет {}", bucketName);
            } else {
                log.info("MINIO_ИНИЦИАЛИЗАЦИЯ: бакет {} уже существует", bucketName);
            }
        } catch (Exception e) {
            log.error("MINIO_ИНИЦИАЛИЗАЦИЯ_ОШИБКА: не удалось подготовить бакет", e);
            throw new MinioOperationException(MessageConstants.STORAGE_INIT_EXCEPTION, e);
        }
    }
}

