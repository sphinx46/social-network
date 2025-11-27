package ru.cs.vsu.social_network.user_profile_service.service;

import ru.cs.vsu.social_network.user_profile_service.dto.request.ProfileEditRequest;
import ru.cs.vsu.social_network.user_profile_service.dto.request.ProfileUploadAvatarRequest;
import ru.cs.vsu.social_network.user_profile_service.dto.response.ProfileResponse;

import java.util.UUID;

/**
 * Сервис управления профилями пользователей.
 */
public interface ProfileService {
    /**
     * Возвращает профиль по идентификатору пользователя Keycloak.
     *
     * @param id идентификатор пользователя
     * @return данные профиля
     */
    ProfileResponse getProfileByUserId(UUID id);

    /**
     * Обновляет данные профиля.
     *
     * @param id идентификатор пользователя
     * @param request параметры редактирования
     * @return обновлённый профиль
     */
    ProfileResponse editProfile(UUID id, ProfileEditRequest request);

    /**
     * Обновляет ссылку на аватар пользователя.
     *
     * @param keycloakUserId идентификатор пользователя
     * @param request запрос с публичной ссылкой
     * @return профиль с новым аватаром
     */
    ProfileResponse uploadAvatar(UUID keycloakUserId, ProfileUploadAvatarRequest request);

    /**
     * Создаёт профиль с настройками по умолчанию.
     *
     * @param keycloakUserId идентификатор пользователя в Keycloak
     * @param username имя пользователя
     * @return созданный профиль
     */
    ProfileResponse createDefaultProfile(UUID keycloakUserId, String username);
}
