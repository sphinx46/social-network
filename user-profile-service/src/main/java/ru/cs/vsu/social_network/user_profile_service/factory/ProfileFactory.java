package ru.cs.vsu.social_network.user_profile_service.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.user_profile_service.entity.Profile;

import java.util.UUID;

@Slf4j
@Component
public class ProfileFactory {

    /**
     * Создает профиль с настройками по умолчанию.
     *
     * @param keycloakUserId идентификатор пользователя из Keycloak
     * @param username имя пользователя
     * @return профиль с настройками по умолчанию
     */
    public Profile createDefaultProfile(UUID keycloakUserId, String username) {
        log.debug("ПРОФИЛЬ_ФАБРИКА_СОЗДАНИЕ_НАЧАЛО: создание профиля по умолчанию для keycloakUserId: {}, username: {}", keycloakUserId, username);
        
        Profile profile = Profile.builder()
                .keycloakUserId(keycloakUserId)
                .username(username)
                .isOnline(false)
                .avatarUrl(null)
                .bio("")
                .city(null)
                .dateOfBirth(null)
                .build();

        log.debug("ПРОФИЛЬ_ФАБРИКА_СОЗДАНИЕ_УСПЕХ: профиль по умолчанию создан для keycloakUserId: {}", keycloakUserId);
        return profile;
    }
}
