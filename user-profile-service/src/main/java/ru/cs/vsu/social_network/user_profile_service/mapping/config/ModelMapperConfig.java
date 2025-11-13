package ru.cs.vsu.social_network.user_profile_service.mapping.config;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.cs.vsu.social_network.user_profile_service.dto.response.ProfileResponse;
import ru.cs.vsu.social_network.user_profile_service.entity.Profile;

import java.time.LocalDate;
import java.time.Period;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        configureProfileMappings(modelMapper);

        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setSkipNullEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

        return modelMapper;
    }


    private void configureProfileMappings(ModelMapper modelMapper) {
        Converter<Profile, Integer> ageConverter = context -> {
            Profile profile = context.getSource();
            LocalDate dateOfBirth = profile.getDateOfBirth();
            return dateOfBirth != null ?
                    Period.between(dateOfBirth, LocalDate.now()).getYears() : null;
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
    }
}