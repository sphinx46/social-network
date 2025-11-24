package ru.cs.vsu.social_network.upload_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на загрузку медиа")
public class MediaUploadRequest {

    @Schema(description = "Файл, переданный для загрузки")
    @NotNull(message = "Файл для загрузки не может быть пустым.")
    private MultipartFile file;

    @Schema(description = "Категория медиа", example = "avatar")
    @NotBlank(message = "Категория должна быть заполнена.")
    private String category;

    @Schema(description = "Краткое описание медиа")
    @Size(max = 512, message = "Описание не должно превышать 512 символов.")
    private String description;
}
