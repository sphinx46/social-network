package ru.cs.vsu.social_network.upload_service.utils;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.UUID;

@Component
public class GeneratorObjectName {

    /**
     * Формирует уникальное имя объекта в MinIO.
     *
     * @param file     загружаемый файл
     * @param category категория медиа
     * @return уникальное имя объекта
     */
    public String generateObjectName(final MultipartFile file, final String category) {
        String sanitizedCategory = normalizeCategory(category);
        String extension = extractExtension(file.getOriginalFilename());
        return sanitizedCategory + "/" + UUID.randomUUID() + extension;
    }

    private String normalizeCategory(final String category) {
        String value = StringUtils.hasText(category) ? category : "common";
        return value.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9-_]", "-");
    }

    private String extractExtension(final String originalName) {
        if (!StringUtils.hasText(originalName) || !originalName.contains(".")) {
            return "";
        }
        String ext = originalName.substring(originalName.lastIndexOf('.'));
        return ext.toLowerCase(Locale.ROOT);
    }
}
