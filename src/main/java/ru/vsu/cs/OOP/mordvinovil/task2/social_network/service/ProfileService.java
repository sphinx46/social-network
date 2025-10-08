package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.ProfileRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.ProfileResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Profile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.ProfileAlreadyExistsException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.ProfileNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.ProfileRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final FileStorageService fileStorageService;
    private final ProfileRepository profileRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    public Profile create(User user, ProfileRequest request) {
        if (profileRepository.findByUser(user).isPresent()) {
            throw new ProfileAlreadyExistsException(ResponseMessageConstants.FAILURE_CREATE_PROFILE);
        }

        var profile = Profile.builder()
                .user(user)
                .bio(request.getBio() != null ? request.getBio() : "")
                .city(request.getCity() != null ? request.getCity() : user.getCity())
                .dateOfBirth(request.getDateOfBirth())
                .imageUrl(request.getImageUrl())
                .build();

        return profileRepository.save(profile);
    }

    public ProfileResponse getProfileByUser(User user) {
        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new ProfileNotFoundException(ResponseMessageConstants.NOT_FOUND));

        ProfileResponse response = modelMapper.map(profile, ProfileResponse.class);
        response.setAge(calculateAge(profile.getDateOfBirth()));
        return response;
    }

    public ProfileResponse getProfileByUserId(Long id) {
        User user = userService.getById(id);
        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new ProfileNotFoundException(ResponseMessageConstants.NOT_FOUND));

        ProfileResponse response = modelMapper.map(profile, ProfileResponse.class);
        response.setAge(calculateAge(profile.getDateOfBirth()));
        return response;
    }

    private Integer calculateAge(LocalDateTime dateOfBirth) {
        if (dateOfBirth == null) {
            return null;
        }
        return Period.between(LocalDate.from(dateOfBirth), LocalDate.now()).getYears();
    }

    @Transactional
    public ProfileResponse uploadAvatar(User user, MultipartFile imageFile) {
        fileStorageService.validateImageFile(imageFile);

        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new ProfileNotFoundException(ResponseMessageConstants.NOT_FOUND));

        if (profile.getImageUrl() != null && !profile.getImageUrl().isEmpty()) {
            fileStorageService.deleteFile(profile.getImageUrl());
        }

        String avatarUrl = fileStorageService.saveAvatar(imageFile, user.getId());
        profile.setImageUrl(avatarUrl);
        Profile updatedProfile = profileRepository.save(profile);

        return modelMapper.map(updatedProfile, ProfileResponse.class);
    }

    @Transactional
    public ProfileResponse removeAvatar(User user){
        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new ProfileNotFoundException(ResponseMessageConstants.NOT_FOUND));

        if (profile.getImageUrl() != null && !profile.getImageUrl().isEmpty()) {
            fileStorageService.deleteFile(profile.getImageUrl());
        }

        profile.setImageUrl(null);
        Profile updatedProfile = profileRepository.save(profile);

        return modelMapper.map(updatedProfile, ProfileResponse.class);
    }

    @Transactional
    public ProfileResponse updateProfile(User user, ProfileRequest request) {
        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new ProfileNotFoundException(ResponseMessageConstants.NOT_FOUND));

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

        Profile updatedProfile = profileRepository.save(profile);
        return modelMapper.map(updatedProfile, ProfileResponse.class);
    }

    @Transactional
    public Profile createDefaultProfile(User user) {
        if (profileRepository.findByUser(user).isPresent()) {
            return profileRepository.findByUser(user).get();
        }

        var profile = Profile.builder()
                .user(user)
                .bio("")
                .city(user.getCity())
                .dateOfBirth(null)
                .imageUrl(null)
                .build();

        return profileRepository.save(profile);
    }
}