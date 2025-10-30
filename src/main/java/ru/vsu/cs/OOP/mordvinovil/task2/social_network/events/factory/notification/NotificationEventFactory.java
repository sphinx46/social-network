package ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.factory.notification;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.NotificationType;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.notification.GenericNotificationEvent;

import java.util.Map;

public interface NotificationEventFactory {
    GenericNotificationEvent createFriendRequestEvent(Object source, Long targetUserId, Long requesterId);
    GenericNotificationEvent createMessageReceivedEvent(Object source, Long targetUserId, Long senderId, String messagePreview);
    GenericNotificationEvent createPostLikedEvent(Object source, Long targetUserId, Long postId, Long likerId);
    GenericNotificationEvent createCommentAddedEvent(Object source, Long targetUserId, Long postId, Long commenterId);
    GenericNotificationEvent createFriendRequestAcceptedEvent(Object source, Long targetUserId, Long acceptorId);
    GenericNotificationEvent createMessageDeletedEvent(Object source, Long targetUserId, Long deleterUserId);
    GenericNotificationEvent createCommentLikedEvent(Object source, Long targetUserId, Long commentId, Long likerId);
    GenericNotificationEvent createNewPostEvent(Object source, Long targetUserId, Long creatorUserId, Long postId);
    GenericNotificationEvent createCustomEvent(Object source, Long targetUserId, NotificationType type, Map<String, Object> data);
}