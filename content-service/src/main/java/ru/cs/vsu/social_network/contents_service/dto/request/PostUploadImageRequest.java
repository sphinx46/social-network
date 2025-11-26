package ru.cs.vsu.social_network.contents_service.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@Schema(description = "Запрос на загрузку изображения для поста")
public class PostUploadImageRequest {

    @NotBlank
    @Schema(description = "Фото для поста")
    private String imageUrl;
}

