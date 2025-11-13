package ru.cs.vsu.social_network.user_profile_service.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.user_profile_service.entity.Profile;
import ru.cs.vsu.social_network.user_profile_service.exceptions.profile.ProfileNotFoundException;
import ru.cs.vsu.social_network.user_profile_service.repository.ProfileRepository;
import ru.cs.vsu.social_network.user_profile_service.utils.constants.MessageConstants;

import java.util.UUID;

@Slf4j
@Component
public class ProfileEntityProviderImpl implements ProfileEntityProvider {
    private final ProfileRepository profileRepository;

    public ProfileEntityProviderImpl(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    /**
     * Получает профиль по внутреннему идентификатору
     *
     * @param id внутренний идентификатор профиля
     * @return найденный профиль
     * @throws ProfileNotFoundException если профиль не найден
     */
    @Override
    public Profile getById(UUID id) {
        log.info("ПРОФИЛЬ_ПОИСК_ПО_ID_НАЧАЛО: поиск профиля по внутреннему ID: {}", id);

        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("ПРОФИЛЬ_ПОИСК_ПО_ID_ОШИБКА: профиль с внутренним ID {} не найден", id);
                    return new ProfileNotFoundException(MessageConstants.FAILURE_PROFILE_NOT_FOUND);
                });

        log.info("ПРОФИЛЬ_ПОИСК_ПО_ID_УСПЕХ: профиль с внутренним ID {} найден", id);
        return profile;
    }

    /**
     * Получает профиль по идентификатору Keycloak
     *
     * @param keycloakUserId идентификатор пользователя из Keycloak
     * @return найденный профиль
     * @throws ProfileNotFoundException если профиль не найден
     */
    public Profile getByKeycloakUserId(UUID keycloakUserId) {
        log.info("ПРОФИЛЬ_ПОИСК_ПО_KEYCLOAK_ID_НАЧАЛО: поиск профиля по keycloakUserId: {}", keycloakUserId);

        Profile profile = profileRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> {
                    log.error("ПРОФИЛЬ_ПОИСК_ПО_KEYCLOAK_ID_ОШИБКА: профиль с keycloakUserId {} не найден", keycloakUserId);
                    return new ProfileNotFoundException(MessageConstants.FAILURE_PROFILE_NOT_FOUND);
                });

        log.info("ПРОФИЛЬ_ПОИСК_ПО_KEYCLOAK_ID_УСПЕХ: профиль с keycloakUserId {} найден", keycloakUserId);
        return profile;
    }
}