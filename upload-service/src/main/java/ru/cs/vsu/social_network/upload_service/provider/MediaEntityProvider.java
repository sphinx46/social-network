package ru.cs.vsu.social_network.upload_service.provider;

import ru.cs.vsu.social_network.upload_service.entity.MediaEntity;

import java.util.UUID;

public interface MediaEntityProvider {
    MediaEntity findByMediaId(UUID id);
    MediaEntity findOwnedMedia(UUID mediaId, UUID ownerId);
}
