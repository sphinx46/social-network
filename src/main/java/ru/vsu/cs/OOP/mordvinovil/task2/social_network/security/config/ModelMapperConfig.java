package ru.vsu.cs.OOP.mordvinovil.task2.social_network.security.config;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.*;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.*;

import java.util.List;
import java.util.Set;
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
        configurePostWithCollectionsMappings(modelMapper);
        configureNewsFeedMappings(modelMapper);
        configureNotificationMappings(modelMapper);

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
                map().setTime(source.getCreatedAt());
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

    private void configureNotificationMappings(ModelMapper modelMapper) {
        modelMapper.addMappings(new PropertyMap<Notification, NotificationResponse>() {
            @Override
            protected void configure() {
                map().setId(source.getId());
                map().setType(source.getType());
                map().setStatus(source.getStatus());
                map().setAdditionalData(source.getAdditionalData());
                map().setCreatedAt(source.getCreatedAt());
                map().setUpdatedAt(source.getUpdatedAt());
            }
        });
    }

    private void configureNewsFeedMappings(ModelMapper modelMapper) {
        Converter<Post, PostResponse> postToPostResponseConverter = context -> {
            Post post = context.getSource();
            PostResponse postResponse = new PostResponse();

            postResponse.setId(post.getId());
            postResponse.setUsername(post.getUser().getUsername());
            postResponse.setContent(post.getContent());
            postResponse.setImageUrl(post.getImageUrl());
            postResponse.setTime(post.getCreatedAt());

            if (post.getComments() != null) {
                List<CommentResponse> commentResponses = post.getComments().stream()
                        .map(comment -> modelMapper.map(comment, CommentResponse.class))
                        .collect(Collectors.toList());
                postResponse.setCommentResponseList(commentResponses);
            } else {
                postResponse.setCommentResponseList(List.of());
            }

            if (post.getLikes() != null) {
                List<LikePostResponse> likeResponses = post.getLikes().stream()
                        .map(like -> modelMapper.map(like, LikePostResponse.class))
                        .collect(Collectors.toList());
                postResponse.setLikePostResponseList(likeResponses);
            } else {
                postResponse.setLikePostResponseList(List.of());
            }

            return postResponse;
        };

        modelMapper.createTypeMap(Post.class, NewsFeedResponse.class, "fullNewsFeed")
                .addMappings(mapper -> {
                    mapper.map(Post::getId, NewsFeedResponse::setId);
                    mapper.map(src -> src.getUser().getUsername(), NewsFeedResponse::setAuthor);
                    mapper.using(postToPostResponseConverter).map(source -> source, NewsFeedResponse::setPostResponse);
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
                map().setTime(source.getCreatedAt());

                using(createCommentsSetConverter(modelMapper)).map(source.getComments(), destination.getCommentResponseList());
                using(createLikesSetConverter(modelMapper)).map(source.getLikes(), destination.getLikePostResponseList());
            }
        });

        modelMapper.createTypeMap(Post.class, PostResponse.class, "withDetails")
                .addMappings(mapper -> {
                    mapper.map(Post::getId, PostResponse::setId);
                    mapper.map(src -> src.getUser().getUsername(), PostResponse::setUsername);
                    mapper.map(Post::getContent, PostResponse::setContent);
                    mapper.map(Post::getImageUrl, PostResponse::setImageUrl);
                    mapper.map(Post::getCreatedAt, PostResponse::setTime);
                    mapper.using(createCommentsSetConverter(modelMapper))
                            .map(Post::getComments, PostResponse::setCommentResponseList);
                    mapper.using(createLikesSetConverter(modelMapper))
                            .map(Post::getLikes, PostResponse::setLikePostResponseList);
                });
    }

    private void configurePostWithCollectionsMappings(ModelMapper modelMapper) {
        Converter<Set<Comment>, List<CommentResponse>> commentsConverter = context -> {
            Set<Comment> comments = context.getSource();
            return comments == null ? List.of() : comments.stream()
                    .map(comment -> modelMapper.map(comment, CommentResponse.class))
                    .collect(Collectors.toList());
        };

        Converter<Set<Like>, List<LikePostResponse>> likesConverter = context -> {
            Set<Like> likes = context.getSource();
            return likes == null ? List.of() : likes.stream()
                    .map(like -> modelMapper.map(like, LikePostResponse.class))
                    .collect(Collectors.toList());
        };

        modelMapper.createTypeMap(Post.class, PostResponse.class, "withCollections")
                .addMappings(mapper -> {
                    mapper.map(Post::getId, PostResponse::setId);
                    mapper.map(src -> src.getUser().getUsername(), PostResponse::setUsername);
                    mapper.map(Post::getContent, PostResponse::setContent);
                    mapper.map(Post::getImageUrl, PostResponse::setImageUrl);
                    mapper.map(Post::getCreatedAt, PostResponse::setTime);
                    mapper.using(commentsConverter).map(Post::getComments, PostResponse::setCommentResponseList);
                    mapper.using(likesConverter).map(Post::getLikes, PostResponse::setLikePostResponseList);
                });

        modelMapper.createTypeMap(Post.class, PostResponse.class, "full")
                .setConverter(context -> {
                    Post source = context.getSource();
                    PostResponse destination = new PostResponse();

                    destination.setId(source.getId());
                    destination.setUsername(source.getUser().getUsername());
                    destination.setContent(source.getContent());
                    destination.setImageUrl(source.getImageUrl());
                    destination.setTime(source.getCreatedAt());

                    if (source.getComments() != null) {
                        List<CommentResponse> commentResponses = source.getComments().stream()
                                .map(comment -> modelMapper.map(comment, CommentResponse.class))
                                .collect(Collectors.toList());
                        destination.setCommentResponseList(commentResponses);
                    } else {
                        destination.setCommentResponseList(List.of());
                    }

                    if (source.getLikes() != null) {
                        List<LikePostResponse> likeResponses = source.getLikes().stream()
                                .map(like -> modelMapper.map(like, LikePostResponse.class))
                                .collect(Collectors.toList());
                        destination.setLikePostResponseList(likeResponses);
                    } else {
                        destination.setLikePostResponseList(List.of());
                    }

                    return destination;
                });
    }

    private Converter<Set<Comment>, List<CommentResponse>> createCommentsSetConverter(ModelMapper modelMapper) {
        return ctx -> ctx.getSource() == null ? List.of() : ctx.getSource().stream()
                .map(comment -> modelMapper.map(comment, CommentResponse.class))
                .collect(Collectors.toList());
    }

    private Converter<Set<Like>, List<LikePostResponse>> createLikesSetConverter(ModelMapper modelMapper) {
        return ctx -> ctx.getSource() == null ? List.of() : ctx.getSource().stream()
                .map(like -> modelMapper.map(like, LikePostResponse.class))
                .collect(Collectors.toList());
    }
}

