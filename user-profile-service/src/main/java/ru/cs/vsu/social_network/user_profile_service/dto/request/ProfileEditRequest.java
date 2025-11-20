package ru.cs.vsu.social_network.user_profile_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Builder
@Data
@Schema(description = "Запрос на редактирование профиля")
public class ProfileEditRequest {
    private static final int MAX_BIO_LENGTH = 500;
    
    @Schema(description = "Описание",
            example = "О себе: увлекаюсь программированием, люблю танцевать!")
    @Size(max = MAX_BIO_LENGTH,
            message = "Длина описания не более 500 символов")
    private String bio;

    @Schema(description = "Дата рождения", example = "2000-01-01")
    private LocalDate dateOfBirth;

    @Schema(description = "Город проживания", example = "Москва")
    private String city;
}
