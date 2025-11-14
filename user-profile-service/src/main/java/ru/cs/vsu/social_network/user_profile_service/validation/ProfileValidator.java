package ru.cs.vsu.social_network.user_profile_service.validation;

import ru.cs.vsu.social_network.user_profile_service.dto.request.ProfileEditRequest;

import java.util.UUID;

public interface ProfileValidator {
    void validateProfileEdit(UUID targetProfileId, ProfileEditRequest request);
}
