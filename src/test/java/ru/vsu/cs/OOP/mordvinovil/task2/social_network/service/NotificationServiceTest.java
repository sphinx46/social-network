package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.NotificationResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Notification;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.NotificationStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.NotificationType;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.GenericNotificationEvent;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.custom.AccessDeniedException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.NotificationNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.NotificationRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.UserRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.NotificationValidator;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private WebSocketNotificationService webSocketNotificationService;

    @Mock
    private NotificationValidator notificationValidator;

    @InjectMocks
    private NotificationService notificationService;

    private User currentUser;
    private User otherUser;
    private Notification notification;
    private NotificationResponse notificationResponse;

    @BeforeEach
    void setUp() {
        currentUser = createTestUser(1L, "currentUser", "current@example.com");
        otherUser = createTestUser(2L, "otherUser", "other@example.com");
        notification = createTestNotification(currentUser, NotificationType.POST_LIKED, NotificationStatus.UNREAD);
        notification.setId(1L);
        notificationResponse = createTestNotificationResponse(notification);
    }

    @Test
    void handleNotificationEvent_whenEventIsValid() {
        GenericNotificationEvent event = createTestNotificationEvent(
                currentUser.getId(),
                NotificationType.POST_LIKED,
                Map.of("postId", "123", "likerId", "456", "likerUsername", "likerUser")
        );

        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.handleNotificationEvent(event);

        verify(userRepository).findById(currentUser.getId());
        verify(notificationRepository).save(any(Notification.class));
        verify(webSocketNotificationService).sendNotification(eq(currentUser.getId()), any(Notification.class));
    }

    @Test
    void handleNotificationEvent_whenUserNotFound() {
        GenericNotificationEvent event = createTestNotificationEvent(
                999L,
                NotificationType.POST_LIKED,
                Map.of("postId", "123", "likerId", "456")
        );

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        notificationService.handleNotificationEvent(event);

        verify(userRepository).findById(999L);
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(webSocketNotificationService, never()).sendNotification(anyLong(), any(Notification.class));
    }

    @Test
    void handleNotificationEvent_whenEventWithCommentData() {
        GenericNotificationEvent event = createTestNotificationEvent(
                currentUser.getId(),
                NotificationType.COMMENT_LIKED,
                Map.of("postId", "123", "commentId", "789", "likerId", "456", "likerUsername", "likerUser")
        );

        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.handleNotificationEvent(event);

        verify(userRepository).findById(currentUser.getId());
        verify(notificationRepository).save(any(Notification.class));
        verify(webSocketNotificationService).sendNotification(eq(currentUser.getId()), any(Notification.class));
    }

    @Test
    void handleNotificationEvent_whenFriendRequestEvent() {
        GenericNotificationEvent event = createTestNotificationEvent(
                currentUser.getId(),
                NotificationType.NEW_FRIEND_REQUEST,
                Map.of("requesterId", "789", "requesterUsername", "requesterUser")
        );

        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.handleNotificationEvent(event);

        verify(userRepository).findById(currentUser.getId());
        verify(notificationRepository).save(any(Notification.class));
        verify(webSocketNotificationService).sendNotification(eq(currentUser.getId()), any(Notification.class));
    }

    @Test
    void handleNotificationEvent_whenMessageEvent() {
        GenericNotificationEvent event = createTestNotificationEvent(
                currentUser.getId(),
                NotificationType.NEW_MESSAGE,
                Map.of("senderId", "789", "senderUsername", "senderUser", "messagePreview", "Hello!")
        );

        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.handleNotificationEvent(event);

        verify(userRepository).findById(currentUser.getId());
        verify(notificationRepository).save(any(Notification.class));
        verify(webSocketNotificationService).sendNotification(eq(currentUser.getId()), any(Notification.class));
    }

    @Test
    void getUserNotifications_whenUserHasNotifications() {
        List<Notification> notifications = List.of(notification);
        List<NotificationResponse> expectedResponses = List.of(notificationResponse);

        when(notificationRepository.findByUserActionOrderByCreatedAtDesc(currentUser)).thenReturn(notifications);
        when(entityMapper.mapList(notifications, NotificationResponse.class)).thenReturn(expectedResponses);

        List<NotificationResponse> result = notificationService.getUserNotifications(currentUser);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(notificationRepository).findByUserActionOrderByCreatedAtDesc(currentUser);
        verify(entityMapper).mapList(notifications, NotificationResponse.class);
    }

    @Test
    void getUserNotificationById_whenNotificationExistsAndUserHasAccess() {
        doNothing().when(notificationValidator).validateNotificationAccess(1L, currentUser);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(entityMapper.map(notification, NotificationResponse.class)).thenReturn(notificationResponse);

        NotificationResponse result = notificationService.getUserNotificationById(1L, currentUser);

        assertNotNull(result);
        assertEquals(notificationResponse.getId(), result.getId());

        verify(notificationValidator).validateNotificationAccess(1L, currentUser);
        verify(notificationRepository).findById(1L);
        verify(entityMapper).map(notification, NotificationResponse.class);
    }

    @Test
    void getUserNotificationById_whenNotificationNotFound() {
        doNothing().when(notificationValidator).validateNotificationAccess(999L, currentUser);
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        NotificationNotFoundException exception = assertThrows(NotificationNotFoundException.class,
                () -> notificationService.getUserNotificationById(999L, currentUser));

        assertEquals(ResponseMessageConstants.NOT_FOUND, exception.getMessage());

        verify(notificationValidator).validateNotificationAccess(999L, currentUser);
        verify(notificationRepository).findById(999L);
        verify(entityMapper, never()).map(any(), any());
    }

    @Test
    void getUserNotificationById_whenUserHasNoAccess() {
        doThrow(new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED))
                .when(notificationValidator).validateNotificationAccess(1L, otherUser);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> notificationService.getUserNotificationById(1L, otherUser));

        assertEquals(ResponseMessageConstants.ACCESS_DENIED, exception.getMessage());

        verify(notificationValidator).validateNotificationAccess(1L, otherUser);
        verify(notificationRepository, never()).findById(anyLong());
        verify(entityMapper, never()).map(any(), any());
    }

    @Test
    void getUnreadNotifications_whenUserHasUnreadNotifications() {
        List<Notification> unreadNotifications = List.of(notification);
        List<NotificationResponse> expectedResponses = List.of(notificationResponse);

        when(notificationRepository.findByUserActionAndStatusOrderByCreatedAtDesc(currentUser, NotificationStatus.UNREAD))
                .thenReturn(unreadNotifications);
        when(entityMapper.mapList(unreadNotifications, NotificationResponse.class)).thenReturn(expectedResponses);

        List<NotificationResponse> result = notificationService.getUnreadNotifications(currentUser);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(notificationRepository).findByUserActionAndStatusOrderByCreatedAtDesc(currentUser, NotificationStatus.UNREAD);
        verify(entityMapper).mapList(unreadNotifications, NotificationResponse.class);
    }

    @Test
    void getUnreadNotificationsCount_whenUserHasUnreadNotifications() {
        Long expectedCount = 2L;

        when(notificationRepository.countByUserActionAndStatus(currentUser, NotificationStatus.UNREAD))
                .thenReturn(expectedCount);

        Long result = notificationService.getUnreadNotificationsCount(currentUser);

        assertEquals(expectedCount, result);

        verify(notificationRepository).countByUserActionAndStatus(currentUser, NotificationStatus.UNREAD);
    }

    @Test
    void markAsRead_whenNotificationExistsAndUserHasAccess() {
        Notification updatedNotification = createTestNotification(currentUser, NotificationType.POST_LIKED, NotificationStatus.READ);
        updatedNotification.setId(1L);
        NotificationResponse expectedResponse = createTestNotificationResponse(updatedNotification);

        doNothing().when(notificationValidator).validateNotificationAccess(1L, currentUser);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(updatedNotification);
        when(entityMapper.map(updatedNotification, NotificationResponse.class)).thenReturn(expectedResponse);

        NotificationResponse result = notificationService.markAsRead(1L, currentUser);

        assertNotNull(result);
        assertEquals(NotificationStatus.READ, updatedNotification.getStatus());
        assertNotNull(updatedNotification.getUpdatedAt());

        verify(notificationValidator).validateNotificationAccess(1L, currentUser);
        verify(notificationRepository).findById(1L);
        verify(notificationRepository).save(notification);
        verify(entityMapper).map(updatedNotification, NotificationResponse.class);
    }

    @Test
    void markAllAsRead_whenUserHasNotifications() {
        notificationService.markAllAsRead(currentUser);

        verify(notificationRepository).markAllAsRead(currentUser.getId(), NotificationStatus.READ);
    }

    @Test
    void deleteNotification_whenNotificationExistsAndUserHasAccess() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        doNothing().when(notificationValidator).validateUserNotificationsAccess(currentUser, currentUser);

        notificationService.deleteNotification(1L, currentUser);

        assertEquals(NotificationStatus.DELETED, notification.getStatus());
        assertNotNull(notification.getUpdatedAt());

        verify(notificationRepository).findById(1L);
        verify(notificationValidator).validateUserNotificationsAccess(currentUser, currentUser);
        verify(notificationRepository).save(notification);
    }

    @Test
    void deleteNotification_whenUserHasNoAccess() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        doThrow(new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED))
                .when(notificationValidator).validateUserNotificationsAccess(currentUser, otherUser);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> notificationService.deleteNotification(1L, otherUser));

        assertEquals(ResponseMessageConstants.ACCESS_DENIED, exception.getMessage());

        verify(notificationRepository).findById(1L);
        verify(notificationValidator).validateUserNotificationsAccess(currentUser, otherUser);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void clearDeletedNotifications_whenUserHasDeletedNotifications() {
        notificationService.clearDeletedNotifications(currentUser);

        verify(notificationRepository).deleteAllDeletedByUser(currentUser);
    }

    @Test
    void handleNotificationEvent_whenExceptionOccurs() {
        GenericNotificationEvent event = createTestNotificationEvent(
                currentUser.getId(),
                NotificationType.POST_LIKED,
                Map.of("postId", "123")
        );

        when(userRepository.findById(currentUser.getId())).thenThrow(new RuntimeException("Database error"));

        assertDoesNotThrow(() -> notificationService.handleNotificationEvent(event));

        verify(userRepository).findById(currentUser.getId());
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(webSocketNotificationService, never()).sendNotification(anyLong(), any(Notification.class));
    }
}