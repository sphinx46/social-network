package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.custom.FileProcessingException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public String saveFile(MultipartFile file, String subDirectory) {
        try {
            Path uploadPath = Paths.get(uploadDir, subDirectory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID() + fileExtension;

            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath);

            log.info("Файл сохранен: {}", filePath);
            return "/uploads/" + subDirectory + "/" + uniqueFilename;
        } catch (IOException e) {
            throw new FileProcessingException("Ошибка при сохранении файла", e);
        }
    }

    public boolean deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isEmpty()) {
                return false;
            }

            String relativePath = fileUrl.replaceFirst("^/uploads/", "");

            Path filePath = Paths.get(uploadDir).resolve(relativePath).normalize();
            if (!filePath.startsWith(Paths.get(uploadDir).normalize())) {
                throw new SecurityException("Некорректный путь к файлу.");
            }

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Файл удален: {}", filePath);
                return true;
            }
            return false;
        } catch (IOException e) {
            log.error("Ошибка при удалении файла: {}", fileUrl, e);
            return false;
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    public void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Файл не может быть пустым");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Размер файла не должен превышать 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Файл должен быть изображением");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = getFileExtension(originalFilename).toLowerCase();
            if (!extension.matches("\\.(jpg|jpeg|png|gif|bmp)$")) {
                throw new IllegalArgumentException("Поддерживаются только JPG, JPEG, PNG, GIF, BMP файлы");
            }
        }
    }
}