package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.ProfileRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.ProfileResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Profile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;

public interface ProfileService extends Service<ProfileRequest, User, Profile> {
    Profile create(ProfileRequest request, User user);
    ProfileResponse getProfileByUser(User user);
    ProfileResponse getProfileByUserId(Long id);
    ProfileResponse uploadAvatar(User user, MultipartFile imageFile);
    ProfileResponse removeAvatar(User user);
    ProfileResponse updateProfile(User user, ProfileRequest request);
    Profile createDefaultProfile(User user);
}
