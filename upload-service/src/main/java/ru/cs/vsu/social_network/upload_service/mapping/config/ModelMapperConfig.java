package ru.cs.vsu.social_network.upload_service.mapping.config;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.cs.vsu.social_network.upload_service.dto.response.MediaMetadataResponse;
import ru.cs.vsu.social_network.upload_service.dto.response.MediaResponse;
import ru.cs.vsu.social_network.upload_service.entity.MediaEntity;
import ru.cs.vsu.social_network.upload_service.event.AvatarUploadedEvent;
import ru.cs.vsu.social_network.upload_service.event.CommentImageUploadedEvent;
import ru.cs.vsu.social_network.upload_service.event.MessageImageUploadedEvent;
import ru.cs.vsu.social_network.upload_service.event.PostImageUploadedEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Конфигурационный класс для настройки ModelMapper.
 * Определяет правила маппинга между сущностями, DTO и событиями.
 * Обеспечивает корректное преобразование данных между слоями приложения.
 *
 */
@Slf4j
@Configuration
public class ModelMapperConfig {

    /**
     * Создает и настраивает ModelMapper для преобразования сущностей в DTO и события.
     * Включает сопоставление полей, пропуск null значений и доступ к приватным полям.
     *
     * @return настроенный экземпляр ModelMapper с определенными правилами маппинга
     */
    @Bean
    public ModelMapper modelMapper() {
        log.info("MODEL_MAPPER_КОНФИГУРАЦИЯ_НАЧАЛО");

        final ModelMapper modelMapper = new ModelMapper();

        configureMediaEntityMappings(modelMapper);
        configureMediaEntityWithMetadataMappings(modelMapper);
        configureAvatarUploadedEventWithMappings(modelMapper);
        configurePostImageUploadedEventWithMappings(modelMapper);
        configureCommentImageUploadedEventWithMappings(modelMapper);
        configureMessageImageUploadedEventWithMappings(modelMapper);

        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setSkipNullEnabled(true)
                .setFieldAccessLevel(
                        org.modelmapper.config.Configuration
                                .AccessLevel.PRIVATE);

        log.info("MODEL_MAPPER_КОНФИГУРАЦИЯ_УСПЕХ");
        return modelMapper;
    }

    /**
     * Настраивает маппинг между MediaEntity и MediaResponse.
     * Определяет соответствие полей сущности и DTO ответа.
     *
     * @param modelMapper экземпляр ModelMapper для настройки
     */
    private void configureMediaEntityMappings(final ModelMapper modelMapper) {
        modelMapper.addMappings(new PropertyMap<MediaEntity, MediaResponse>() {
            @Override
            protected void configure() {
                map().setOwnerId(source.getOwnerId());
                map().setCategory(source.getCategory());
                map().setMimeType(source.getMimeType());
                map().setSize(source.getSize());
                map().setPublicUrl(source.getPublicUrl());
                map().setId(source.getId());
                map().setObjectName(source.getObjectName());
                map().setDescription(source.getDescription());
                map().setOriginalFileName(source.getOriginalFileName());
            }
        });
    }

    /**
     * Настраивает маппинг между MediaEntity и MediaMetadataResponse.
     * Определяет соответствие полей для ответа с метаданными.
     *
     * @param modelMapper экземпляр ModelMapper для настройки
     */
    private void configureMediaEntityWithMetadataMappings(final ModelMapper modelMapper) {
        modelMapper.addMappings(new PropertyMap<MediaEntity, MediaMetadataResponse>() {
            @Override
            protected void configure() {
                map().setCategory(source.getCategory());
                map().setMimeType(source.getMimeType());
                map().setSize(source.getSize());
                map().setPublicUrl(source.getPublicUrl());
                map().setObjectName(source.getObjectName());
                map().setBucketName(source.getBucketName());
                map().setOriginalFileName(source.getOriginalFileName());
                map().setDescription(source.getDescription());
                map().setMediaId(source.getId());
                map().setCreatedAt(source.getCreatedAt());
                map().setUpdatedAt(source.getUpdatedAt());
                map().setOwnerId(source.getOwnerId());
            }
        });
    }

