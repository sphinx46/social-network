package ru.cs.vsu.social_network.upload_service.event.publisher;

import ru.cs.vsu.social_network.upload_service.entity.MediaEntity;

/**
 * Абстракция для публикации событий, связанных с медиа.
 */
public interface MediaEventPublisher {

    /**
     * Публикует событие загрузки аватара.
     *
     * @param mediaEntity сущность медиа с информацией об аватаре
     */
    void publishAvatarUploaded(MediaEntity mediaEntity);

    /**
     * Публикует событие загрузки изображения поста.
     *
     * @param mediaEntity сущность медиа с информацией об изображении поста
     */
    void publishPostImageUploaded(MediaEntity mediaEntity);
}