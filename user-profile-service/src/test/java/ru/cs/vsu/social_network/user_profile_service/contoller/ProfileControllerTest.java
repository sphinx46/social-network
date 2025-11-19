package ru.cs.vsu.social_network.user_profile_service.contoller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import ru.cs.vsu.social_network.user_profile_service.controller.ProfileController;
import ru.cs.vsu.social_network.user_profile_service.dto.request.ProfileEditRequest;
import ru.cs.vsu.social_network.user_profile_service.dto.response.ProfileResponse;
import ru.cs.vsu.social_network.user_profile_service.exceptions.profile.ProfileNotFoundException;
import ru.cs.vsu.social_network.user_profile_service.service.ProfileService;
import ru.cs.vsu.social_network.user_profile_service.utils.TestDataFactory;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
@ActiveProfiles("test")
public class ProfileControllerTest extends BaseControllerTest {
    @MockitoBean
    private ProfileService profileService;

    private static final UUID TEST_USER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @Test
    @WithMockUser(username = "testUser", authorities = "ROLE_USER")
    @DisplayName("Создание профиля по умолчанию - успешно")
    void createDefaultProfile_whenRequestIsValid() throws Exception {
        String username = "testUser";
        ProfileResponse response = TestDataFactory.createProfileResponse(username, null, null);

        when(profileService.createDefaultProfile(any(UUID.class), any(String.class))).thenReturn(response);

        mockMvcUtils.performPost("/profile/me", username,
                        "X-User-Id", TEST_USER_ID.toString(),
                        "X-Username", username)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));

        verify(profileService, times(1)).createDefaultProfile(any(UUID.class), eq(username));
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "ROLE_USER")
    @DisplayName("Получение профиля текущего пользователя - успешно")
    void getCurrentUserProfile_whenRequestIsValid() throws Exception {
        String username = "testUser";
        ProfileResponse response = TestDataFactory.createProfileResponse(username, "Moscow", "Test bio");

        when(profileService.getProfileByUserId(any(UUID.class))).thenReturn(response);

        mockMvcUtils.performGet("/profile/me",
                        "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.city").value("Moscow"))
                .andExpect(jsonPath("$.bio").value("Test bio"));

        verify(profileService, times(1)).getProfileByUserId(any(UUID.class));
    }


    @Test
    @WithMockUser(username = "testUser", authorities = "ROLE_USER")
    @DisplayName("Получение профиля текущего пользователя - когда профиль не найден")
    void getCurrentUserProfile_whenProfileNotFound() throws Exception {
        when(profileService.getProfileByUserId(any(UUID.class)))
                .thenThrow(new ProfileNotFoundException("Профиль не найден"));

        mockMvcUtils.performGet("/profile/me",
                        "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isNotFound());

        verify(profileService, times(1)).getProfileByUserId(any(UUID.class));
    }


    @Test
    @WithMockUser(username = "testUser", authorities = "ROLE_USER")
    @DisplayName("Редактирование профиля - успешно")
    void editProfile_whenRequestIsValid() throws Exception {
        String username = "testUser";
        ProfileEditRequest request = TestDataFactory.createProfileRequest();
        ProfileResponse response = TestDataFactory.createProfileResponse(username, "Москва", "I am Ilya.");

        when(profileService.editProfile(any(UUID.class), any(ProfileEditRequest.class))).thenReturn(response);

        mockMvcUtils.performPut("/profile/me", request,
                        "X-User-Id", TEST_USER_ID.toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.city").value("Москва"))
                .andExpect(jsonPath("$.bio").value("I am Ilya."));

        verify(profileService, times(1)).editProfile(any(UUID.class), any(ProfileEditRequest.class));
    }

    @Test
    @DisplayName("Получение профиля по ID - успешно")
    void getProfileById_whenRequestIsValid() throws Exception {
        String username = "testUser";
        ProfileResponse response = TestDataFactory.createProfileResponse(username, "Moscow", "Test bio");

        when(profileService.getProfileByUserId(any(UUID.class))).thenReturn(response);

        mockMvcUtils.performGet("/profile/" + TEST_USER_ID)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.city").value("Moscow"));

        verify(profileService, times(1)).getProfileByUserId(any(UUID.class));
    }

    @Test
    @DisplayName("Получение профиля по ID - когда профиль не найден")
    void getProfileById_whenProfileNotFound() throws Exception {
        when(profileService.getProfileByUserId(any(UUID.class)))
                .thenThrow(new ProfileNotFoundException("Профиль не найден"));

        mockMvcUtils.performGet("/profile/" + TEST_USER_ID)
                .andExpect(status().isNotFound());

        verify(profileService, times(1)).getProfileByUserId(any(UUID.class));
    }
}
