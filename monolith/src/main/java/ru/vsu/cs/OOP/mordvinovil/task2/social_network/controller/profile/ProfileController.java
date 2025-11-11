package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller.profile;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.profile.ProfileRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.profile.ProfileResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.user.ProfileService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.user.UserService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging.CentralLogger;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService service;
    private final UserService userService;
    private final CentralLogger centralLogger;

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение профиля текущего пользователя")
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getCurrentProfile() {
        Map<String, Object> context = new HashMap<>();

        centralLogger.logInfo("ПРОФИЛЬ_ТЕКУЩИЙ_ЗАПРОС",
                "Запрос профиля текущего пользователя", context);

        try {
            User user = userService.getCurrentUser();
            context.put("userId", user.getId());

            ProfileResponse response = service.getProfileByUser(user);

            centralLogger.logInfo("ПРОФИЛЬ_ТЕКУЩИЙ_ПОЛУЧЕН",
                    "Профиль текущего пользователя успешно получен", context);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("ПРОФИЛЬ_ТЕКУЩИЙ_ОШИБКА",
                    "Ошибка при получении профиля текущего пользователя", context, e);
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Редактирование профиля текущего пользователя")
    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> editProfile(@Valid @RequestBody ProfileRequest request) {
        Map<String, Object> context = new HashMap<>();

        centralLogger.logInfo("ПРОФИЛЬ_РЕДАКТИРОВАНИЕ_ЗАПРОС",
                "Запрос на редактирование профиля", context);

        try {
            User user = userService.getCurrentUser();
            context.put("userId", user.getId());

            ProfileResponse response = service.updateProfile(user, request);

            centralLogger.logInfo("ПРОФИЛЬ_ОТРЕДАКТИРОВАН",
                    "Профиль успешно отредактирован", context);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("ПРОФИЛЬ_ОШИБКА_РЕДАКТИРОВАНИЯ",
                    "Ошибка при редактировании профиля", context, e);
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Загрузка аватарки текущему пользователю")
    @PostMapping(value = "/me/avatar")
    public ResponseEntity<ProfileResponse> uploadAvatar(@RequestParam("file") MultipartFile imageFile) {
        Map<String, Object> context = new HashMap<>();
        context.put("fileName", imageFile.getOriginalFilename());
        context.put("fileSize", imageFile.getSize());

        centralLogger.logInfo("АВАТАР_ЗАГРУЗКА_ЗАПРОС",
                "Запрос на загрузку аватара", context);

        try {
            User user = userService.getCurrentUser();
            context.put("userId", user.getId());

            ProfileResponse response = service.uploadAvatar(user, imageFile);

            centralLogger.logInfo("АВАТАР_ЗАГРУЖЕН",
                    "Аватар успешно загружен", context);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("АВАТАР_ОШИБКА_ЗАГРУЗКИ",
                    "Ошибка при загрузке аватара", context, e);
            throw e;
        }
    }

    @PreAuthorize("permitAll()")
    @Operation(summary = "Получения профиля по Id пользователя")
    @GetMapping("/{userId}")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable Long userId) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);

        centralLogger.logInfo("ПРОФИЛЬ_ПОЛЬЗОВАТЕЛЯ_ЗАПРОС",
                "Запрос профиля пользователя по ID", context);

        try {
            ProfileResponse response = service.getProfileByUserId(userId);

            centralLogger.logInfo("ПРОФИЛЬ_ПОЛЬЗОВАТЕЛЯ_ПОЛУЧЕН",
                    "Профиль пользователя успешно получен", context);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            centralLogger.logError("ПРОФИЛЬ_ПОЛЬЗОВАТЕЛЯ_ОШИБКА",
                    "Ошибка при получении профиля пользователя", context, e);
            throw e;
        }
    }
}