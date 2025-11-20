package ru.cs.vsu.social_network.user_profile_service.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.cs.vsu.social_network.user_profile_service.entity.Profile;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    /**
     * Проверяет существование профиля по Keycloak ID.
     *
     * @param id Keycloak ID пользователя
     * @return true, если профиль существует
     */
    boolean existsByKeycloakUserId(UUID id);

    /**
     * Находит профиль по Keycloak ID.
     *
     * @param keycloakUserId Keycloak ID пользователя
     * @return Optional с профилем, если найден
     */
    Optional<Profile> findByKeycloakUserId(UUID keycloakUserId);
}
