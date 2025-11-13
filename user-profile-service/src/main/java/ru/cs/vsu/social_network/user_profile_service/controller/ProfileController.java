package ru.cs.vsu.social_network.user_profile_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.cs.vsu.social_network.user_profile_service.dto.request.ProfileEditRequest;
import ru.cs.vsu.social_network.user_profile_service.dto.response.ProfileResponse;
import ru.cs.vsu.social_network.user_profile_service.service.ProfileService;

import java.util.UUID;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получение профиля текущего пользователя")
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getCurrentUserProfile(@RequestHeader("X-User-Id") UUID keycloakUserId) {
        ProfileResponse response = profileService.getProfileByUserId(keycloakUserId);
        return ResponseEntity.ok(response);
    }

    /// temp
    @PostMapping("/me")
    @Operation(summary = "Создание профиля с настройками по умолчанию")
    public ResponseEntity<ProfileResponse> createDefaultProfile(
            @RequestHeader("X-User-Id") UUID keycloakUserId,
            @RequestHeader("X-Username") String username) {

        ProfileResponse response = profileService.createDefaultProfile(keycloakUserId, username);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Редактирование профиля текущего пользователя")
    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> editProfile(@Valid @RequestBody ProfileEditRequest request,
                                                       @RequestHeader("X-User-Id") UUID keycloakUserId) {
        ProfileResponse response = profileService.editProfile(keycloakUserId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получение профиля пользователя по Keycloak ID")
    @GetMapping("/{keycloakUserId}")
    public ResponseEntity<ProfileResponse> getProfileById(@PathVariable UUID keycloakUserId) {
        return ResponseEntity.ok(profileService.getProfileByUserId(keycloakUserId));
    }
}