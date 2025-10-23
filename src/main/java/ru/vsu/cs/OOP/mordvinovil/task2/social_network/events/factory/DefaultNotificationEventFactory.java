package ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.factory;

import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.NotifitcationType;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.GenericNotificationEvent;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.utils.NotificationEventUtils;

import java.util.Map;

@Component
public class DefaultNotificationEventFactory extends AbstractNotificationEventFactory {
    public DefaultNotificationEventFactory(NotificationEventUtils utils) {
        super(utils);
    }

    @Override
    public GenericNotificationEvent createFriendRequestEvent(Object source, Long targetUserId, Long requesterId) {
        return createEventWithUserData(source, targetUserId, NotifitcationType.NEW_FRIEND_REQUEST,
                requesterId, "requester", null);
    }

    @Override
    public GenericNotificationEvent createMessageReceivedEvent(Object source, Long targetUserId, Long senderId, String messagePreview) {
        Map<String, Object> data = Map.of("messagePreview", utils.truncateMessage(messagePreview, 50));
        return createEventWithUserData(source, targetUserId, NotifitcationType.NEW_MESSAGE,
                senderId, "sender", data);
    }

    @Override
    public GenericNotificationEvent createPostLikedEvent(Object source, Long targetUserId, Long postId, Long likerId) {
        Map<String, Object> data = Map.of("postId", postId);
        return createEventWithUserData(source, targetUserId, NotifitcationType.POST_LIKED, likerId, "liker", data);
    }

    @Override
    public GenericNotificationEvent createCommentAddedEvent(Object source, Long targetUserId, Long postId, Long commenterId) {
        Map<String, Object> data = Map.of("postId", postId);
        return createEventWithUserData(source, targetUserId, NotifitcationType.NEW_COMMENT, commenterId, "commenter", data);
    }

    @Override
    public GenericNotificationEvent createFriendRequestAcceptedEvent(Object source, Long targetUserId, Long acceptorId) {
        return createEventWithUserData(source, targetUserId, NotifitcationType.FRIEND_REQUEST_ACCEPTED, acceptorId, "acceptor", null);
    }

    @Override
    public GenericNotificationEvent createMessageDeletedEvent(Object source, Long targetUserId, Long deleterUserId) {
        return createEventWithUserData(source, targetUserId, NotifitcationType.MESSAGE_DELETED, deleterUserId, "deleter", null);
    }

    @Override
    public GenericNotificationEvent createCommentLikedEvent(Object source, Long targetUserId, Long commentId, Long likerId) {
        Map<String, Object> data = Map.of("postId", commentId);
        return createEventWithUserData(source, targetUserId, NotifitcationType.COMMENT_LIKED, likerId, "liker", data);
    }

    @Override
    public GenericNotificationEvent createNewPostEvent(Object source, Long targetUserId, Long creatorUserId, Long postId) {
        Map<String, Object> data = Map.of("postId", postId);
        return createEventWithUserData(source, targetUserId, NotifitcationType.NEW_POST, creatorUserId, "creator", data);
    }

    @Override
    public GenericNotificationEvent createCustomEvent(Object source, Long targetUserId, NotifitcationType type, Map<String, Object> data) {
        return createEvent(source, targetUserId, type, data);
    }
}
