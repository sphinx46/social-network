package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.ProfileRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.response.ProfileResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Profile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.ProfileRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.exceptions.profile.ProfileAlreadyExistsException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.exceptions.profile.ProfileNotFoundException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;


@Service
@RequiredArgsConstructor
public class ProfileService {
    private final FileStorageService fileStorageService;
    private final ProfileRepository profileRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    public Profile save(Profile profile) {
        return profileRepository.save(profile);
    }

    public Profile create(User user, ProfileRequest request) {
        if (profileRepository.findByUser(user).isPresent()) {
            throw new ProfileAlreadyExistsException("Профиль уже существует");

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
                .orElseThrow(() -> new ProfileNotFoundException("Профиль не найден"));

        ProfileResponse response = modelMapper.map(profile, ProfileResponse.class);

        response.setUsername(user.getUsername());
        response.setAge(calculateAge(profile.getDateOfBirth()));
        response.setIsOnline(user.isOnline());
        response.setCreatedAt(user.getCreatedAt());

        return response;
    }

    public ProfileResponse getProfileByUserId(Long id) {
        User user = userService.getById(id);
        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new ProfileNotFoundException("Профиль с таким user id не найден"));

        ProfileResponse response = modelMapper.map(profile, ProfileResponse.class);

        response.setUsername(user.getUsername());
        response.setAge(calculateAge(profile.getDateOfBirth()));
        response.setIsOnline(user.isOnline());
        response.setCreatedAt(user.getCreatedAt());

        return response;
    }

    private Integer calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return null;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }


    public ProfileResponse uploadAvatar(User user, MultipartFile imageFile) throws FileUploadException {
        try {
            fileStorageService.validateImageFile(imageFile);

            Profile profile = profileRepository.findByUser(user)
                    .orElseThrow(() -> new ProfileNotFoundException("Профиль не найден"));

            if (profile.getImageUrl() != null && !profile.getImageUrl().isEmpty()) {
                fileStorageService.deleteFile(profile.getImageUrl());
            }

            String avatarUrl = fileStorageService.saveFile(imageFile, "avatars");

            profile.setImageUrl(avatarUrl);
            Profile updatedProfile = profileRepository.save(profile);

            ProfileResponse response = modelMapper.map(updatedProfile, ProfileResponse.class);

            return response;
        } catch (IOException e) {
            throw new FileUploadException("Ошибка при сохранении файла", e);
        }
    }

    @Transactional
    public ProfileResponse removeAvatar(User user){
        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new ProfileNotFoundException("Профиль не найден"));

        if (profile.getImageUrl() != null && !profile.getImageUrl().isEmpty()) {
            fileStorageService.deleteFile(profile.getImageUrl());
        }

        profile.setImageUrl(null);
        Profile updatedProfile = profileRepository.save(profile);

        ProfileResponse response = modelMapper.map(updatedProfile, ProfileResponse.class);
        return response;
    }

    @Transactional
    public ProfileResponse updateProfile(User user, ProfileRequest request) {
        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new ProfileNotFoundException("Профиль не найден"));

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
        ProfileResponse response = modelMapper.map(updatedProfile, ProfileResponse.class);
        return response;
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
