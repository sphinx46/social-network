//package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller;
//
//import io.swagger.v3.oas.annotations.Operation;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.apache.tomcat.util.http.fileupload.FileUploadException;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.ProfileRequest;
//import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.response.ProfileResponse;
//import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
//import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.ProfileService;
//import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.exceptions.profile.ProfileNotFoundException;
//
//@RestController
//@RequestMapping("/profile")
//@RequiredArgsConstructor
//public class ProfileController {
//    private final ProfileService service;
//
//    @Operation(summary = "Получение профиля текущего пользователя")
//    @GetMapping("/me")
//    public ResponseEntity<ProfileResponse> getCurrentProfile(@AuthenticationPrincipal User user) {
//        ProfileResponse response = service.getProfileByUser(user);
//        return ResponseEntity.ok(response);
//    }
//
//    @Operation(summary = "Редактирование профиля текущего пользователя")
//    @PutMapping("/me")
//    public ResponseEntity<ProfileResponse> editProfile(@AuthenticationPrincipal User user,
//                                                       @Valid @RequestBody ProfileRequest request) {
//        ProfileResponse response = service.updateProfile(user, request);
//        return ResponseEntity.ok(response);
//    }
//
//    @Operation(summary = "Загрузка аватарки текущему пользователю")
//    @PostMapping("/me/avatar")
//    public ResponseEntity<ProfileResponse> uploadAvatar(@AuthenticationPrincipal User user,
//                                                        @RequestParam("file") MultipartFile imageFile) throws FileUploadException {
//        try {
//            ProfileResponse response = service.uploadAvatar(user, imageFile);
//            return ResponseEntity.ok(response);
//        } catch (FileUploadException e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }
//
//    @Operation(summary = "Получения профиля по Id пользователя")
//    @GetMapping("/{userId}")
//    public ResponseEntity<ProfileResponse> getProfile(@PathVariable Long userId) {
//        try {
//            ProfileResponse response = service.getProfileByUserId(userId);
//            return ResponseEntity.ok(response);
//        } catch (ProfileNotFoundException e) {
//            return ResponseEntity.notFound().build();
//        }
//    }
//}
package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.ProfileRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.ProfileResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.security.filters.UserDetailsImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.ProfileService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.UserService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.profile.ProfileNotFoundException;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService service;
    private final UserService userService;

    @Operation(summary = "Получение профиля текущего пользователя")
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getCurrentProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        User user = userService.getByUsername(userDetails.getUsername());
        ProfileResponse response = service.getProfileByUser(user);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Редактирование профиля текущего пользователя")
    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> editProfile(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                       @Valid @RequestBody ProfileRequest request) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        User user = userService.getByUsername(userDetails.getUsername());
        ProfileResponse response = service.updateProfile(user, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Загрузка аватарки текущему пользователю")
    @PostMapping(value = "/me/avatar")
    public ResponseEntity<ProfileResponse> uploadAvatar(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                        @RequestParam("file") MultipartFile imageFile) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            User user = userService.getByUsername(userDetails.getUsername());
            ProfileResponse response = service.uploadAvatar(user, imageFile);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Получения профиля по Id пользователя")
    @GetMapping("/{userId}")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable Long userId) {
        try {
            ProfileResponse response = service.getProfileByUserId(userId);
            return ResponseEntity.ok(response);
        } catch (ProfileNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}