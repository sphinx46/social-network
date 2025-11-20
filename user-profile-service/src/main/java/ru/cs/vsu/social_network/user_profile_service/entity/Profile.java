package ru.cs.vsu.social_network.user_profile_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "profile")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Profile extends BaseEntity {
    private static final int MAX_BIO_LENGTH = 500;

    @Column(name = "keycloak_user_id", unique = true, nullable = false)
    private UUID keycloakUserId;

    @Column(name = "username")
    private String username;

    @Column(length = MAX_BIO_LENGTH)
    private String bio;

    @Column(name = "profile_avatar_url")
    private String avatarUrl;

    @Column(name = "city")
    private String city;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "is_online")
    private boolean isOnline;
}
