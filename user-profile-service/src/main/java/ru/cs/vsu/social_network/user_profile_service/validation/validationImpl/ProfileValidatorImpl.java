package ru.cs.vsu.social_network.user_profile_service.validation.validationImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.user_profile_service.dto.request.ProfileEditRequest;
import ru.cs.vsu.social_network.user_profile_service.exceptions.profile.ProfileBioTooLongException;
import ru.cs.vsu.social_network.user_profile_service.exceptions.profile.ProfileCityTooLongException;
import ru.cs.vsu.social_network.user_profile_service.provider.ProfileEntityProvider;
import ru.cs.vsu.social_network.user_profile_service.utils.constants.MessageConstants;
import ru.cs.vsu.social_network.user_profile_service.validation.ProfileValidator;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileValidatorImpl implements ProfileValidator {
    private final ProfileEntityProvider provider;

    /**
     * Максимальная длина описания профиля.
     */
    private static final int MAX_BIO_LENGTH = 500;

    /**
     * Максимальная длина названия города.
     */
    private static final int MAX_CITY_LENGTH = 100;

    /**
     * Валидирует данные для редактирования профиля.
     *
     * @param keycloakUserId идентификатор пользователя из Keycloak
     * @param request запрос на редактирование
     * @throws ProfileBioTooLongException если длина био превышает 500
     *                                     символов
     * @throws ProfileCityTooLongException если длина названия города
     *                                     превышает 100 символов
     */
    @Override
    public void validateProfileEdit(
            final UUID keycloakUserId,
            final ProfileEditRequest request) {
        log.info("ПРОФИЛЬ_ВАЛИДАЦИЯ_НАЧАЛО: "
                + "валидация редактирования профиля с keycloakUserId: {}",
                keycloakUserId);

        provider.getByKeycloakUserId(keycloakUserId);

        if (request.getBio() != null
                && request.getBio().length() > MAX_BIO_LENGTH) {
            log.warn("ПРОФИЛЬ_ВАЛИДАЦИЯ_БИО_ОШИБКА: "
                    + "длина био превышает допустимую - {} символов",
                    request.getBio().length());
            throw new ProfileBioTooLongException(
                    MessageConstants.FAILURE_PROFILE_BIO_TOO_LONG);
        }

        if (request.getCity() != null
                && request.getCity().length() > MAX_CITY_LENGTH) {
            log.warn("ПРОФИЛЬ_ВАЛИДАЦИЯ_ГОРОД_ОШИБКА: "
                    + "длина названия города превышает допустимую - {} "
                    + "символов", request.getCity().length());
            throw new ProfileCityTooLongException(
                    MessageConstants.FAILURE_PROFILE_CITY_TOO_LONG);
        }

        log.info("ПРОФИЛЬ_ВАЛИДАЦИЯ_УСПЕХ: "
                + "валидация профиля {} пройдена успешно", keycloakUserId);
    }
}
