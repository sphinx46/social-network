package ru.cs.vsu.social_network.user_profile_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Builder
@Data
@Schema(description = "Запрос на загрузку аватарки в профиль")
public class ProfileUploadAvatarRequest {
    @Schema(description = "Аватарка профиля")
    private MultipartFile imageFile;
}
