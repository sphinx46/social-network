package ru.cs.vsu.social_network.upload_service.validation;

import org.springframework.web.multipart.MultipartFile;

/**
 * Валидатор загружаемых медиа-файлов.
 */
public interface MediaValidator {
    /**
     * Проверяет корректность файла перед загрузкой.
     *
     * @param file исходный файл
     */
    void validateFile(MultipartFile file);
}
