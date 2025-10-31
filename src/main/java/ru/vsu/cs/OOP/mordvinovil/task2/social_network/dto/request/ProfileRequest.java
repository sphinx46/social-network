package ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Builder
@Data
@Schema(description = "Запрос на редактирование профиля")
public class ProfileRequest {
    @Schema(description = "Описание", example = "О себе: увлекаюсь программированием, люблю танцевать!")
    @Size(max = 500, message = "Длина описания не более 500 символов")
    private String bio;

    @Schema(description = "Дата рождения", example = "2000-01-01")
    private LocalDate dateOfBirth;

    @Schema(description = "Город проживания", example = "Москва")
    private String city;

    @Schema(description = "Аватар профиля")
    private String imageUrl;
}
