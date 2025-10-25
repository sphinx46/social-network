package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.ProfileRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.ProfileResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Profile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.ProfileRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.ProfileService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.ProfileAgeCalculator;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.ProfileFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.ProfileValidator;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    private final FileStorageServiceImpl fileStorageServiceImpl;
    private final ProfileRepository profileRepository;
    private final UserServiceImpl userServiceImpl;
    private final EntityMapper entityMapper;
    private final ProfileFactory profileFactory;
    private final ProfileAgeCalculator ageCalculator;
    private final ProfileValidator profileValidator;
    private final EntityUtils entityUtils;

    @Override
    public Profile create(ProfileRequest request, User user) {
        profileValidator.validate(request, user);

        Profile profile = profileFactory.createProfile(user, request);
        return profileRepository.save(profile);
    }

    @Override
    public ProfileResponse getProfileByUser(User user) {
        Profile profile = entityUtils.getProfileByUser(user);
        ProfileResponse response = entityMapper.map(profile, ProfileResponse.class);
        response.setAge(ageCalculator.calculateAge(profile.getDateOfBirth()));
        return response;
    }

    @Override
    public ProfileResponse getProfileByUserId(Long id) {
        User user = userServiceImpl.getById(id);
        return getProfileByUser(user);
    }

    @Transactional
    @Override
    public ProfileResponse uploadAvatar(User user, MultipartFile imageFile) {
        fileStorageServiceImpl.validateImageFile(imageFile);
        profileValidator.validateAvatarUpload(user);

        Profile profile = entityUtils.getProfileByUser(user);

        if (profile.getImageUrl() != null && !profile.getImageUrl().isEmpty()) {
            fileStorageServiceImpl.deleteFile(profile.getImageUrl());
        }

        String avatarUrl = fileStorageServiceImpl.saveAvatar(imageFile, user.getId());
        profile.setImageUrl(avatarUrl);
        Profile updatedProfile = profileRepository.save(profile);

        return entityMapper.map(updatedProfile, ProfileResponse.class);
    }

    @Transactional
    @Override
    public ProfileResponse removeAvatar(User user) {
        profileValidator.validateAvatarUpload(user);

        Profile profile = entityUtils.getProfileByUser(user);

        if (profile.getImageUrl() != null && !profile.getImageUrl().isEmpty()) {
            fileStorageServiceImpl.deleteFile(profile.getImageUrl());
        }

        profile.setImageUrl(null);
        Profile updatedProfile = profileRepository.save(profile);
        return entityMapper.map(updatedProfile, ProfileResponse.class);
    }

    @Transactional
    @Override
    public ProfileResponse updateProfile(User user, ProfileRequest request) {
        profileValidator.validateProfileUpdate(request, user);

        Profile profile = entityUtils.getProfileByUser(user);
        updateProfileFromRequest(profile, user, request);

        Profile updatedProfile = profileRepository.save(profile);
        return entityMapper.map(updatedProfile, ProfileResponse.class);
    }

    @Transactional
    @Override
    public Profile createDefaultProfile(User user) {
        return profileRepository.findByUser(user)
                .orElseGet(() -> profileRepository.save(profileFactory.createDefaultProfile(user)));
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
                fileStorageServiceImpl.deleteFile(profile.getImageUrl());
            }
            profile.setImageUrl(request.getImageUrl());
        }
    }
}