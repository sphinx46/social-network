package ru.cs.vsu.social_network.upload_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.cs.vsu.social_network.upload_service.entity.MediaEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MediaRepository extends JpaRepository<MediaEntity, UUID> {
    Optional<MediaEntity> findByIdAndOwnerId(UUID id, UUID ownerId);
    Optional<MediaEntity> findByObjectName(String objectName);
}
