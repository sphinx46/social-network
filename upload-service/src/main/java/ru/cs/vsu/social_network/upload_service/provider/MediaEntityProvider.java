package ru.cs.vsu.social_network.upload_service.provider;

import ru.cs.vsu.social_network.upload_service.entity.MediaEntity;

import java.util.UUID;

/**
 * Провайдер для получения медиа-сущностей из хранилища.
 */
public interface MediaEntityProvider {
    /**
     * Возвращает медиа по идентификатору.
     *
     * @param id идентификатор медиа
     * @return сущность или null
     */
    MediaEntity findByMediaId(UUID id);

    /**
     * Возвращает медиа, принадлежащее конкретному пользователю.
     *
     * @param mediaId идентификатор медиа
     * @param ownerId идентификатор владельца
     * @return найденная сущность
     */
    MediaEntity findOwnedMedia(UUID mediaId, UUID ownerId);
}
