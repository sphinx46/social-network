package ru.cs.vsu.social_network.user_profile_service.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с данными профиля")
public class ProfileResponse {
    @Schema(description = "Имя пользователя", example = "john_doe")
    private String username;

    @Schema(description = "Город", example = "Москва")
    private String city;

    @Schema(description = "Описание", example = "Увлекаюсь программированием")
    private String bio;

    @Schema(description = "Аватар профиля", example = "/uploads/avatar.jpg")
    private String avatarUrl;

    @Schema(description = "Дата рождения", example = "2000-01-01")
    private LocalDate dateOfBirth;

    @Schema(description = "Дата создания профиля", example = "2024-01-15")
    private LocalDateTime createdAt;

    @Schema(description = "Дата обновления профиля", example = "2024-01-16")
    private LocalDateTime updatedAt;

    @Schema(description = "Возраст пользователя", example = "24")
    private Integer age;

    @Schema(description = "Статус онлайн", example = "true")
    private Boolean isOnline;
}