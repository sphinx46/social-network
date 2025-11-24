package ru.cs.vsu.social_network.user_profile_service.provider;

import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.user_profile_service.entity.Profile;

import java.util.UUID;

/**
 * Провайдер для безопасного получения сущностей профиля.
 */
@Component
public interface ProfileEntityProvider {
    /**
     * Возвращает профиль по внутреннему идентификатору.
     *
     * @param id идентификатор профиля
     * @return найденная сущность
     */
    Profile getById(UUID id);

    /**
     * Возвращает профиль по идентификатору пользователя Keycloak.
     *
     * @param id идентификатор пользователя в Keycloak
     * @return найденная сущность
     */
    Profile getByKeycloakUserId(UUID id);
}
