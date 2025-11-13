package ru.cs.vsu.social_network.user_profile_service.provider;

import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.user_profile_service.entity.Profile;

import java.util.UUID;

@Component
public interface ProfileEntityProvider {
    Profile getById(UUID id);
    Profile getByKeycloakUserId(UUID id);
}
