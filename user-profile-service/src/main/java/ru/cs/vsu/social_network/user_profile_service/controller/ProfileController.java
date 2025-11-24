package ru.cs.vsu.social_network.user_profile_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.cs.vsu.social_network.user_profile_service.dto.request.ProfileEditRequest;
import ru.cs.vsu.social_network.user_profile_service.dto.request.ProfileUploadAvatarRequest;
import ru.cs.vsu.social_network.user_profile_service.dto.response.ProfileResponse;
import ru.cs.vsu.social_network.user_profile_service.service.ProfileService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    /**
     * Создает профиль пользователя с настройками по умолчанию.
     *
     * @param keycloakUserId идентификатор пользователя из Keycloak (из заголовка X-User-Id)
     * @param username имя пользователя (из заголовка X-Username)
     * @return созданный профиль
     */
    @Operation(summary = "Создание профиля по умолчанию")
    @PostMapping("/me")
    public ResponseEntity<ProfileResponse> createDefaultProfile(
            @RequestHeader("X-User-Id") final UUID keycloakUserId,
            @RequestHeader("X-Username") final String username) {
        log.info("ПРОФИЛЬ_КОНТРОЛЛЕР_СОЗДАНИЕ_НАЧАЛО: "
                + "создание профиля по умолчанию для keycloakUserId: {}, "
                + "username: {}", keycloakUserId, username);
        ProfileResponse response = profileService.createDefaultProfile(
                keycloakUserId, username);
        log.info("ПРОФИЛЬ_КОНТРОЛЛЕР_СОЗДАНИЕ_УСПЕХ: "
                + "профиль создан для keycloakUserId: {}", keycloakUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * Получает профиль текущего аутентифицированного пользователя.
     *
     * @param keycloakUserId идентификатор пользователя из Keycloak
     *                       (из заголовка X-User-Id)
     * @return профиль пользователя
     */
    @Operation(summary = "Получение профиля текущего пользователя")
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getCurrentUserProfile(
            @RequestHeader("X-User-Id") final UUID keycloakUserId) {
        log.info("ПРОФИЛЬ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_НАЧАЛО: "
                + "получение текущего профиля для keycloakUserId: {}",
                keycloakUserId);
        ProfileResponse response = profileService.getProfileByUserId(
                keycloakUserId);
        log.info("ПРОФИЛЬ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_УСПЕХ: "
                + "профиль получен для keycloakUserId: {}", keycloakUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * Редактирует профиль текущего аутентифицированного пользователя.
     *
     * @param request данные для обновления профиля
     * @param keycloakUserId идентификатор пользователя из Keycloak
     *                       (из заголовка X-User-Id)
     * @return обновленный профиль
     */
    @Operation(summary = "Редактирование профиля")
    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> editProfile(
            @Valid @RequestBody final ProfileEditRequest request,
            @RequestHeader("X-User-Id") final UUID keycloakUserId) {
        log.info("ПРОФИЛЬ_КОНТРОЛЛЕР_РЕДАКТИРОВАНИЕ_НАЧАЛО: "
                + "редактирование профиля для keycloakUserId: {}",
                keycloakUserId);
        ProfileResponse response = profileService.editProfile(
                keycloakUserId, request);
        log.info("ПРОФИЛЬ_КОНТРОЛЛЕР_РЕДАКТИРОВАНИЕ_УСПЕХ: "
                + "профиль отредактирован для keycloakUserId: {}",
                keycloakUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * Получает профиль пользователя по его Keycloak UUID
     * (публичный эндпоинт).
     *
     * @param keycloakUserId идентификатор пользователя из Keycloak
     * @return профиль пользователя
     */
    @Operation(summary = "Получить профиль пользователя по keycloakUserId")
    @GetMapping("/{keycloakUserId}")
    public ResponseEntity<ProfileResponse> getProfileById(
            @PathVariable("keycloakUserId") final UUID keycloakUserId) {
        log.info("ПРОФИЛЬ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_ПО_ID_НАЧАЛО: "
                + "получение профиля по keycloakUserId: {}", keycloakUserId);
        ProfileResponse response = profileService.getProfileByUserId(
                keycloakUserId);
        log.info("ПРОФИЛЬ_КОНТРОЛЛЕР_ПОЛУЧЕНИЕ_ПО_ID_УСПЕХ: "
                + "профиль получен для keycloakUserId: {}", keycloakUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * Загружает URL аватара для текущего аутентифицированного пользователя.
     * Используется upload-service для обновления ссылки на аватар после загрузки файла.
     *
     * @param keycloakUserId идентификатор текущего пользователя из Keycloak (из заголовка)
     *  @param request данные для загрузки аватарки
     * @return обновленный профиль пользователя с новым URL аватара
     */
    @Operation(summary = "Загрузить аватарку для текущего пользователя")
    @PostMapping("/me/avatar")
    public ResponseEntity<ProfileResponse> uploadAvatar(
            @RequestHeader("X-User-Id") final UUID keycloakUserId,
            @RequestBody final ProfileUploadAvatarRequest request) {
        log.info("ПРОФИЛЬ_КОНТРОЛЛЕР_АВАТАР_ЗАГРУЗКА_НАЧАЛО: "
                        + "начало загрузки аватара для пользователя {}, "
                        + "длина URL: {} символов",
                keycloakUserId, request.getPublicUrl().length());

        ProfileResponse response = profileService.uploadAvatar(keycloakUserId, request);

        log.info("ПРОФИЛЬ_КОНТРОЛЛЕР_АВАТАР_ЗАГРУЗКА_УСПЕХ: "
                + "аватар успешно загружен для пользователя {}, "
                + "новый URL установлен", keycloakUserId);

        return ResponseEntity.ok(response);
    }
}
