package ru.cs.vsu.social_network.upload_service.provider.providerImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.upload_service.entity.MediaEntity;
import ru.cs.vsu.social_network.upload_service.exception.AccessDeniedException;
import ru.cs.vsu.social_network.upload_service.exception.MediaNotFoundException;
import ru.cs.vsu.social_network.upload_service.provider.MediaEntityProvider;
import ru.cs.vsu.social_network.upload_service.repository.MediaRepository;
import ru.cs.vsu.social_network.upload_service.utils.MessageConstants;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public final class MediaEntityProviderImpl implements MediaEntityProvider {

    private final MediaRepository repository;

    /**
     * Возвращает медиа по идентификатору.
     *
     * @param id идентификатор медиа
     * @return найденная сущность
     */
    @Override
    public MediaEntity findByMediaId(final UUID id) {
        log.debug("МЕДИА_ПОИСК_ID_НАЧАЛО: {}", id);
        MediaEntity media = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("МЕДИА_ПОИСК_ID_ОШИБКА: медиа {} не найдено", id);
                    return new MediaNotFoundException(MessageConstants.MEDIA_NOT_FOUND);
                });
        log.debug("МЕДИА_ПОИСК_ID_УСПЕХ: {}", id);
        return media;
    }

    /**
     * Возвращает медиа, принадлежащее конкретному владельцу.
     *
     * @param mediaId идентификатор медиа
     * @param ownerId идентификатор владельца
     * @return найденная сущность
     */
    @Override
    public MediaEntity findOwnedMedia(final UUID mediaId, final UUID ownerId) {
        log.debug("МЕДИА_ПОИСК_ВЛАДЕЛЕЦ_НАЧАЛО: mediaId={}, ownerId={}", mediaId, ownerId);
        return repository.findByIdAndOwnerId(mediaId, ownerId)
                .orElseThrow(() -> {
                    log.error("МЕДИА_ПОИСК_ВЛАДЕЛЕЦ_ОШИБКА: mediaId={} недоступно для ownerId={}",
                            mediaId, ownerId);
                    return new AccessDeniedException(MessageConstants.ACCESS_DENIED_EXCEPTION);
                });
    }
}
