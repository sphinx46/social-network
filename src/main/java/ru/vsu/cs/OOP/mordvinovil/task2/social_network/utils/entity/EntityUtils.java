package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.*;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.providersImpl.*;

@Component
@RequiredArgsConstructor
public class EntityUtils {
    private final UserEntityProviderImpl userProvider;
    private final ProfileEntityProviderImpl profileProvider;
    private final PostEntityProviderImpl postProvider;
    private final CommentEntityProviderImpl commentProvider;
    private final MessageEntityProviderImpl messageProvider;
    private final RelationshipEntityProviderImpl relationshipProvider;
    private final LikeEntityProviderImpl likeProvider;
    private final NotificationEntityProviderImpl notificationProvider;

    public User getUser(Long id) {
        return userProvider.getById(id);
    }

    public Notification getNotification(Long id) {
        return notificationProvider.getById(id);
    }

    public Profile getProfile(Long id) {
        return profileProvider.getById(id);
    }

    public Profile getProfileByUser(User user) {
        return profileProvider.getByUser(user);
    }

    public Post getPost(Long id) {
        return postProvider.getById(id);
    }

    public Comment getComment(Long id) {
        return commentProvider.getById(id);
    }

    public Message getMessage(Long id) {
        return messageProvider.getById(id);
    }

    public Relationship getRelationship(Long id) {
        return relationshipProvider.getById(id);
    }

    public Like getLike(Long id) {
        return likeProvider.getById(id);
    }
}