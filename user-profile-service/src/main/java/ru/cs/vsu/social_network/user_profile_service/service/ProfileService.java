package ru.cs.vsu.social_network.user_profile_service.service;

import ru.cs.vsu.social_network.user_profile_service.dto.request.ProfileEditRequest;
import ru.cs.vsu.social_network.user_profile_service.dto.response.ProfileResponse;

import java.util.UUID;


public interface ProfileService {
    ProfileResponse getProfileByUserId(UUID id);
    ProfileResponse editProfile(UUID id, ProfileEditRequest request);
    ProfileResponse createDefaultProfile(UUID keycloakUserId, String username);
}
