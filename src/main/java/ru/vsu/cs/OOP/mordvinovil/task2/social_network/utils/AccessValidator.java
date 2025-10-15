package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Comment;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Message;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Relationship;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.FriendshipStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.custom.AccessDeniedException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.SelfMessageException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;

@Component
@RequiredArgsConstructor
public class AccessValidator {

    public void validateOwnership(User currentUser, User resourceOwner) {
        if (!currentUser.getId().equals(resourceOwner.getId())) {
            throw new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED);
        }
    }

    public void validateSelfMessage(User sender, User receiver) {
        if (sender.getId().equals(receiver.getId())) {
            throw new SelfMessageException(ResponseMessageConstants.FAILURE_CREATE_SELF_MESSAGE);
        }
    }

    public void validatePostOwnership(User currentUser, Post post) {
        validateOwnership(currentUser, post.getUser());
    }

    public void validateCommentOwnership(User currentUser, Comment comment) {
        boolean isCommentCreator = comment.getCreator().getId().equals(currentUser.getId());
        boolean isPostOwner = comment.getPost().getUser().getId().equals(currentUser.getId());

        if (!isCommentCreator && !isPostOwner) {
            throw new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED);
        }
    }

    public void validateMessageOwnership(User currentUser, Message message) {
        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED);
        }
    }

    public void validateMessageAccess(User currentUser, Message message) {
        if (!message.getSender().getId().equals(currentUser.getId()) &&
                !message.getReceiver().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED);
        }
    }

    public void validateMessageReceiver(User currentUser, Message message) {
        if (!message.getReceiver().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED);
        }
    }

    public void validateRelationshipAccess(User currentUser, Relationship relationship) {
        if (!relationship.getReceiver().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You cannot change this request");
        }
    }

    public void validateRelationshipStatus(Relationship relationship, FriendshipStatus expectedStatus) {
        if (relationship.getStatus() != expectedStatus) {
            throw new AccessDeniedException("Invalid relationship status");
        }
    }
}