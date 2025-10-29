package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.ProfileRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.ProfileResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.ProfileService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.UserService;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService service;
    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение профиля текущего пользователя")
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getCurrentProfile() {
        User user = userService.getCurrentUser();
        log.info("Пользователь {} запрашивает свой профиль", user.getId());
        ProfileResponse response = service.getProfileByUser(user);
        log.info("Профиль пользователя {} успешно получен", user.getId());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Редактирование профиля текущего пользователя")
    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> editProfile(@Valid @RequestBody ProfileRequest request) {
        User user = userService.getCurrentUser();
        log.info("Пользователь {} редактирует свой профиль", user.getId());
        ProfileResponse response = service.updateProfile(user, request);
        log.info("Профиль пользователя {} успешно отредактирован", user.getId());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Загрузка аватарки текущему пользователю")
    @PostMapping(value = "/me/avatar")
    public ResponseEntity<ProfileResponse> uploadAvatar(@RequestParam("file") MultipartFile imageFile) {
        User user = userService.getCurrentUser();
        log.info("Пользователь {} загружает аватар", user.getId());
        ProfileResponse response = service.uploadAvatar(user, imageFile);
        log.info("Аватар пользователя {} успешно загружен", user.getId());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("permitAll()")
    @Operation(summary = "Получения профиля по Id пользователя")
    @GetMapping("/{userId}")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable Long userId) {
        log.info("Запрос на получение профиля пользователя {}", userId);
        ProfileResponse response = service.getProfileByUserId(userId);
        log.info("Профиль пользователя {} успешно получен", userId);
        return ResponseEntity.ok(response);
    }
}