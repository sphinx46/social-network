package ru.vsu.cs.OOP.mordvinovil.task2.social_network.security.config;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.*;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.*;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        configureLikeMappings(modelMapper);
        configurePostMappings(modelMapper);
        configureCommentMappings(modelMapper);
        configureProfileMappings(modelMapper);
        configureRelationshipMappings(modelMapper);
        configureMessagesMappings(modelMapper);
        configurePostWithDetailsMappings(modelMapper);
        configureNewsFeedMappings(modelMapper);

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

    private void configurePostWithDetailsMappings(ModelMapper modelMapper) {
        modelMapper.createTypeMap(Post.class, PostResponse.class, "withDetails")
                .addMappings(mapper -> {
                    mapper.map(Post::getId, PostResponse::setId);
                    mapper.map(src -> src.getUser().getUsername(), PostResponse::setUsername);
                    mapper.map(Post::getContent, PostResponse::setContent);
                    mapper.map(Post::getImageUrl, PostResponse::setImageUrl);
                    mapper.map(Post::getTime, PostResponse::setTime);

                    mapper.using(createCommentsConverter(modelMapper))
                            .map(Post::getComments, PostResponse::setCommentResponseList);
                    mapper.using(createLikesConverter(modelMapper))
                            .map(Post::getLikes, PostResponse::setLikePostResponseList);
                });
    }

    private void configureRelationshipMappings(ModelMapper modelMapper) {
        modelMapper.addMappings(new PropertyMap<Relationship, RelationshipResponse>() {
            @Override
            protected void configure() {
                map().setId(source.getId());
                map().setCreatedAt(source.getCreatedAt());
                map().setSenderId(source.getSender().getId());
                map().setReceiverId(source.getReceiver().getId());
                map().setStatus(source.getStatus());
                map().setUpdatedAt(source.getUpdatedAt());
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

    private void configureMessagesMappings(ModelMapper modelMapper) {
        modelMapper.addMappings(new PropertyMap<Message, MessageResponse>() {
            @Override
            protected void configure() {
                map().setId(source.getId());
                map().setSenderUsername(source.getSender().getUsername());
                map().setReceiverUsername(source.getReceiver().getUsername());
                map().setContent(source.getContent());
                map().setImageUrl(source.getImageUrl());
                map().setStatus(source.getStatus());
                map().setCreatedAt(source.getCreatedAt());
                map().setUpdatedAt(source.getUpdatedAt());
            }
        });
    }

    private void configureNewsFeedMappings(ModelMapper modelMapper) {
        Converter<Post, PostResponse> postToPostResponseConverter = context -> {
            Post post = context.getSource();
            return modelMapper.map(post, PostResponse.class, "withDetails");
        };

        modelMapper.createTypeMap(Post.class, NewsFeedResponse.class, "fullNewsFeed")
                .addMappings(mapper -> {
                    mapper.map(Post::getId, NewsFeedResponse::setId);
                    mapper.map(src -> src.getUser().getUsername(), NewsFeedResponse::setAuthor);

                    mapper.using(postToPostResponseConverter)
                            .map(source -> source, NewsFeedResponse::setPostResponse);
                });
    }

    private Converter<List<Comment>, List<CommentResponse>> createCommentsConverter(ModelMapper modelMapper) {
        return ctx -> ctx.getSource() == null ?
                List.of() :
                ctx.getSource().stream()
                        .map(comment -> modelMapper.map(comment, CommentResponse.class))
                        .collect(Collectors.toList());
    }

    private Converter<List<Like>, List<LikePostResponse>> createLikesConverter(ModelMapper modelMapper) {
        return ctx -> ctx.getSource() == null ?
                List.of() :
                ctx.getSource().stream()
                        .map(like -> modelMapper.map(like, LikePostResponse.class))
                        .collect(Collectors.toList());
    }
}