    /**
     * Настраивает маппинг между MediaEntity и AvatarUploadedEvent.
     * Автоматически генерирует eventId и eventTimeStamp для события.
     *
     * @param modelMapper экземпляр ModelMapper для настройки
     */
    private void configureAvatarUploadedEventWithMappings(final ModelMapper modelMapper) {
        modelMapper.addMappings(new PropertyMap<MediaEntity, AvatarUploadedEvent>() {
            @Override
            protected void configure() {
                map().setEventId(UUID.randomUUID());
                map().setEventTimeStamp(LocalDateTime.now());
                map().setOwnerId(source.getOwnerId());
                map().setPublicUrl(source.getPublicUrl());
                map().setObjectName(source.getObjectName());
                map().setMimeType(source.getMimeType());
                map().setSize(source.getSize());
                map().setDescription(source.getDescription());
                map().setOriginalFileName(source.getOriginalFileName());
            }
        });
    }

    /**
     * Настраивает маппинг между MediaEntity и PostImageUploadedEvent.
     * Автоматически генерирует eventId и eventTimeStamp для события.
     *
     * @param modelMapper экземпляр ModelMapper для настройки
     */
    private void configurePostImageUploadedEventWithMappings(final ModelMapper modelMapper) {
        modelMapper.addMappings(new PropertyMap<MediaEntity, PostImageUploadedEvent>() {
            @Override
            protected void configure() {
                map().setEventId(UUID.randomUUID());
                map().setEventTimeStamp(LocalDateTime.now());
                map().setOwnerId(source.getOwnerId());
                map().setPublicUrl(source.getPublicUrl());
                map().setObjectName(source.getObjectName());
                map().setMimeType(source.getMimeType());
                map().setSize(source.getSize());
                map().setDescription(source.getDescription());
                map().setOriginalFileName(source.getOriginalFileName());
            }
        });
    }

    /**
     * Настраивает маппинг между MediaEntity и CommentImageUploadedEvent.
     * Автоматически генерирует eventId и eventTimeStamp для события.
     * Определяет соответствие полей сущности медиа и события загрузки изображения комментария.
     *
     * @param modelMapper экземпляр ModelMapper для настройки
     */
    private void configureCommentImageUploadedEventWithMappings(final ModelMapper modelMapper) {
        modelMapper.addMappings(new PropertyMap<MediaEntity, CommentImageUploadedEvent>() {
            @Override
            protected void configure() {
                map().setEventId(UUID.randomUUID());
                map().setEventTimeStamp(LocalDateTime.now());
                map().setOwnerId(source.getOwnerId());
                map().setPublicUrl(source.getPublicUrl());
                map().setObjectName(source.getObjectName());
                map().setMimeType(source.getMimeType());
                map().setSize(source.getSize());
                map().setDescription(source.getDescription());
                map().setOriginalFileName(source.getOriginalFileName());
            }
        });
    }

    /**
     * Настраивает маппинг между MediaEntity и MessageImageUploadedEvent.
     * Автоматически генерирует eventId и eventTimeStamp для события.
     * Определяет соответствие полей сущности медиа и события загрузки изображения сообщения.
     *
     * @param modelMapper экземпляр ModelMapper для настройки
     */
    private void configureMessageImageUploadedEventWithMappings(final ModelMapper modelMapper) {
        modelMapper.addMappings(new PropertyMap<MediaEntity, MessageImageUploadedEvent>() {
            @Override
            protected void configure() {
                map().setEventId(UUID.randomUUID());
                map().setEventTimeStamp(LocalDateTime.now());
                map().setOwnerId(source.getOwnerId());
                map().setPublicUrl(source.getPublicUrl());
                map().setObjectName(source.getObjectName());
                map().setMimeType(source.getMimeType());
                map().setSize(source.getSize());
                map().setDescription(source.getDescription());
                map().setOriginalFileName(source.getOriginalFileName());
            }
        });
    }
}