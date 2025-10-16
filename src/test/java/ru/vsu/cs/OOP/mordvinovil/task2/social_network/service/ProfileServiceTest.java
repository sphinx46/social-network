package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.ProfileRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.ProfileResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Profile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.ProfileAlreadyExistsException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.ProfileRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.ProfileAgeCalculator;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.ProfileFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.ProfileValidator;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory.*;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceTest {

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserService userService;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private ProfileFactory profileFactory;

    @Mock
    private ProfileAgeCalculator ageCalculator;

    @Mock
    private ProfileValidator profileValidator;

    @Mock
    private EntityUtils entityUtils;

    @InjectMocks
    private ProfileService profileService;

    @Test
    void createProfile_whenProfileDoesNotExist() {
        User user = createTestUser(1L, "user", "user@example.com");
        ProfileRequest request = createProfileRequest();
        Profile profile = new Profile();
        profile.setUser(user);
        profile.setBio(request.getBio());
        profile.setCity(request.getCity());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setImageUrl(request.getImageUrl());

        doNothing().when(profileValidator).validate(request, user);
        when(profileFactory.createProfile(user, request)).thenReturn(profile);
        when(profileRepository.save(any(Profile.class))).thenReturn(profile);

        Profile result = profileService.create(user, request);

        assertNotNull(result);
        verify(profileValidator).validate(request, user);
        verify(profileFactory).createProfile(user, request);
        verify(profileRepository).save(profile);
    }

    @Test
    void createProfile_whenProfileAlreadyExists() {
        User user = createTestUser(1L, "user", "user@example.com");
        ProfileRequest request = createProfileRequest();

        doThrow(new ProfileAlreadyExistsException(ResponseMessageConstants.FAILURE_CREATE_PROFILE))
                .when(profileValidator).validate(request, user);

        ProfileAlreadyExistsException exception = assertThrows(ProfileAlreadyExistsException.class,
                () -> profileService.create(user, request));

        assertEquals(ResponseMessageConstants.FAILURE_CREATE_PROFILE, exception.getMessage());
    }

    @Test
    void getProfileByUser_whenProfileExists() {
        User user = createTestUser(1L, "user", "user@example.com");
        Profile profile = new Profile();
        profile.setUser(user);
        profile.setDateOfBirth(LocalDateTime.now().minusYears(25));
        ProfileResponse response = createProfileResponse();
        response.setAge(25);

        when(entityUtils.getProfileByUser(user)).thenReturn(profile);
        when(entityMapper.map(profile, ProfileResponse.class)).thenReturn(response);
        when(ageCalculator.calculateAge(profile.getDateOfBirth())).thenReturn(25);

        ProfileResponse result = profileService.getProfileByUser(user);

        assertNotNull(result);
        assertEquals(25, result.getAge());

        verify(entityUtils).getProfileByUser(user);
        verify(entityMapper).map(profile, ProfileResponse.class);
        verify(ageCalculator).calculateAge(profile.getDateOfBirth());
    }

    @Test
    void getProfileByUserId() {
        User user = createTestUser(1L, "user", "user@example.com");
        Profile profile = new Profile();
        profile.setUser(user);
        profile.setDateOfBirth(LocalDateTime.now().minusYears(30));
        ProfileResponse response = createProfileResponse();
        response.setAge(30);

        when(userService.getById(1L)).thenReturn(user);
        when(entityUtils.getProfileByUser(user)).thenReturn(profile);
        when(entityMapper.map(profile, ProfileResponse.class)).thenReturn(response);
        when(ageCalculator.calculateAge(profile.getDateOfBirth())).thenReturn(30);

        ProfileResponse result = profileService.getProfileByUserId(1L);

        assertNotNull(result);
        assertEquals(30, result.getAge());

        verify(userService).getById(1L);
        verify(entityUtils).getProfileByUser(user);
        verify(entityMapper).map(profile, ProfileResponse.class);
        verify(ageCalculator).calculateAge(profile.getDateOfBirth());
    }

    @Test
    void uploadAvatar_whenProfileExists() {
        User user = createTestUser(1L, "user", "user@example.com");
        Profile profile = new Profile();
        profile.setUser(user);
        profile.setImageUrl("old-avatar.jpg");
        MultipartFile imageFile = mock(MultipartFile.class);
        Profile updatedProfile = new Profile();
        updatedProfile.setUser(user);
        updatedProfile.setImageUrl("new-avatar.jpg");
        ProfileResponse expectedResponse = createProfileResponse();
        expectedResponse.setImageUrl("new-avatar.jpg");

        when(entityUtils.getProfileByUser(user)).thenReturn(profile);
        doNothing().when(profileValidator).validateAvatarUpload(user);
        when(fileStorageService.saveAvatar(imageFile, 1L)).thenReturn("new-avatar.jpg");
        when(profileRepository.save(any(Profile.class))).thenReturn(updatedProfile);
        when(entityMapper.map(updatedProfile, ProfileResponse.class)).thenReturn(expectedResponse);

        ProfileResponse result = profileService.uploadAvatar(user, imageFile);

        assertNotNull(result);
        assertEquals("new-avatar.jpg", result.getImageUrl());

        verify(fileStorageService).validateImageFile(imageFile);
        verify(profileValidator).validateAvatarUpload(user);
        verify(entityUtils).getProfileByUser(user);
        verify(fileStorageService).deleteFile("old-avatar.jpg");
        verify(fileStorageService).saveAvatar(imageFile, 1L);
        verify(profileRepository).save(any(Profile.class));
        verify(entityMapper).map(updatedProfile, ProfileResponse.class);
    }

    @Test
    void removeAvatar_whenProfileExists() {
        User user = createTestUser(1L, "user", "user@example.com");
        Profile profile = new Profile();
        profile.setUser(user);
        profile.setImageUrl("avatar.jpg");
        Profile updatedProfile = new Profile();
        updatedProfile.setUser(user);
        updatedProfile.setImageUrl(null);
        ProfileResponse expectedResponse = createProfileResponse();
        expectedResponse.setImageUrl(null);

        when(entityUtils.getProfileByUser(user)).thenReturn(profile);
        doNothing().when(profileValidator).validateAvatarUpload(user);
        when(profileRepository.save(any(Profile.class))).thenReturn(updatedProfile);
        when(entityMapper.map(updatedProfile, ProfileResponse.class)).thenReturn(expectedResponse);

        ProfileResponse result = profileService.removeAvatar(user);

        assertNotNull(result);
        assertNull(result.getImageUrl());

        verify(profileValidator).validateAvatarUpload(user);
        verify(entityUtils).getProfileByUser(user);
        verify(fileStorageService).deleteFile("avatar.jpg");
        verify(profileRepository).save(any(Profile.class));
        verify(entityMapper).map(updatedProfile, ProfileResponse.class);
    }

    @Test
    void updateProfile_whenProfileExists() {
        User user = createTestUser(1L, "user", "user@example.com");
        user.setCity("OldCity");
        ProfileRequest request = createProfileRequest();
        Profile profile = new Profile();
        profile.setUser(user);
        profile.setBio("Old bio");
        profile.setCity("OldCity");
        profile.setImageUrl("old.jpg");

        Profile updatedProfile = new Profile();
        updatedProfile.setUser(user);
        updatedProfile.setBio(request.getBio());
        updatedProfile.setCity(request.getCity());
        updatedProfile.setDateOfBirth(request.getDateOfBirth());
        updatedProfile.setImageUrl(request.getImageUrl());
        ProfileResponse expectedResponse = createProfileResponse();

        when(entityUtils.getProfileByUser(user)).thenReturn(profile);
        doNothing().when(profileValidator).validateProfileUpdate(request, user);
        when(profileRepository.save(any(Profile.class))).thenReturn(updatedProfile);
        when(entityMapper.map(updatedProfile, ProfileResponse.class)).thenReturn(expectedResponse);

        ProfileResponse result = profileService.updateProfile(user, request);

        assertNotNull(result);

        verify(profileValidator).validateProfileUpdate(request, user);
        verify(entityUtils).getProfileByUser(user);
        verify(fileStorageService).deleteFile("old.jpg");
        verify(profileRepository).save(any(Profile.class));
        verify(entityMapper).map(updatedProfile, ProfileResponse.class);
    }

    @Test
    void createDefaultProfile_whenProfileDoesNotExist() {
        User user = createTestUser(1L, "user", "user@example.com");
        Profile defaultProfile = new Profile();
        defaultProfile.setUser(user);

        when(profileRepository.findByUser(user)).thenReturn(Optional.empty());
        when(profileFactory.createDefaultProfile(user)).thenReturn(defaultProfile);
        when(profileRepository.save(any(Profile.class))).thenReturn(defaultProfile);

        Profile result = profileService.createDefaultProfile(user);

        assertNotNull(result);
        verify(profileRepository).findByUser(user);
        verify(profileFactory).createDefaultProfile(user);
        verify(profileRepository).save(defaultProfile);
    }

    @Test
    void createDefaultProfile_whenProfileAlreadyExists() {
        User user = createTestUser(1L, "user", "user@example.com");
        Profile existingProfile = new Profile();
        existingProfile.setUser(user);

        when(profileRepository.findByUser(user)).thenReturn(Optional.of(existingProfile));

        Profile result = profileService.createDefaultProfile(user);

        assertNotNull(result);
        assertEquals(existingProfile, result);
        verify(profileRepository).findByUser(user);
        verify(profileFactory, never()).createDefaultProfile(any());
        verify(profileRepository, never()).save(any());
    }
}