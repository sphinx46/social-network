package ru.vsu.cs.OOP.mordvinovil.task2.social_network.security.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.*;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.*;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        configureLikeMappings(modelMapper);
        configurePostMappings(modelMapper);
        configureCommentMappings(modelMapper);
        configureProfileMappings(modelMapper);

        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setSkipNullEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

        return modelMapper;
    }

    private void configureLikeMappings(ModelMapper modelMapper) {
        modelMapper.addMappings(new PropertyMap<Like, LikePostResponse>() {
            @Override
            protected void configure() {
                map().setId(source.getId());
                map().setUserId(source.getUser().getId());
                map().setUsername(source.getUser().getUsername());
                map().setPostId(source.getPost().getId());
                map().setCreatedAt(source.getCreatedAt());
            }
        });

        modelMapper.addMappings(new PropertyMap<Like, LikeCommentResponse>() {
            @Override
            protected void configure() {
                map().setId(source.getId());
                map().setUserId(source.getUser().getId());
                map().setUsername(source.getUser().getUsername());
                map().setCommentId(source.getComment().getId());
                map().setCreatedAt(source.getCreatedAt());
            }
        });
    }

    private void configurePostMappings(ModelMapper modelMapper) {
        modelMapper.addMappings(new PropertyMap<Post, PostResponse>() {
            @Override
            protected void configure() {
                map().setId(source.getId());
                map().setUsername(source.getUser().getUsername());
                map().setContent(source.getContent());
                map().setImageUrl(source.getImageUrl());
                map().setTime(source.getTime());
            }
        });
    }

    private void configureCommentMappings(ModelMapper modelMapper) {
        modelMapper.addMappings(new PropertyMap<Comment, CommentResponse>() {
            @Override
            protected void configure() {
                map().setId(source.getId());
                map().setUsername(source.getCreator().getUsername());
                map().setContent(source.getContent());
                map().setImageUrl(source.getImageUrl());
                map().setTime(source.getTime());
            }
        });
    }

    private void configureProfileMappings(ModelMapper modelMapper) {
        modelMapper.addMappings(new PropertyMap<Profile, ProfileResponse>() {
            @Override
            protected void configure() {
                map().setUsername(source.getUser().getUsername());
                map().setCity(source.getCity());
                map().setBio(source.getBio());
                map().setImageUrl(source.getImageUrl());
                map().setDateOfBirth(source.getDateOfBirth());
                map().setCreatedAt(source.getUser().getCreatedAt());
                map().setIsOnline(source.getUser().isOnline());
            }
        });
    }
}