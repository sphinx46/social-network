package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.NotificationResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Notification;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.NotificationStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.NotificationType;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.custom.AccessDeniedException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.notification.NotificationNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.NotificationRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.NotificationServiceImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.NotificationValidator;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private NotificationValidator notificationValidator;

    @Mock
    private EntityUtils entityUtils;

    @InjectMocks
    private NotificationServiceImpl notificationServiceImpl;

    private User currentUser;
    private User otherUser;
    private Notification notification;
    private NotificationResponse notificationResponse;
    private PageRequest pageRequest;

    @BeforeEach
    void setUp() {
        currentUser = createTestUser(1L, "currentUser", "current@example.com");
        otherUser = createTestUser(2L, "otherUser", "other@example.com");
        notification = createTestNotification(currentUser, NotificationType.POST_LIKED, NotificationStatus.UNREAD);
        notification.setId(1L);
        notificationResponse = createTestNotificationResponse(notification);
        pageRequest = org.springframework.data.domain.PageRequest.of(0, 10);
    }

    @Test
    void getUserNotifications_whenUserHasNotifications() {
        List<Notification> notifications = List.of(notification);
        Page<Notification> notificationPage = new PageImpl<>(notifications);
        Page<NotificationResponse> expectedResponses = new PageImpl<>(List.of(notificationResponse));

        when(notificationRepository.findByUserActionOrderByCreatedAtDesc(eq(currentUser), any(PageRequest.class))).thenReturn(notificationPage);
        when(entityMapper.map(notification, NotificationResponse.class)).thenReturn(notificationResponse);

        PageResponse<NotificationResponse> result = notificationServiceImpl.getUserNotifications(currentUser, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(notificationRepository).findByUserActionOrderByCreatedAtDesc(eq(currentUser), any(PageRequest.class));
    }

    @Test
    void getUserNotificationById_whenNotificationExistsAndUserHasAccess() {
        doNothing().when(notificationValidator).validateNotificationAccess(1L, currentUser);
        when(entityUtils.getNotification(1L)).thenReturn(notification);
        when(entityMapper.map(notification, NotificationResponse.class)).thenReturn(notificationResponse);

        NotificationResponse result = notificationServiceImpl.getUserNotificationById(1L, currentUser);

        assertNotNull(result);
        assertEquals(notificationResponse.getId(), result.getId());

        verify(notificationValidator).validateNotificationAccess(1L, currentUser);
        verify(entityUtils).getNotification(1L);
        verify(entityMapper).map(notification, NotificationResponse.class);
    }

    @Test
    void getUserNotificationById_whenNotificationNotFound() {
        doNothing().when(notificationValidator).validateNotificationAccess(999L, currentUser);
        when(entityUtils.getNotification(999L))
                .thenThrow(new NotificationNotFoundException(ResponseMessageConstants.FAILURE_NOTIFICATION_NOT_FOUND));

        NotificationNotFoundException exception = assertThrows(NotificationNotFoundException.class,
                () -> notificationServiceImpl.getUserNotificationById(999L, currentUser));

        assertEquals(ResponseMessageConstants.FAILURE_NOTIFICATION_NOT_FOUND, exception.getMessage());

        verify(notificationValidator).validateNotificationAccess(999L, currentUser);
        verify(entityUtils).getNotification(999L);
        verify(entityMapper, never()).map(any(), any());
    }

    @Test
    void getUserNotificationById_whenUserHasNoAccess() {
        doThrow(new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED))
                .when(notificationValidator).validateNotificationAccess(1L, otherUser);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> notificationServiceImpl.getUserNotificationById(1L, otherUser));

        assertEquals(ResponseMessageConstants.ACCESS_DENIED, exception.getMessage());

        verify(notificationValidator).validateNotificationAccess(1L, otherUser);
        verify(entityUtils, never()).getNotification(anyLong());
        verify(entityMapper, never()).map(any(), any());
    }

    @Test
    void getUnreadNotifications_whenUserHasUnreadNotifications() {
        List<Notification> unreadNotifications = List.of(notification);
        Page<Notification> notificationPage = new PageImpl<>(unreadNotifications);
        Page<NotificationResponse> expectedResponses = new PageImpl<>(List.of(notificationResponse));

        when(notificationRepository.findByUserActionAndStatusOrderByCreatedAtDesc(eq(currentUser), eq(NotificationStatus.UNREAD), any(PageRequest.class)))
                .thenReturn(notificationPage);
        when(entityMapper.map(notification, NotificationResponse.class)).thenReturn(notificationResponse);

        PageResponse<NotificationResponse> result = notificationServiceImpl.getUnreadNotifications(currentUser, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(notificationRepository).findByUserActionAndStatusOrderByCreatedAtDesc(eq(currentUser), eq(NotificationStatus.UNREAD), any(PageRequest.class));
    }

    @Test
    void getUnreadNotificationsCount_whenUserHasUnreadNotifications() {
        Long expectedCount = 2L;

        when(notificationRepository.countByUserActionAndStatus(currentUser, NotificationStatus.UNREAD))
                .thenReturn(expectedCount);

        Long result = notificationServiceImpl.getUnreadNotificationsCount(currentUser);

        assertEquals(expectedCount, result);

        verify(notificationRepository).countByUserActionAndStatus(currentUser, NotificationStatus.UNREAD);
    }

    @Test
    void markAsRead_whenNotificationExistsAndUserHasAccess() {
        Notification updatedNotification = createTestNotification(currentUser, NotificationType.POST_LIKED, NotificationStatus.READ);
        updatedNotification.setId(1L);
        NotificationResponse expectedResponse = createTestNotificationResponse(updatedNotification);

        doNothing().when(notificationValidator).validateNotificationAccess(1L, currentUser);
        when(entityUtils.getNotification(1L)).thenReturn(notification);
        when(notificationRepository.save(any(Notification.class))).thenReturn(updatedNotification);
        when(entityMapper.map(updatedNotification, NotificationResponse.class)).thenReturn(expectedResponse);

        NotificationResponse result = notificationServiceImpl.markAsRead(1L, currentUser);

        assertNotNull(result);
        assertEquals(NotificationStatus.READ, updatedNotification.getStatus());
        assertNotNull(updatedNotification.getUpdatedAt());

        verify(notificationValidator).validateNotificationAccess(1L, currentUser);
        verify(entityUtils).getNotification(1L);
        verify(notificationRepository).save(any(Notification.class));
        verify(entityMapper).map(updatedNotification, NotificationResponse.class);
    }

    @Test
    void markAllAsRead_whenUserHasNotifications() {
        notificationServiceImpl.markAllAsRead(currentUser);

        verify(notificationRepository).markAllAsRead(currentUser.getId(), NotificationStatus.READ);
    }

    @Test
    void deleteNotification_whenNotificationExistsAndUserHasAccess() {
        Notification mockNotification = mock(Notification.class);
        when(mockNotification.getUserAction()).thenReturn(currentUser);

        when(entityUtils.getNotification(1L)).thenReturn(mockNotification);
        doNothing().when(notificationValidator).validateUserNotificationsAccess(currentUser, currentUser);

        notificationServiceImpl.deleteNotification(1L, currentUser);

        verify(mockNotification).setStatus(NotificationStatus.DELETED);
        verify(mockNotification).setUpdatedAt(any(LocalDateTime.class));
        verify(notificationRepository).save(mockNotification);

        verify(entityUtils).getNotification(1L);
        verify(notificationValidator).validateUserNotificationsAccess(currentUser, currentUser);
    }

    @Test
    void deleteNotification_whenUserHasNoAccess() {
        when(entityUtils.getNotification(1L)).thenReturn(notification);
        doThrow(new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED))
                .when(notificationValidator).validateUserNotificationsAccess(currentUser, otherUser);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> notificationServiceImpl.deleteNotification(1L, otherUser));

        assertEquals(ResponseMessageConstants.ACCESS_DENIED, exception.getMessage());

        verify(entityUtils).getNotification(1L);
        verify(notificationValidator).validateUserNotificationsAccess(currentUser, otherUser);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void clearDeletedNotifications_whenUserHasDeletedNotifications() {
        notificationServiceImpl.clearDeletedNotifications(currentUser);

        verify(notificationRepository).deleteAllDeletedByUser(currentUser);
    }
}