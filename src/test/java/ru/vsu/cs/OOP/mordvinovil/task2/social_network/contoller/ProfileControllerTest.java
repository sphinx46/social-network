package ru.vsu.cs.OOP.mordvinovil.task2.social_network.contoller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller.ProfileController;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.ProfileService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.BaseControllerTest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
class ProfileControllerTest extends BaseControllerTest {

    @MockitoBean
    private ProfileService profileService;

    @Test
    @DisplayName("Получение профиля текущего пользователя без авторизации - должно вернуть 401")
    void getCurrentProfile_whenUnAuthorized_shouldReturn401() throws Exception {
        mockMvcUtils.performGet("/profile/me")
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение профиля текущего пользователя - успешно")
    void getCurrentProfile_whenRequestIsValid() throws Exception {
        var response = TestDataFactory.createProfileResponse();

        when(profileService.getProfileByUser(any())).thenReturn(response);

        mockMvcUtils.performGet("/profile/me")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.city").value("Москва"))
                .andExpect(jsonPath("$.bio").value("Тестовое описание"))
                .andExpect(jsonPath("$.age").value(25));

        verify(profileService, times(1)).getProfileByUser(any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Получение профиля текущего пользователя - когда сервис выбрасывает исключение")
    void getCurrentProfile_whenServiceThrowsException_shouldReturnError() throws Exception {
        when(profileService.getProfileByUser(any()))
                .thenThrow(new RuntimeException("Ошибка получения профиля"));

        mockMvcUtils.performGet("/profile/me")
                .andExpect(status().isInternalServerError());

        verify(profileService, times(1)).getProfileByUser(any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Редактирование профиля текущего пользователя - успешно")
    void editProfile_whenRequestIsValid() throws Exception {
        var request = TestDataFactory.createProfileRequest();
        var response = TestDataFactory.createProfileResponse();

        when(profileService.updateProfile(any(), any())).thenReturn(response);

        mockMvcUtils.performPut("/profile/me", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.city").value("Москва"))
                .andExpect(jsonPath("$.bio").value("Тестовое описание"));

        verify(profileService, times(1)).updateProfile(any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @DisplayName("Редактирование профиля текущего пользователя без авторизации - должно вернуть 401")
    void editProfile_whenUnAuthorized_shouldReturn401() throws Exception {
        var request = TestDataFactory.createProfileRequest();

        mockMvcUtils.performPut("/profile/me", request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Редактирование профиля - когда сервис выбрасывает исключение")
    void editProfile_whenServiceThrowsException_shouldReturnError() throws Exception {
        var request = TestDataFactory.createProfileRequest();

        when(profileService.updateProfile(any(), any()))
                .thenThrow(new RuntimeException("Ошибка обновления профиля"));

        mockMvcUtils.performPut("/profile/me", request)
                .andExpect(status().isInternalServerError());

        verify(profileService, times(1)).updateProfile(any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Загрузка аватарки текущему пользователю - успешно")
    void uploadAvatar_whenRequestIsValid() throws Exception {
        var response = TestDataFactory.createProfileResponse();

        when(profileService.uploadAvatar(any(), any())).thenReturn(response);

        MockMultipartFile file = mockMvcUtils.createMockImageFile();

        mockMvcUtils.performMultipart("/profile/me/avatar", file)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.imageUrl").value("http://example.com/image.jpg"));

        verify(profileService, times(1)).uploadAvatar(any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @DisplayName("Загрузка аватарки текущему пользователю без авторизации - должно вернуть 401")
    void uploadAvatar_whenUnAuthorized_shouldReturn401() throws Exception {
        MockMultipartFile file = mockMvcUtils.createMockImageFile();

        mockMvcUtils.performMultipart("/profile/me/avatar", file)
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "USER")
    @DisplayName("Загрузка аватарки - когда сервис выбрасывает исключение")
    void uploadAvatar_whenServiceThrowsException_shouldReturnError() throws Exception {
        MockMultipartFile file = mockMvcUtils.createMockImageFile();

        when(profileService.uploadAvatar(any(), any()))
                .thenThrow(new RuntimeException("Ошибка загрузки аватарки"));

        mockMvcUtils.performMultipart("/profile/me/avatar", file)
                .andExpect(status().isInternalServerError());

        verify(profileService, times(1)).uploadAvatar(any(), any());
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    @DisplayName("Получение профиля по ID пользователя - успешно")
    @WithMockUser
    void getProfile_whenRequestIsValid() throws Exception {
        Long userId = 1L;
        var response = TestDataFactory.createProfileResponse();

        when(profileService.getProfileByUserId(userId)).thenReturn(response);

        mockMvcUtils.performGet("/profile/" + userId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.city").value("Москва"))
                .andExpect(jsonPath("$.bio").value("Тестовое описание"));

        verify(profileService, times(1)).getProfileByUserId(userId);
        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Получение профиля по ID пользователя - выбрасывает исключение, профиль не существует")
    @WithMockUser
    void getProfile_whenProfileIsNotExists() throws Exception {
        Long userId = 1L;

        when(profileService.getProfileByUserId(userId))
                .thenThrow(new RuntimeException("Профиль не существует"));

        mockMvcUtils.performGet("/profile/" + userId)
                .andExpect(status().isInternalServerError());

        verify(profileService, times(1)).getProfileByUserId(userId);
        verifyNoInteractions(userService);
    }
}