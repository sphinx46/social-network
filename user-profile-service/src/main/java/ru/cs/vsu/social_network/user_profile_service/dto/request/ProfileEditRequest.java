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
    public ProfileEditRequest() {
    }
    
    public ProfileEditRequest(String bio, LocalDate dateOfBirth, String city) {
        this.bio = bio;
        this.dateOfBirth = dateOfBirth;
        this.city = city;
    }
    
    @Schema(description = "Описание",
            example = "О себе: увлекаюсь программированием, люблю танцевать!")
    @Size(max = 500, message = "Длина описания не более 500 символов")
    private String bio;

    @Schema(description = "Дата рождения", example = "2000-01-01")
    private LocalDate dateOfBirth;

    @Schema(description = "Город проживания", example = "Москва")
    private String city;
}
