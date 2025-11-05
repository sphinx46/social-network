package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Notification;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.NotificationStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.NotificationType;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.notification.GenericNotificationEvent;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.NotificationRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.UserRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.eventhandler.notification.NotificationEventHandlerImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.notification.WebSocketNotificationService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.NotificationFactory;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class NotificationHandlerImplTest {
    @Mock
    private NotificationFactory factory;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private WebSocketNotificationService webSocketNotificationService;

    @InjectMocks
    private NotificationEventHandlerImpl notificationEventHandler;

    private User currentUser;
    private Notification notification;

    @BeforeEach
    void setUp() {
        currentUser = createTestUser(1L, "currentUser", "current@example.com");
        notification = createTestNotification(currentUser, NotificationType.POST_LIKED, NotificationStatus.UNREAD);
    }

    @Test
    void handleNotificationEvent_whenEventIsValid() {
        GenericNotificationEvent event = createTestNotificationEvent(
                currentUser.getId(),
                NotificationType.POST_LIKED,
                Map.of("postId", "123", "likerId", "456", "likerUsername", "likerUser")
        );

        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(factory.createNotificationFromEvent(event, currentUser)).thenReturn(notification);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationEventHandler.handleNotificationEvent(event);

        verify(userRepository).findById(currentUser.getId());
        verify(factory).createNotificationFromEvent(event, currentUser);
        verify(notificationRepository).save(notification);
        verify(webSocketNotificationService).sendNotification(eq(currentUser.getId()), eq(notification));
    }

    @Test
    void handleNotificationEvent_whenUserNotFound() {
        GenericNotificationEvent event = createTestNotificationEvent(
                999L,
                NotificationType.POST_LIKED,
                Map.of("postId", "123", "likerId", "456")
        );

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        notificationEventHandler.handleNotificationEvent(event);

        verify(userRepository).findById(999L);
        verify(factory, never()).createNotificationFromEvent(any(), any());
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(webSocketNotificationService, never()).sendNotification(anyLong(), any(Notification.class));
    }

    @Test
    void handleNotificationEvent_whenExceptionOccurs() {
        GenericNotificationEvent event = createTestNotificationEvent(
                currentUser.getId(),
                NotificationType.POST_LIKED,
                Map.of("postId", "123")
        );

        when(userRepository.findById(currentUser.getId())).thenThrow(new RuntimeException("Database error"));

        assertDoesNotThrow(() -> notificationEventHandler.handleNotificationEvent(event));

        verify(userRepository).findById(currentUser.getId());
        verify(factory, never()).createNotificationFromEvent(any(), any());
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(webSocketNotificationService, never()).sendNotification(anyLong(), any(Notification.class));
    }
}