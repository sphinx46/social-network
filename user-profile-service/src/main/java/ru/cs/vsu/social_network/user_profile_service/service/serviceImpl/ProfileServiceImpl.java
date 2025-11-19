package ru.cs.vsu.social_network.user_profile_service.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.cs.vsu.social_network.user_profile_service.dto.request.ProfileEditRequest;
import ru.cs.vsu.social_network.user_profile_service.dto.response.ProfileResponse;
import ru.cs.vsu.social_network.user_profile_service.entity.Profile;
import ru.cs.vsu.social_network.user_profile_service.factory.ProfileFactory;
import ru.cs.vsu.social_network.user_profile_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.user_profile_service.provider.ProfileEntityProvider;
import ru.cs.vsu.social_network.user_profile_service.repository.ProfileRepository;
import ru.cs.vsu.social_network.user_profile_service.service.ProfileService;
import ru.cs.vsu.social_network.user_profile_service.validation.ProfileValidator;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    private final ProfileEntityProvider provider;
    private final ProfileFactory factory;
    private final ProfileValidator validator;
    private final ProfileRepository profileRepository;
    private final EntityMapper mapper;

    /**
     * Получает профиль пользователя по идентификатору Keycloak.
     *
     * @param keycloakUserId идентификатор пользователя из Keycloak
     * @return данные профиля пользователя
     */
    @Cacheable(value="profile", key="#keycloakUserId")
    @Override
    public ProfileResponse getProfileByUserId(UUID keycloakUserId) {
        log.info("ПРОФИЛЬ_ПОЛУЧЕНИЕ_НАЧАЛО: запрос профиля пользователя с keycloakUserId: {}", keycloakUserId);

        Profile profile = provider.getByKeycloakUserId(keycloakUserId);
        log.debug("ПРОФИЛЬ_ПОЛУЧЕНИЕ_ДАННЫЕ: найден профиль пользователя {} с именем {}", keycloakUserId, profile.getUsername());

        ProfileResponse response = mapper.map(profile, ProfileResponse.class);
        log.info("ПРОФИЛЬ_ПОЛУЧЕНИЕ_УСПЕХ: профиль пользователя {} успешно получен", keycloakUserId);

        return response;
    }

    /**
     * Редактирует профиль пользователя.
     *
     * @param keycloakUserId идентификатор пользователя из Keycloak
     * @param request данные для обновления профиля
     * @return обновленные данные профиля
     */
    @CacheEvict(value="profile", key="#keycloakUserId")
    @Override
    public ProfileResponse editProfile(UUID keycloakUserId, ProfileEditRequest request) {
        log.info("ПРОФИЛЬ_РЕДАКТИРОВАНИЕ_НАЧАЛО: начало редактирования профиля пользователя {}", keycloakUserId);

        validator.validateProfileEdit(keycloakUserId, request);
        log.debug("ПРОФИЛЬ_РЕДАКТИРОВАНИЕ_ВАЛИДАЦИЯ: валидация данных пройдена для пользователя {}", keycloakUserId);

        Profile profile = provider.getByKeycloakUserId(keycloakUserId);
        log.debug("ПРОФИЛЬ_РЕДАКТИРОВАНИЕ_ДАННЫЕ: текущий профиль - город: {}, длина био: {} символов",
                profile.getCity(), profile.getBio() != null ? profile.getBio().length() : 0);

        updateProfileFromRequest(profile, request);

        Profile updatedProfile = profileRepository.save(profile);
        log.info("ПРОФИЛЬ_РЕДАКТИРОВАНИЕ_УСПЕХ: профиль пользователя {} успешно обновлен", keycloakUserId);

        return mapper.map(updatedProfile, ProfileResponse.class);
    }

    /**
     * Создает профиль пользователя с настройками по умолчанию.
     *
     * @param keycloakUserId идентификатор пользователя из Keycloak
     * @param username имя пользователя
     * @return созданный профиль с настройками по умолчанию
     */
    @Override
    public ProfileResponse createDefaultProfile(UUID keycloakUserId, String username) {
        log.info("ПРОФИЛЬ_СОЗДАНИЕ_НАЧАЛО: создание профиля для keycloakUserId: {}, username: {}", keycloakUserId, username);

        if (profileRepository.existsByKeycloakUserId(keycloakUserId)) {
            log.info("ПРОФИЛЬ_СОЗДАНИЕ_СУЩЕСТВУЕТ: профиль для keycloakUserId {} уже существует, возвращаем существующий", keycloakUserId);
            Profile existingProfile = provider.getByKeycloakUserId(keycloakUserId);
            log.info("ПРОФИЛЬ_СОЗДАНИЕ_ВОЗВРАТ: возвращаем существующий профиль для keycloakUserId {}", keycloakUserId);
            return mapper.map(existingProfile, ProfileResponse.class);
        }

        Profile profile = factory.createDefaultProfile(keycloakUserId, username);
        Profile savedProfile = profileRepository.save(profile);

        log.info("ПРОФИЛЬ_СОЗДАНИЕ_УСПЕХ: профиль создан для keycloakUserId: {}, внутренний ID: {}",
                keycloakUserId, savedProfile.getId());
        return mapper.map(savedProfile, ProfileResponse.class);
    }

    /**
     * Обновляет поля профиля на основе данных из запроса.
     *
     * @param profile сущность профиля для обновления
     * @param request запрос с данными для обновления
     */
    private void updateProfileFromRequest(Profile profile, ProfileEditRequest request) {
        boolean hasChanges = false;

        if (request.getCity() != null && !request.getCity().equals(profile.getCity())) {
            log.debug("ПРОФИЛЬ_ОБНОВЛЕНИЕ_ГОРОД: обновление города с '{}' на '{}'", profile.getCity(), request.getCity());
            profile.setCity(request.getCity());
            hasChanges = true;
        }

        if (request.getDateOfBirth() != null && !request.getDateOfBirth().equals(profile.getDateOfBirth())) {
            log.debug("ПРОФИЛЬ_ОБНОВЛЕНИЕ_ДАТА_РОЖДЕНИЯ: обновление даты рождения с {} на {}",
                    profile.getDateOfBirth(), request.getDateOfBirth());
            profile.setDateOfBirth(request.getDateOfBirth());
            hasChanges = true;
        }

        if (request.getBio() != null && !request.getBio().equals(profile.getBio())) {
            log.debug("ПРОФИЛЬ_ОБНОВЛЕНИЕ_БИО: обновление био, длина: {} символов", request.getBio().length());
            profile.setBio(request.getBio());
            hasChanges = true;
        }

        if (!hasChanges) {
            log.debug("ПРОФИЛЬ_ОБНОВЛЕНИЕ_ИЗМЕНЕНИЙ_НЕТ: изменения в профиле отсутствуют");
        }
    }
}
