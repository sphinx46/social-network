package ru.cs.vsu.social_network.user_profile_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на загрузку аватарки в профиль")
public class ProfileUploadAvatarRequest {
    private static final int MAX_URL_LENGTH = 2048;

    /**
     * Публичная ссылка на аватарку.
     */
    @NotBlank(message = "Ссылка на аватарку обязательна")
    @Size(max = MAX_URL_LENGTH, message = "Ссылка на аватарку слишком длинная")
    @Schema(description = "Ссылка на аватарку профиля")
    private String publicUrl;
}
