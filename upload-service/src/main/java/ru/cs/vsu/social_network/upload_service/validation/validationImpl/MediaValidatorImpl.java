package ru.cs.vsu.social_network.upload_service.validation.validationImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.cs.vsu.social_network.upload_service.exception.InvalidFileException;
import ru.cs.vsu.social_network.upload_service.utils.MessageConstants;
import ru.cs.vsu.social_network.upload_service.validation.MediaValidator;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public final class MediaValidatorImpl implements MediaValidator {

    private static final long MAX_FILE_SIZE = 15 * 1024 * 1024;
    private static final List<String> ALLOWED_MIME_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "video/mp4", "video/quicktime"
    );

    /**
     * Проверяет валидность переданного файла.
     *
     * @param file загружаемый файл
     */
    @Override
    public void validateFile(final MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("МЕДИА_ВАЛИДАЦИЯ_ФАЙЛ_ОШИБКА: файл пустой");
            throw new InvalidFileException(MessageConstants.FILE_INVALID_EXCEPTION);
        }
        if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            log.warn("МЕДИА_ВАЛИДАЦИЯ_ФАЙЛ_ОШИБКА: отсутствует оригинальное имя файла");
            throw new InvalidFileException(MessageConstants.FILE_INVALID_EXCEPTION);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("МЕДИА_ВАЛИДАЦИЯ_ФАЙЛ_ОШИБКА: превышен размер {} байт", file.getSize());
            throw new InvalidFileException(MessageConstants.FILE_INVALID_EXCEPTION);
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            log.warn("МЕДИА_ВАЛИДАЦИЯ_ФАЙЛ_ОШИБКА: mime type {} не поддерживается", contentType);
            throw new InvalidFileException(MessageConstants.FILE_INVALID_EXCEPTION);
        }
    }
}