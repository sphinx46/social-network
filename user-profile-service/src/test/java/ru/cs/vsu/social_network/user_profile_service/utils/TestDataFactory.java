package ru.cs.vsu.social_network.user_profile_service.utils;

import ru.cs.vsu.social_network.user_profile_service.dto.request.ProfileEditRequest;
import ru.cs.vsu.social_network.user_profile_service.dto.request.ProfileUploadAvatarRequest;
import ru.cs.vsu.social_network.user_profile_service.dto.response.ProfileResponse;
import ru.cs.vsu.social_network.user_profile_service.entity.Profile;

import java.util.UUID;

/**
 * Фабрика тестовых данных для создания объектов, используемых в модульных и интеграционных тестах.
 * Предоставляет статические методы для создания тестовых экземпляров сущностей, DTO и запросов
 * с предопределенными или настраиваемыми значениями.
 */
public class TestDataFactory {

    /**
     * Создает тестовый профиль с указанными именем пользователя и идентификатором Keycloak.
     *
     * @param username имя пользователя для профиля
     * @param id идентификатор пользователя в Keycloak
     * @return новый экземпляр Profile с установленными базовыми полями
     */
    public static Profile createTestProfile(String username, UUID id) {
        Profile profile = new Profile();
        profile.setKeycloakUserId(id);
        profile.setUsername(username);
        return profile;
    }

    /**
     * Создает тестовый ответ профиля с указанными параметрами.
     * Поля, переданные как null, не устанавливаются в объекте ответа.
     *
     * @param username обязательное имя пользователя
     * @param city город проживания (может быть null)
     * @param bio биография пользователя (может быть null)
     * @param avatarUrl URL аватара пользователя (может быть null)
     * @return новый экземпляр ProfileResponse с установленными полями
     */
    public static ProfileResponse createProfileResponse(String username,
                                                        String city,
                                                        String bio,
                                                        String avatarUrl) {
        ProfileResponse response = new ProfileResponse();
        response.setUsername(username);

        if (city != null) {
            response.setCity(city);
        }
        if (bio != null) {
            response.setBio(bio);
        }
        if (avatarUrl != null) {
            response.setAvatarUrl(avatarUrl);
        }
        return response;
    }

    /**
     * Создает стандартный тестовый запрос на редактирование профиля
     * с предопределенными значениями "I am Ilya." для био и "Москва" для города.
     *
     * @return новый экземпляр ProfileEditRequest с тестовыми данными
     */
    public static ProfileEditRequest createProfileRequest() {
        return ProfileEditRequest.builder()
                .bio("I am Ilya.")
                .city("Москва")
                .build();
    }

    /**
     * Создает тестовый запрос на загрузку аватара со стандартным URL.
     *
     * @return новый экземпляр ProfileUploadAvatarRequest с тестовым URL "testPublicUrl"
     */
    public static ProfileUploadAvatarRequest createUploadAvatarRequest() {
        return ProfileUploadAvatarRequest.builder()
                .publicUrl("testPublicUrl")
                .build();
    }

    /**
     * Создает тестовый запрос на загрузку аватара с указанным URL.
     *
     * @param publicUrl URL аватара для использования в запросе
     * @return новый экземпляр ProfileUploadAvatarRequest с указанным URL
     */
    public static ProfileUploadAvatarRequest createUploadAvatarRequest(String publicUrl) {
        return ProfileUploadAvatarRequest.builder()
                .publicUrl(publicUrl)
                .build();
    }
}