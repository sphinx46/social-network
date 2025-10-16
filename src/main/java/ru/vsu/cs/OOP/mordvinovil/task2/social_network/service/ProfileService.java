package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.ProfileRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.ProfileResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Profile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.ProfileRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.ProfileAgeCalculator;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.ProfileFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.ProfileValidator;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final FileStorageService fileStorageService;
    private final ProfileRepository profileRepository;
    private final UserService userService;
    private final EntityMapper entityMapper;
    private final ProfileFactory profileFactory;
    private final ProfileAgeCalculator ageCalculator;
    private final ProfileValidator profileValidator;

    public Profile create(User user, ProfileRequest request) {
        profileValidator.validate(request, user);

        Profile profile = profileFactory.createProfile(user, request);
        return profileRepository.save(profile);
    }

    public ProfileResponse getProfileByUser(User user) {
        Profile profile = getProfileEntity(user);
        ProfileResponse response = entityMapper.map(profile, ProfileResponse.class);
        response.setAge(ageCalculator.calculateAge(profile.getDateOfBirth()));
        return response;
    }

    public ProfileResponse getProfileByUserId(Long id) {
        User user = userService.getById(id);
        return getProfileByUser(user);
    }

    @Transactional
    public ProfileResponse uploadAvatar(User user, MultipartFile imageFile) {
        fileStorageService.validateImageFile(imageFile);
        profileValidator.validateAvatarUpload(user);

        Profile profile = getProfileEntity(user);

        if (profile.getImageUrl() != null && !profile.getImageUrl().isEmpty()) {
            fileStorageService.deleteFile(profile.getImageUrl());
        }

        String avatarUrl = fileStorageService.saveAvatar(imageFile, user.getId());
        profile.setImageUrl(avatarUrl);
        Profile updatedProfile = profileRepository.save(profile);

        return entityMapper.map(updatedProfile, ProfileResponse.class);
    }

    @Transactional
    public ProfileResponse removeAvatar(User user) {
        profileValidator.validateAvatarUpload(user);

        Profile profile = getProfileEntity(user);

        if (profile.getImageUrl() != null && !profile.getImageUrl().isEmpty()) {
            fileStorageService.deleteFile(profile.getImageUrl());
        }

        profile.setImageUrl(null);
        Profile updatedProfile = profileRepository.save(profile);
        return entityMapper.map(updatedProfile, ProfileResponse.class);
    }

    @Transactional
    public ProfileResponse updateProfile(User user, ProfileRequest request) {
        profileValidator.validateProfileUpdate(request, user);

        Profile profile = getProfileEntity(user);
        updateProfileFromRequest(profile, user, request);

        Profile updatedProfile = profileRepository.save(profile);
        return entityMapper.map(updatedProfile, ProfileResponse.class);
    }

    @Transactional
    public Profile createDefaultProfile(User user) {
        return profileRepository.findByUser(user)
                .orElseGet(() -> profileRepository.save(profileFactory.createDefaultProfile(user)));
    }

    private Profile getProfileEntity(User user) {
        return profileRepository.findByUser(user)
                .orElseThrow(() -> new ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.ProfileNotFoundException(ResponseMessageConstants.NOT_FOUND));
    }

    private void updateProfileFromRequest(Profile profile, User user, ProfileRequest request) {
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }

        if (request.getCity() != null) {
            profile.setCity(request.getCity());
            user.setCity(request.getCity());
        }

        if (request.getDateOfBirth() != null) {
            profile.setDateOfBirth(request.getDateOfBirth());
        }

        if (request.getImageUrl() != null) {
            if (profile.getImageUrl() != null && !profile.getImageUrl().isEmpty()) {
                fileStorageService.deleteFile(profile.getImageUrl());
            }
            profile.setImageUrl(request.getImageUrl());
        }
    }
}