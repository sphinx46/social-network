package ru.cs.vsu.social_network.user_profile_service.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.cs.vsu.social_network.user_profile_service.dto.request.ProfileEditRequest;
import ru.cs.vsu.social_network.user_profile_service.dto.response.ProfileResponse;
import ru.cs.vsu.social_network.user_profile_service.entity.Profile;
import ru.cs.vsu.social_network.user_profile_service.exceptions.profile.ProfileBioTooLongException;
import ru.cs.vsu.social_network.user_profile_service.exceptions.profile.ProfileUploadAvatarException;
import ru.cs.vsu.social_network.user_profile_service.factory.ProfileFactory;
import ru.cs.vsu.social_network.user_profile_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.user_profile_service.provider.ProfileEntityProvider;
import ru.cs.vsu.social_network.user_profile_service.repository.ProfileRepository;
import ru.cs.vsu.social_network.user_profile_service.service.serviceImpl.ProfileServiceImpl;
import ru.cs.vsu.social_network.user_profile_service.utils.TestDataFactory;
import ru.cs.vsu.social_network.user_profile_service.utils.constants.MessageConstants;
import ru.cs.vsu.social_network.user_profile_service.validation.ProfileValidator;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceImplTest {
    @Mock
    private ProfileEntityProvider provider;
    @Mock
    private ProfileFactory factory;
    @Mock
    private ProfileValidator validator;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private EntityMapper mapper;

    @InjectMocks
    private ProfileServiceImpl profileServiceImpl;

    private static final UUID TEST_USER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @Test
    @DisplayName("Создание профиля по умолчанию - успешно")
    void createDefaultProfile_whenRequestIsValid() {
        String username = "testUser";
        Profile profile = TestDataFactory.createTestProfile(username, TEST_USER_ID);
        ProfileResponse expectedResponse = TestDataFactory.createProfileResponse(username, null, null, null);

        when(profileRepository.existsByKeycloakUserId(TEST_USER_ID)).thenReturn(false);
        when(factory.createDefaultProfile(TEST_USER_ID, username)).thenReturn(profile);
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);
        when(mapper.map(any(Profile.class), eq(ProfileResponse.class))).thenReturn(expectedResponse);

        ProfileResponse result = profileServiceImpl.createDefaultProfile(TEST_USER_ID, username);

        assertNotNull(result);
        verify(factory).createDefaultProfile(TEST_USER_ID, username);
        verify(profileRepository).save(profile);
        verify(mapper).map(profile, ProfileResponse.class);
    }

    @Test
    @DisplayName("Создание профиля по умолчанию - когда профиль уже существует")
    void createDefaultProfile_whenProfileExists() {
        String username = "testUser";
        Profile existingProfile = TestDataFactory.createTestProfile(username, TEST_USER_ID);
        ProfileResponse expectedResponse = TestDataFactory.createProfileResponse(username, null, null, null);

        when(profileRepository.existsByKeycloakUserId(TEST_USER_ID)).thenReturn(true);
        when(provider.getByKeycloakUserId(TEST_USER_ID)).thenReturn(existingProfile);
        when(mapper.map(any(Profile.class), eq(ProfileResponse.class))).thenReturn(expectedResponse);

        ProfileResponse result = profileServiceImpl.createDefaultProfile(TEST_USER_ID, username);

        assertNotNull(result);
        verify(profileRepository, never()).save(any(Profile.class));
        verify(factory, never()).createDefaultProfile(any(), any());
        verify(provider).getByKeycloakUserId(TEST_USER_ID);
    }

    @Test
    @DisplayName("Получение профиля по userId - успешно")
    void getProfileByUserId_whenRequestIsValid() {
        String username = "testUser";
        Profile profile = TestDataFactory.createTestProfile(username, TEST_USER_ID);
        ProfileResponse expectedResponse = TestDataFactory.createProfileResponse(username, "Moscow", "Test bio", null);

        when(provider.getByKeycloakUserId(TEST_USER_ID)).thenReturn(profile);
        when(mapper.map(any(Profile.class), eq(ProfileResponse.class))).thenReturn(expectedResponse);

        ProfileResponse result = profileServiceImpl.getProfileByUserId(TEST_USER_ID);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(provider).getByKeycloakUserId(TEST_USER_ID);
        verify(mapper).map(profile, ProfileResponse.class);
    }

    @Test
    @DisplayName("Редактирование профиля - успешно")
    void editProfile_whenRequestIsValid() {
        String username = "testUser";
        Profile profile = TestDataFactory.createTestProfile(username, TEST_USER_ID);
        ProfileEditRequest profileRequest = TestDataFactory.createProfileRequest();

        Profile updatedProfile = TestDataFactory.createTestProfile(username, TEST_USER_ID);
        updatedProfile.setCity("Moscow");
        updatedProfile.setBio("I am Ilya.");
        ProfileResponse expectedResponse = TestDataFactory.createProfileResponse(username, "Moscow", "I am Ilya.", null);

        when(provider.getByKeycloakUserId(TEST_USER_ID)).thenReturn(profile);
        when(profileRepository.save(any(Profile.class))).thenReturn(updatedProfile);
        doNothing().when(validator).validateProfileEdit(TEST_USER_ID, profileRequest);
        when(mapper.map(updatedProfile, ProfileResponse.class)).thenReturn(expectedResponse);

        ProfileResponse result = profileServiceImpl.editProfile(TEST_USER_ID, profileRequest);

        assertNotNull(result);
        assertEquals("Moscow", result.getCity());
        assertEquals("I am Ilya.", result.getBio());

        verify(validator).validateProfileEdit(TEST_USER_ID, profileRequest);
        verify(provider).getByKeycloakUserId(TEST_USER_ID);
        verify(profileRepository).save(any(Profile.class));
        verify(mapper).map(updatedProfile, ProfileResponse.class);
    }

    @Test
    @DisplayName("Редактирование профиля - когда валидация не пройдена")
    void editProfile_whenRequestIsNotValid() {
        ProfileEditRequest profileRequest = TestDataFactory.createProfileRequest();

        doThrow(new ProfileBioTooLongException(MessageConstants.FAILURE_PROFILE_CITY_TOO_LONG))
                .when(validator).validateProfileEdit(TEST_USER_ID, profileRequest);

        ProfileBioTooLongException exception = assertThrows(ProfileBioTooLongException.class,
                () -> profileServiceImpl.editProfile(TEST_USER_ID, profileRequest));

        assertEquals(MessageConstants.FAILURE_PROFILE_CITY_TOO_LONG, exception.getMessage());

        verify(validator).validateProfileEdit(TEST_USER_ID, profileRequest);
        verify(profileRepository, never()).save(any(Profile.class));
        verify(provider, never()).getByKeycloakUserId(any(UUID.class));
        verify(mapper, never()).map(any(Profile.class), any(Class.class));
    }

    @Test
    @DisplayName("Загрузка аватара - успешно")
    void uploadAvatar_whenRequestIsValid() {
        String username = "testUser";
        String avatarUrl = "https://storage.example.com/avatars/user123.jpg";
        Profile profile = TestDataFactory.createTestProfile(username, TEST_USER_ID);
        Profile updatedProfile = TestDataFactory.createTestProfile(username, TEST_USER_ID);
        updatedProfile.setAvatarUrl(avatarUrl);
        ProfileResponse expectedResponse = TestDataFactory.createProfileResponse(username, null, null, avatarUrl);

        when(provider.getByKeycloakUserId(TEST_USER_ID)).thenReturn(profile);
        when(profileRepository.save(any(Profile.class))).thenReturn(updatedProfile);
        when(mapper.map(any(Profile.class), eq(ProfileResponse.class))).thenReturn(expectedResponse);

        ProfileResponse result = profileServiceImpl.uploadAvatar(TEST_USER_ID,
                TestDataFactory.createUploadAvatarRequest(avatarUrl));

        assertNotNull(result);
        assertEquals(avatarUrl, result.getAvatarUrl());
        verify(provider).getByKeycloakUserId(TEST_USER_ID);
        verify(profileRepository).save(any(Profile.class));
        verify(mapper).map(updatedProfile, ProfileResponse.class);
    }

    @Test
    @DisplayName("Загрузка аватара - когда URL пустой")
    void uploadAvatar_whenUrlIsEmpty() {
        String emptyUrl = "";

        ProfileUploadAvatarException exception = assertThrows(ProfileUploadAvatarException.class,
                () -> profileServiceImpl.uploadAvatar(TEST_USER_ID,
                        TestDataFactory.createUploadAvatarRequest(emptyUrl)));

        assertEquals(MessageConstants.FAILURE_PROFILE_UPLOAD_AVATAR, exception.getMessage());

        verify(provider, never()).getByKeycloakUserId(any(UUID.class));
        verify(profileRepository, never()).save(any(Profile.class));
        verify(mapper, never()).map(any(Profile.class), any(Class.class));
    }

    @Test
    @DisplayName("Загрузка аватара - когда URL состоит из пробелов")
    void uploadAvatar_whenUrlIsBlank() {
        String blankUrl = "   ";

        ProfileUploadAvatarException exception = assertThrows(ProfileUploadAvatarException.class,
                () -> profileServiceImpl.uploadAvatar(TEST_USER_ID,
                        TestDataFactory.createUploadAvatarRequest(blankUrl)));

        assertEquals(MessageConstants.FAILURE_PROFILE_UPLOAD_AVATAR, exception.getMessage());

        verify(provider, never()).getByKeycloakUserId(any(UUID.class));
        verify(profileRepository, never()).save(any(Profile.class));
        verify(mapper, never()).map(any(Profile.class), any(Class.class));
    }

    @Test
    @DisplayName("Загрузка аватара - когда URL null")
    void uploadAvatar_whenUrlIsNull() {
        ProfileUploadAvatarException exception = assertThrows(ProfileUploadAvatarException.class,
                () -> profileServiceImpl.uploadAvatar(TEST_USER_ID,
                        TestDataFactory.createUploadAvatarRequest(null)));

        assertEquals(MessageConstants.FAILURE_PROFILE_UPLOAD_AVATAR, exception.getMessage());

        verify(provider, never()).getByKeycloakUserId(any(UUID.class));
        verify(profileRepository, never()).save(any(Profile.class));
        verify(mapper, never()).map(any(Profile.class), any(Class.class));
    }

    @Test
    @DisplayName("Загрузка аватара - обновление существующего аватара")
    void uploadAvatar_whenReplacingExistingAvatar() {
        String username = "testUser";
        String oldAvatarUrl = "https://storage.example.com/avatars/old.jpg";
        String newAvatarUrl = "https://storage.example.com/avatars/new.jpg";
        Profile profile = TestDataFactory.createTestProfile(username, TEST_USER_ID);
        profile.setAvatarUrl(oldAvatarUrl);
        Profile updatedProfile = TestDataFactory.createTestProfile(username, TEST_USER_ID);
        updatedProfile.setAvatarUrl(newAvatarUrl);
        ProfileResponse expectedResponse = TestDataFactory.createProfileResponse(username, null, null, newAvatarUrl);

        when(provider.getByKeycloakUserId(TEST_USER_ID)).thenReturn(profile);
        when(profileRepository.save(any(Profile.class))).thenReturn(updatedProfile);
        when(mapper.map(any(Profile.class), eq(ProfileResponse.class))).thenReturn(expectedResponse);

        ProfileResponse result = profileServiceImpl.uploadAvatar(TEST_USER_ID,
                TestDataFactory.createUploadAvatarRequest(newAvatarUrl));

        assertNotNull(result);
        assertEquals(newAvatarUrl, result.getAvatarUrl());
        verify(provider).getByKeycloakUserId(TEST_USER_ID);
        verify(profileRepository).save(any(Profile.class));
        verify(mapper).map(updatedProfile, ProfileResponse.class);
    }
}
