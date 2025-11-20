package ru.cs.vsu.social_network.user_profile_service.validation;

import ru.cs.vsu.social_network.user_profile_service.dto.request.ProfileEditRequest;

import java.util.UUID;

/**
 * Интерфейс для валидации данных профиля.
 */
public interface ProfileValidator {
    /**
     * Валидирует данные для редактирования профиля.
     *
     * @param targetProfileId идентификатор профиля
     * @param request запрос на редактирование
     */
    void validateProfileEdit(UUID targetProfileId, ProfileEditRequest request);
}
