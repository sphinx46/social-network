package ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "Ответ с данными профиля")
public class ProfileResponse {
    @Schema(description = "Имя пользователя", example = "john_doe")
    private String username;

    @Schema(description = "Город", example = "Москва")
    private String city;

    @Schema(description = "Описание", example = "Увлекаюсь программированием")
    private String bio;

    @Schema(description = "Аватар профиля", example = "/uploads/avatar.jpg")
    private String imageUrl;

    @Schema(description = "Дата рождения", example = "2000-01-01")
    private LocalDate dateOfBirth;

    @Schema(description = "Дата создания профиля", example = "2024-01-15")
    private LocalDateTime createdAt;

    @Schema(description = "Возраст пользователя", example = "24")
    private Integer age;

    @Schema(description = "Статус онлайн", example = "true")
    private Boolean isOnline;
}