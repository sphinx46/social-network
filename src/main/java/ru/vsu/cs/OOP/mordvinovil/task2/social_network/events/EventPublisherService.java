package ru.vsu.cs.OOP.mordvinovil.task2.social_network.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.NotifitcationType;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.factory.DefaultNotificationEventFactory;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisherService {
    private final ApplicationEventPublisher eventPublisher;
    private final DefaultNotificationEventFactory eventFactory;

    public void publishFriendRequest(Object source, Long targetUserId, Long requesterId) {
        publishEvent(eventFactory.createFriendRequestEvent(source, targetUserId, requesterId));
    }

    public void publishMessageReceived(Object source, Long targetUserId, Long senderId, String messagePreview) {
        publishEvent(eventFactory.createMessageReceivedEvent(source, targetUserId, senderId, messagePreview));
    }

    public void publishMessageDeleted(Object source, Long targetUserId, Long deleterId) {
        publishEvent(eventFactory.createMessageDeletedEvent(source, targetUserId, deleterId));
    }

    public void publishPostLiked(Object source, Long targetUserId, Long postId, Long likerId) {
        publishEvent(eventFactory.createPostLikedEvent(source, targetUserId, postId, likerId));
    }

    public void publisCommentLiked(Object source, Long targetUserId, Long postId, Long likerId) {
        publishEvent(eventFactory.createCommentLikedEvent(source, targetUserId, postId, likerId));
    }

    public void publishCommentAdded(Object source, Long targetUserId, Long postId, Long commenterId) {
        publishEvent(eventFactory.createCommentAddedEvent(source, targetUserId, postId, commenterId));
    }

    public void publishNewPost(Object source, Long targetUserId, Long creatorUserId, Long postId) {
        publishEvent(eventFactory.createNewPostEvent(source, targetUserId, creatorUserId, postId));
    }

    public void publishFriendRequestAccepted(Object source, Long targetUserId, Long acceptorId) {
        publishEvent(eventFactory.createFriendRequestAcceptedEvent(source, targetUserId, acceptorId));
    }

    public void publishCustomEvent(Object source, Long targetUserId, NotifitcationType type, Map<String, Object> data) {
        publishEvent(eventFactory.createCustomEvent(source, targetUserId, type, data));
    }

    private void publishEvent(GenericNotificationEvent event) {
        try {
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            log.error("Failed to publish event: {}", event.getNotifitcationType(), e);
        }
    }
}