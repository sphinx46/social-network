package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.file.FileProcessingException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.file.FileEmptyException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.file.FileOversizeException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.file.FileUnsupportedFormat;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.storage.FileStorageService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging.CentralLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private final CentralLogger centralLogger;

    public FileStorageServiceImpl(CentralLogger centralLogger) {
        this.centralLogger = centralLogger;
    }

    /**
     * Сохраняет файл в указанную поддиректорию
     *
     * @param file файл для сохранения
     * @param subDirectory поддиректория для сохранения
     * @return URL сохраненного файла
     */
    @Override
    public String saveFile(MultipartFile file, String subDirectory) {
        Map<String, Object> context = new HashMap<>();
        context.put("originalFilename", file.getOriginalFilename());
        context.put("fileSize", file.getSize());
        context.put("contentType", file.getContentType());
        context.put("subDirectory", subDirectory);

        centralLogger.logInfo("ФАЙЛ_СОХРАНЕНИЕ",
                "Сохранение файла", context);

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

            String fileUrl = "/uploads/" + subDirectory + "/" + uniqueFilename;

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("uniqueFilename", uniqueFilename);
            successContext.put("filePath", filePath.toString());
            successContext.put("fileUrl", fileUrl);

            centralLogger.logInfo("ФАЙЛ_СОХРАНЕН",
                    "Файл успешно сохранен", successContext);

            return fileUrl;
        } catch (IOException e) {
            centralLogger.logError("ФАЙЛ_ОШИБКА_СОХРАНЕНИЯ",
                    "Ошибка при сохранении файла", context, e);
            throw new FileProcessingException(ResponseMessageConstants.FAILURE_FILE_SAVE, e);
        }
    }

    /**
     * Удаляет файл по его URL
     *
     * @param fileUrl URL файла для удаления
     * @return true если файл успешно удален, false если файл не найден
     */
    @Override
    public boolean deleteFile(String fileUrl) {
        Map<String, Object> context = new HashMap<>();
        context.put("fileUrl", fileUrl);

        centralLogger.logInfo("ФАЙЛ_УДАЛЕНИЕ",
                "Удаление файла", context);

        try {
            if (fileUrl == null || fileUrl.isEmpty()) {
                centralLogger.logInfo("ФАЙЛ_НЕ_НАЙДЕН_УДАЛЕНИЕ",
                        "Файл не найден для удаления", context);
                return false;
            }

            String relativePath = fileUrl.replaceFirst("^/uploads/", "");

            Path filePath = Paths.get(uploadDir).resolve(relativePath).normalize();
            if (!filePath.startsWith(Paths.get(uploadDir).normalize())) {
                Map<String, Object> errorContext = new HashMap<>(context);
                errorContext.put("filePath", filePath.toString());

                centralLogger.logError("ФАЙЛ_НЕКОРРЕКТНЫЙ_ПУТЬ",
                        "Некорректный путь к файлу", errorContext, new FileProcessingException(ResponseMessageConstants.FAILURE_INCORRECT_PATH_TO_FILE));
                throw new FileProcessingException(ResponseMessageConstants.FAILURE_INCORRECT_PATH_TO_FILE);
            }

            if (Files.exists(filePath)) {
                Files.delete(filePath);

                Map<String, Object> successContext = new HashMap<>(context);
                successContext.put("filePath", filePath.toString());

                centralLogger.logInfo("ФАЙЛ_УДАЛЕН",
                        "Файл успешно удален", successContext);
                return true;
            }

            centralLogger.logInfo("ФАЙЛ_НЕ_СУЩЕСТВУЕТ",
                    "Файл не существует", context);
            return false;
        } catch (IOException e) {
            centralLogger.logError("ФАЙЛ_ОШИБКА_УДАЛЕНИЯ",
                    "Ошибка при удалении файла", context, e);
            return false;
        }
    }

    /**
     * Получает расширение файла из имени
     *
     * @param filename имя файла
     * @return расширение файла
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * Проверяет валидность изображения
     *
     * @param file файл изображения для проверки
     */
    @Override
    public void validateImageFile(MultipartFile file) {
        Map<String, Object> context = new HashMap<>();
        context.put("originalFilename", file.getOriginalFilename());
        context.put("fileSize", file.getSize());
        context.put("contentType", file.getContentType());

        centralLogger.logInfo("ИЗОБРАЖЕНИЕ_ВАЛИДАЦИЯ",
                "Валидация изображения", context);

        try {
            if (file.isEmpty()) {
                centralLogger.logError("ИЗОБРАЖЕНИЕ_ПУСТОЕ",
                        "Изображение пустое", context, new FileEmptyException(ResponseMessageConstants.FAILURE_FILE_EMPTY));
                throw new FileEmptyException(ResponseMessageConstants.FAILURE_FILE_EMPTY);
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                Map<String, Object> errorContext = new HashMap<>(context);
                errorContext.put("maxSize", "5MB");
                errorContext.put("actualSize", file.getSize());

                centralLogger.logError("ИЗОБРАЖЕНИЕ_ПРЕВЫШЕН_РАЗМЕР",
                        "Превышен размер изображения", errorContext, new FileOversizeException(ResponseMessageConstants.FAILURE_FILE_OVERSIZE));
                throw new FileOversizeException(ResponseMessageConstants.FAILURE_FILE_OVERSIZE);
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                centralLogger.logError("ИЗОБРАЖЕНИЕ_НЕВЕРНЫЙ_ТИП",
                        "Неверный тип изображения", context, new FileUnsupportedFormat(ResponseMessageConstants.FAILURE_FILE_MUST_BE_IMAGE));
                throw new FileUnsupportedFormat(ResponseMessageConstants.FAILURE_FILE_MUST_BE_IMAGE);
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null) {
                String extension = getFileExtension(originalFilename).toLowerCase();
                if (!extension.matches("\\.(jpg|jpeg|png|gif|bmp)$")) {
                    Map<String, Object> errorContext = new HashMap<>(context);
                    errorContext.put("fileExtension", extension);

                    centralLogger.logError("ИЗОБРАЖЕНИЕ_НЕПОДДЕРЖИВАЕМЫЙ_ФОРМАТ",
                            "Неподдерживаемый формат изображения", errorContext, new FileUnsupportedFormat(ResponseMessageConstants.FAULURE_FILE_UNSUPPORTED_FORMAT));
                    throw new FileUnsupportedFormat(ResponseMessageConstants.FAULURE_FILE_UNSUPPORTED_FORMAT);
                }
            }

            centralLogger.logInfo("ИЗОБРАЖЕНИЕ_ВАЛИДАЦИЯ_УСПЕШНА",
                    "Валидация изображения успешно пройдена", context);
        } catch (Exception e) {
            centralLogger.logError("ИЗОБРАЖЕНИЕ_ОШИБКА_ВАЛИДАЦИИ",
                    "Ошибка при валидации изображения", context, e);
            throw e;
        }
    }

    /**
     * Сохраняет изображение для поста
     *
     * @param file файл изображения
     * @param postId идентификатор поста
     * @return URL сохраненного изображения
     */
    @Override
    public String savePostImage(MultipartFile file, Long postId) {
        Map<String, Object> context = new HashMap<>();
        context.put("postId", postId);
        context.put("originalFilename", file.getOriginalFilename());
        context.put("fileSize", file.getSize());

        centralLogger.logInfo("ИЗОБРАЖЕНИЕ_ПОСТА_СОХРАНЕНИЕ",
                "Сохранение изображения для поста", context);

        try {
            String subDirectory = "post-images/" + postId;
            String fileUrl = saveFile(file, subDirectory);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("fileUrl", fileUrl);

            centralLogger.logInfo("ИЗОБРАЖЕНИЕ_ПОСТА_СОХРАНЕНО",
                    "Изображение для поста успешно сохранено", successContext);

            return fileUrl;
        } catch (Exception e) {
            centralLogger.logError("ИЗОБРАЖЕНИЕ_ПОСТА_ОШИБКА_СОХРАНЕНИЯ",
                    "Ошибка при сохранении изображения для поста", context, e);
            throw e;
        }
    }

    /**
     * Сохраняет аватар пользователя
     *
     * @param file файл аватара
     * @param userId идентификатор пользователя
     * @return URL сохраненного аватара
     */
    @Override
    public String saveAvatar(MultipartFile file, Long userId) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("originalFilename", file.getOriginalFilename());
        context.put("fileSize", file.getSize());

        centralLogger.logInfo("АВАТАР_СОХРАНЕНИЕ",
                "Сохранение аватара пользователя", context);

        try {
            String subDirectory = "avatars/" + userId;
            String fileUrl = saveFile(file, subDirectory);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("fileUrl", fileUrl);

            centralLogger.logInfo("АВАТАР_СОХРАНЕН",
                    "Аватар пользователя успешно сохранен", successContext);

            return fileUrl;
        } catch (Exception e) {
            centralLogger.logError("АВАТАР_ОШИБКА_СОХРАНЕНИЯ",
                    "Ошибка при сохранении аватара пользователя", context, e);
            throw e;
        }
    }
}