package ru.cs.vsu.social_network.contents_service.mapping.config;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.cs.vsu.social_network.contents_service.dto.response.content.*;
import ru.cs.vsu.social_network.contents_service.entity.Comment;
import ru.cs.vsu.social_network.contents_service.entity.LikeComment;
import ru.cs.vsu.social_network.contents_service.entity.LikePost;
import ru.cs.vsu.social_network.contents_service.entity.Post;

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

        configurePostMappings(modelMapper);
        configureCommentMappings(modelMapper);
        configureLikeCommentMappings(modelMapper);
        configureLikePostMappings(modelMapper);
        configurePostDetailsMappings(modelMapper);
        configureCommentDetailsMappings(modelMapper);

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
     * Настраивает маппинг между сущностью Post и PostResponse.
     * Определяет правила преобразования полей поста в DTO ответа.
     *
     * @param modelMapper экземпляр ModelMapper для настройки
     */
    private void configurePostMappings(final ModelMapper modelMapper) {
        modelMapper.addMappings(new PropertyMap<Post, PostResponse>() {
            @Override
            protected void configure() {
                map().setId(source.getId());
                map().setOwnerId(source.getOwnerId());
                map().setContent(source.getContent());
                map().setImageUrl(source.getImageUrl());
                map().setCreatedAt(source.getCreatedAt());
                map().setUpdatedAt(source.getUpdatedAt());
            }
        });
    }

    /**
     * Настраивает маппинг между сущностью Comment и CommentResponse.
     * Определяет правила преобразования полей комментария в DTO ответа,
     * включая извлечение ID связанного поста.
     *
     * @param modelMapper экземпляр ModelMapper для настройки
     */
    private void configureCommentMappings(final ModelMapper modelMapper) {
        modelMapper.addMappings(new PropertyMap<Comment, CommentResponse>() {
            @Override
            protected void configure() {
                map().setId(source.getId());
                map().setOwnerId(source.getOwnerId());
                map().setPostId(source.getPost().getId());
                map().setContent(source.getContent());
                map().setImageUrl(source.getImageUrl());
                map().setCreatedAt(source.getCreatedAt());
                map().setUpdatedAt(source.getUpdatedAt());
            }
        });
    }

    /**
     * Настраивает маппинг между сущностью LikeComment и LikeCommentResponse.
     * Определяет правила преобразования полей лайка комментария в DTO ответа,
     * включая извлечение ID связанного комментария.
     *
     * @param modelMapper экземпляр ModelMapper для настройки
     */
    private void configureLikeCommentMappings(final ModelMapper modelMapper) {
        modelMapper.addMappings(new PropertyMap<LikeComment, LikeCommentResponse>() {
            @Override
            protected void configure() {
                map().setId(source.getId());
                map().setOwnerId(source.getOwnerId());
                map().setCommentId(source.getComment().getId());
                map().setCreatedAt(source.getCreatedAt());
            }
        });
    }

    /**
     * Настраивает маппинг между сущностью LikePost и LikePostResponse.
     * Определяет правила преобразования полей лайка поста в DTO ответа,
     * включая извлечение ID связанного поста.
     *
     * @param modelMapper экземпляр ModelMapper для настройки
     */
    private void configureLikePostMappings(final ModelMapper modelMapper) {
        modelMapper.addMappings(new PropertyMap<LikePost, LikePostResponse>() {
            @Override
            protected void configure() {
                map().setId(source.getId());
                map().setOwnerId(source.getOwnerId());
                map().setPostId(source.getPost().getId());
                map().setCreatedAt(source.getCreatedAt());
            }
        });
    }

    /**
     * Настраивает маппинг между сущностью Post и PostDetailsResponse.
     * Определяет правила преобразования полей поста в расширенный DTO ответа
     * с детальной информацией.
     *
     * @param modelMapper экземпляр ModelMapper для настройки
     */
    private void configurePostDetailsMappings(final ModelMapper modelMapper) {
        modelMapper.addMappings(new PropertyMap<Post, PostDetailsResponse>() {
            @Override
            protected void configure() {
                map().setId(source.getId());
                map().setOwnerId(source.getOwnerId());
                map().setContent(source.getContent());
                map().setImageUrl(source.getImageUrl());
                map().setCreatedAt(source.getCreatedAt());
                map().setUpdatedAt(source.getUpdatedAt());
            }
        });
    }

    /**
     * Настраивает маппинг между CommentResponse и CommentDetailsResponse.
     * Определяет правила преобразования базового DTO комментария в расширенный DTO
     * с детальной информацией о лайках и другой связанной информацией.
     *
     * @param modelMapper экземпляр ModelMapper для настройки
     */
    private void configureCommentDetailsMappings(final ModelMapper modelMapper) {
        modelMapper.addMappings(new PropertyMap<CommentResponse, CommentDetailsResponse>() {
            @Override
            protected void configure() {
                map().setId(source.getId());
                map().setOwnerId(source.getOwnerId());
                map().setContent(source.getContent());
                map().setImageUrl(source.getImageUrl());
                map().setCreatedAt(source.getCreatedAt());
                map().setUpdatedAt(source.getUpdatedAt());
            }
        });
    }
}