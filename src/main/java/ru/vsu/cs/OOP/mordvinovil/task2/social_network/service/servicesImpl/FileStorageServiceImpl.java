package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.file.FileProcessingException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.file.FileEmptyException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.file.FileOversizeException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.file.FileUnsupportedFormat;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.FileStorageService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
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
            throw new FileProcessingException(ResponseMessageConstants.FAILURE_FILE_SAVE, e);
        }
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isEmpty()) {
                return false;
            }

            String relativePath = fileUrl.replaceFirst("^/uploads/", "");

            Path filePath = Paths.get(uploadDir).resolve(relativePath).normalize();
            if (!filePath.startsWith(Paths.get(uploadDir).normalize())) {
                throw new SecurityException(ResponseMessageConstants.FAILURE_INCORRECT_PATH_TO_FILE);
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

    @Override
    public void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileEmptyException(ResponseMessageConstants.FAILURE_FILE_EMPTY);
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new FileOversizeException(ResponseMessageConstants.FAILURE_FILE_OVERSIZE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new FileUnsupportedFormat(ResponseMessageConstants.FAILURE_FILE_MUST_BE_IMAGE);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = getFileExtension(originalFilename).toLowerCase();
            if (!extension.matches("\\.(jpg|jpeg|png|gif|bmp)$")) {
                throw new FileUnsupportedFormat(ResponseMessageConstants.FAULURE_FILE_UNSUPPORTED_FORMAT);
            }
        }
    }

    @Override
    public String savePostImage(MultipartFile file, Long postId) {
        String subDirectory = "post-images/" + postId;
        return saveFile(file, subDirectory);
    }

    @Override
    public String saveAvatar(MultipartFile file, Long userId) {
        String subDirectory = "avatars/" + userId;
        return saveFile(file, subDirectory);
    }
}