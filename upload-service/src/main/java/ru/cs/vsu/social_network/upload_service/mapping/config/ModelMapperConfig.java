package ru.cs.vsu.social_network.upload_service.mapping.config;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.cs.vsu.social_network.upload_service.dto.response.MediaMetadataResponse;
import ru.cs.vsu.social_network.upload_service.dto.response.MediaResponse;
import ru.cs.vsu.social_network.upload_service.entity.MediaEntity;

@Slf4j
@Configuration
public class ModelMapperConfig {

    /**
     * Создает и настраивает ModelMapper для преобразования сущностей в DTO.
     *
     * @return настроенный ModelMapper
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        configureMediaEntityMappings(modelMapper);
        configureMediaEntityWithMetadataMappings(modelMapper);

        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setSkipNullEnabled(true)
                .setFieldAccessLevel(
                        org.modelmapper.config.Configuration
                                .AccessLevel.PRIVATE);

        return modelMapper;
    }

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
}
