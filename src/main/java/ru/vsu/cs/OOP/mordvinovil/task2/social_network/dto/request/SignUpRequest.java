package ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Запрос на регистрацию")
public class SignUpRequest {

    @Schema(description = "Имя пользователя", example = "Jon")
    @Size(min = 5, max = 50, message = "Имя пользователя должно содержать от 5 до 50 символов")
    @NotBlank(message = "Имя пользователя не может быть пустыми")
    private String username;

    @Schema(description = "Email", example = "jon@example.com")
    @Size(max = 50, message = "Email должен содержать до 50 символов")
    @NotBlank(message = "Email не может быть пустым")
    private String email;

    @Schema(description = "Город", example = "Москва")
    @Size(max = 50, message = "Название города должно содержать до 50 символов")
    @NotBlank(message = "Город не может быть пустым")
    private String city;

    @Schema(description = "Пароль", example = "my_1secret1_password")
    @Size(min = 8, max = 255, message = "Длина пароля должна быть от 8 до 255 символов")
    @NotBlank(message = "Пароль не может быть пустыми")
    private String password;
}