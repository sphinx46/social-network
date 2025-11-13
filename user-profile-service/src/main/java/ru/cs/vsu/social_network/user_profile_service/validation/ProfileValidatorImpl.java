package ru.cs.vsu.social_network.user_profile_service.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.user_profile_service.dto.request.ProfileEditRequest;
import ru.cs.vsu.social_network.user_profile_service.exceptions.profile.ProfileBioTooLongException;
import ru.cs.vsu.social_network.user_profile_service.exceptions.profile.ProfileCityTooLongException;
import ru.cs.vsu.social_network.user_profile_service.provider.ProfileEntityProvider;
import ru.cs.vsu.social_network.user_profile_service.utils.constants.MessageConstants;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProfileValidatorImpl implements ProfileValidator {
    private final ProfileEntityProvider provider;

    /**
     * Валидирует данные для редактирования профиля
     *
     * @param keycloakUserId идентификатор пользователя из Keycloak
     * @param request запрос на редактирование
     */
    @Override
    public void validateProfileEdit(UUID keycloakUserId, ProfileEditRequest request) {
        log.info("ПРОФИЛЬ_ВАЛИДАЦИЯ_НАЧАЛО: валидация редактирования профиля с keycloakUserId: {}", keycloakUserId);

        provider.getByKeycloakUserId(keycloakUserId);

        if (request.getBio() != null && request.getBio().length() > 500) {
            log.warn("ПРОФИЛЬ_ВАЛИДАЦИЯ_БИО_ОШИБКА: длина био превышает допустимую - {} символов", request.getBio().length());
            throw new ProfileBioTooLongException(MessageConstants.FAILURE_PROFILE_BIO_TOO_LONG);
        }

        if (request.getCity() != null && request.getCity().length() > 100) {
            log.warn("ПРОФИЛЬ_ВАЛИДАЦИЯ_ГОРОД_ОШИБКА: длина названия города превышает допустимую - {} символов", request.getCity().length());
            throw new ProfileCityTooLongException(MessageConstants.FAILURE_PROFILE_CITY_TOO_LONG);
        }

        log.info("ПРОФИЛЬ_ВАЛИДАЦИЯ_УСПЕХ: валидация профиля {} пройдена успешно", keycloakUserId);
    }
}