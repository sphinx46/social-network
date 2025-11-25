package ru.cs.vsu.social_network.user_profile_service.mapping.config;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.cs.vsu.social_network.user_profile_service.dto.request.ProfileUploadAvatarRequest;
import ru.cs.vsu.social_network.user_profile_service.dto.response.ProfileResponse;
import ru.cs.vsu.social_network.user_profile_service.entity.Profile;
import ru.cs.vsu.social_network.user_profile_service.events.AvatarUploadedEvent;

import java.time.LocalDate;
import java.time.Period;

@Slf4j
@Configuration
public class ModelMapperConfig {

    /**
     * Создает и настраивает экземпляр ModelMapper с предопределенными маппингами.
     * Настраивает преобразования для профилей, событий и DTO.
     *
     * @return настроенный экземпляр ModelMapper
     */
    @Bean
    public ModelMapper modelMapper() {
        log.info("MODEL_MAPPER_НАСТРОЙКА_НАЧАЛО: инициализация маппера");

        final ModelMapper modelMapper = new ModelMapper();
        configureProfileMappings(modelMapper);
        configureEventMappings(modelMapper);

        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setSkipNullEnabled(true)
                .setFieldAccessLevel(
                        org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

        log.info("MODEL_MAPPER_НАСТРОЙКА_УСПЕХ: маппер успешно настроен");
        return modelMapper;
    }

    /**
     * Настраивает маппинги для преобразования Profile в ProfileResponse.
     * Включает вычисление возраста на основе даты рождения.
     *
     * @param modelMapper ModelMapper для настройки
     */
    private void configureProfileMappings(final ModelMapper modelMapper) {
        final Converter<Profile, Integer> ageConverter = context -> {
            final Profile profile = context.getSource();
            final LocalDate dateOfBirth = profile.getDateOfBirth();
            return dateOfBirth != null
                    ? Period.between(dateOfBirth, LocalDate.now()).getYears()
                    : null;
        };

        modelMapper.typeMap(Profile.class, ProfileResponse.class)
                .addMappings(mapper -> {
                    mapper.map(Profile::getUsername, ProfileResponse::setUsername);
                    mapper.map(Profile::getCity, ProfileResponse::setCity);
                    mapper.map(Profile::getBio, ProfileResponse::setBio);
                    mapper.using(ageConverter).map(src -> src, ProfileResponse::setAge);
                    mapper.map(Profile::getAvatarUrl, ProfileResponse::setAvatarUrl);
                    mapper.map(Profile::getDateOfBirth, ProfileResponse::setDateOfBirth);
                    mapper.map(Profile::getCreatedAt, ProfileResponse::setCreatedAt);
                    mapper.map(Profile::getUpdatedAt, ProfileResponse::setUpdatedAt);
                    mapper.map(Profile::isOnline, ProfileResponse::setIsOnline);
                });

        log.debug("MODEL_MAPPER_ПРОФИЛЬ_МАППИНГИ_НАСТРОЕНЫ: настройки для Profile -> ProfileResponse применены");
    }

    /**
     * Настраивает маппинги для преобразования событий в DTO.
     * Определяет правила преобразования AvatarUploadedEvent в ProfileUploadAvatarRequest.
     *
     * @param modelMapper ModelMapper для настройки
     */
    private void configureEventMappings(final ModelMapper modelMapper) {
        modelMapper.addMappings(new PropertyMap<AvatarUploadedEvent, ProfileUploadAvatarRequest>() {
            @Override
            protected void configure() {
                map().setPublicUrl(source.getPublicUrl());
            }
        });

        log.debug("MODEL_MAPPER_СОБЫТИЯ_МАППИНГИ_НАСТРОЕНЫ: настройки для событий применены");
    }
}