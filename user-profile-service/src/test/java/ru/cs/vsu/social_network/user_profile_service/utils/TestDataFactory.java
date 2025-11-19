package ru.cs.vsu.social_network.user_profile_service.utils;

import ru.cs.vsu.social_network.user_profile_service.dto.request.ProfileEditRequest;
import ru.cs.vsu.social_network.user_profile_service.dto.response.ProfileResponse;
import ru.cs.vsu.social_network.user_profile_service.entity.Profile;

import java.util.UUID;

public class TestDataFactory {

    public static Profile createTestProfile(String username, UUID id) {
        Profile profile = new Profile();
        profile.setKeycloakUserId(id);
        profile.setUsername(username);

        return profile;
    }

    public static ProfileResponse createProfileResponse(String username,
                                                        String city,
                                                        String bio) {
        ProfileResponse response = new ProfileResponse();
        response.setUsername(username);

        if (city != null) {
            response.setCity(city);
        }
        if (bio != null) {
            response.setBio(bio);
        }
        return response;
    }

    public static ProfileEditRequest createProfileRequest() {
        return ProfileEditRequest.builder()
                .bio("I am Ilya.")
                .city("Москва")
                .build();
    }
}
