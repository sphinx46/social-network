package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.MessageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.MessageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Message;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.MessageStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.notification.NotificationEventPublisherService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.MessageRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.MessageServiceImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.MessageFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.MessageValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceImplTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private MessageFactory messageFactory;

    @Mock
    private MessageValidator messageValidator;

    @Mock
    private NotificationEventPublisherService notificationEventPublisherService;

    @Mock
    private EntityUtils entityUtils;

    @InjectMocks
    private MessageServiceImpl messageServiceImpl;

    @Test
    void createMessage_whenRequestIsValid() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        User receiverUser = createTestUser(2L, "receiver", "123@example.com");
        MessageRequest request = createTestRequest(receiverUser.getId(), "привет", null);
        Message message = createTestMessage(currentUser, receiverUser, "привет", null,
                MessageStatus.SENT, LocalDateTime.now(), LocalDateTime.now());
        MessageResponse expectedResponse = createTestResponse(message);

        when(entityUtils.getUser(receiverUser.getId())).thenReturn(receiverUser);
        when(messageFactory.createMessage(currentUser, receiverUser, request)).thenReturn(message);
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        when(entityMapper.map(message, MessageResponse.class)).thenReturn(expectedResponse);

        MessageResponse result = messageServiceImpl.create(request, currentUser);

        assertNotNull(result);

        verify(messageValidator).validateMessageCreation(request, currentUser);
        verify(entityUtils).getUser(receiverUser.getId());
        verify(messageFactory).createMessage(currentUser, receiverUser, request);
        verify(messageRepository).save(any(Message.class));
        verify(notificationEventPublisherService).publishMessageReceived(any(), eq(receiverUser.getId()),
                eq(currentUser.getId()), eq("привет"));
        verify(entityMapper).map(message, MessageResponse.class);
    }

    @Test
    void markAsReceived_whenMessageExistsAndReceiverIsCorrect() {
        User sender = createTestUser(1L, "sender", "sender@example.com");
        User receiver = createTestUser(2L, "receiver", "receiver@example.com");
        Message message = createTestMessage(sender, receiver, "привет", null,
                MessageStatus.SENT, LocalDateTime.now(), LocalDateTime.now());
        message.setId(1L);
        MessageResponse expectedResponse = createTestResponse(message);
        expectedResponse.setStatus(MessageStatus.RECEIVED);

        when(entityUtils.getMessage(1L)).thenReturn(message);
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        when(entityMapper.map(message, MessageResponse.class)).thenReturn(expectedResponse);

        MessageResponse result = messageServiceImpl.markAsReceived(1L, receiver);

        assertNotNull(result);
        assertEquals(MessageStatus.RECEIVED, result.getStatus());

        verify(entityUtils).getMessage(1L);
        verify(messageRepository).save(any(Message.class));
        verify(entityMapper).map(message, MessageResponse.class);
        verify(messageValidator).validateMessageReceiver(receiver, message);
    }

    @Test
    void markAsRead_whenMessageExistsAndReceiverIsCorrect() {
        User sender = createTestUser(1L, "sender", "sender@example.com");
        User receiver = createTestUser(2L, "receiver", "receiver@example.com");
        Message message = createTestMessage(sender, receiver, "привет", null,
                MessageStatus.RECEIVED, LocalDateTime.now(), LocalDateTime.now());
        message.setId(1L);
        MessageResponse expectedResponse = createTestResponse(message);
        expectedResponse.setStatus(MessageStatus.READ);

        when(entityUtils.getMessage(1L)).thenReturn(message);
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        when(entityMapper.map(message, MessageResponse.class)).thenReturn(expectedResponse);

        MessageResponse result = messageServiceImpl.markAsRead(1L, receiver);

        assertNotNull(result);
        assertEquals(MessageStatus.READ, result.getStatus());

        verify(entityUtils).getMessage(1L);
        verify(messageRepository).save(any(Message.class));
        verify(entityMapper).map(message, MessageResponse.class);
        verify(messageValidator).validateMessageReceiver(receiver, message);
    }

    @Test
    void editMessage_whenMessageExistsAndUserIsOwner() {
        User owner = createTestUser(1L, "owner", "owner@example.com");
        User receiver = createTestUser(2L, "receiver", "receiver@example.com");
        Message originalMessage = createTestMessage(owner, receiver, "привет", null,
                MessageStatus.SENT, LocalDateTime.now(), LocalDateTime.now());
        originalMessage.setId(1L);
        MessageRequest updateRequest = createTestRequest(receiver.getId(), "пока", "image.jpg");
        Message updatedMessage = createTestMessage(owner, receiver, "пока", "image.jpg",
                MessageStatus.SENT, originalMessage.getCreatedAt(), LocalDateTime.now());
        updatedMessage.setId(1L);
        MessageResponse expectedResponse = createTestResponse(updatedMessage);

        when(entityUtils.getMessage(1L)).thenReturn(originalMessage);
        when(messageRepository.save(any(Message.class))).thenReturn(updatedMessage);
        when(entityMapper.map(updatedMessage, MessageResponse.class)).thenReturn(expectedResponse);

        MessageResponse result = messageServiceImpl.editMessage(1L, updateRequest, owner);

        assertNotNull(result);
        assertEquals("пока", result.getContent());
        assertEquals("image.jpg", result.getImageUrl());

        verify(messageValidator).validateMessageUpdate(updateRequest, owner);
        verify(entityUtils).getMessage(1L);
        verify(messageRepository).save(any(Message.class));
        verify(entityMapper).map(updatedMessage, MessageResponse.class);
        verify(messageValidator).validateMessageOwnership(owner, originalMessage);
    }

    @Test
    void deleteMessage_whenMessageExistsAndUserIsOwner() {
        User owner = createTestUser(1L, "owner", "owner@example.com");
        User receiver = createTestUser(2L, "receiver", "receiver@example.com");
        Message message = createTestMessage(owner, receiver, "привет", null,
                MessageStatus.SENT, LocalDateTime.now(), LocalDateTime.now());
        message.setId(1L);

        when(entityUtils.getMessage(1L)).thenReturn(message);

        assertDoesNotThrow(() -> messageServiceImpl.deleteMessage(1L, owner));

        verify(entityUtils).getMessage(1L);
        verify(messageRepository).delete(message);
        verify(messageValidator).validateMessageOwnership(owner, message);
        verify(notificationEventPublisherService).publishMessageDeleted(any(), eq(receiver.getId()),
                eq(owner.getId()));
    }

    @Test
    void getConversation() {
        User currentUser = createTestUser(1L, "user", "user@example.com");
        User otherUser = createTestUser(2L, "other", "other@example.com");
        Message message1 = createTestMessage(currentUser, otherUser, "привет", null,
                MessageStatus.SENT, LocalDateTime.now(), LocalDateTime.now());
        Message message2 = createTestMessage(otherUser, currentUser, "привет", null,
                MessageStatus.RECEIVED, LocalDateTime.now(), LocalDateTime.now());
        List<Message> messages = List.of(message1, message2);
        Page<Message> messagePage = new PageImpl<>(messages);
        PageRequest pageRequest = PageRequest.builder()
                .pageNumber(0)
                .size(10)
                .sortBy("createdAt")
                .direction(org.springframework.data.domain.Sort.Direction.DESC)
                .build();

        when(entityUtils.getUser(2L)).thenReturn(otherUser);
        when(messageRepository.findMessagesBetweenUsers(eq(1L), eq(2L), any())).thenReturn(Optional.of(messagePage));

        PageResponse<MessageResponse> result = messageServiceImpl.getConversation(2L, currentUser, pageRequest);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());

        verify(entityUtils).getUser(2L);
        verify(messageRepository).findMessagesBetweenUsers(eq(1L), eq(2L), any());
    }

    @Test
    void getSentMessages() {
        User currentUser = createTestUser(1L, "user", "user@example.com");
        Message message = createTestMessage(currentUser, createTestUser(2L, "receiver", "receiver@example.com"),
                "привет", null, MessageStatus.SENT, LocalDateTime.now(), LocalDateTime.now());
        List<Message> messages = List.of(message);
        Page<Message> messagePage = new PageImpl<>(messages);
        PageRequest pageRequest = PageRequest.builder()
                .pageNumber(0)
                .size(10)
                .sortBy("createdAt")
                .direction(org.springframework.data.domain.Sort.Direction.DESC)
                .build();

        when(messageRepository.findBySenderId(eq(1L), any())).thenReturn(Optional.of(messagePage));

        PageResponse<MessageResponse> result = messageServiceImpl.getSentMessages(currentUser, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(messageRepository).findBySenderId(eq(1L), any());
    }

    @Test
    void getReceivedMessages() {
        User currentUser = createTestUser(1L, "user", "user@example.com");
        Message message = createTestMessage(createTestUser(2L, "sender", "sender@example.com"), currentUser,
                "привет", null, MessageStatus.RECEIVED, LocalDateTime.now(), LocalDateTime.now());
        List<Message> messages = List.of(message);
        Page<Message> messagePage = new PageImpl<>(messages);
        PageRequest pageRequest = PageRequest.builder()
                .pageNumber(0)
                .size(10)
                .sortBy("createdAt")
                .direction(org.springframework.data.domain.Sort.Direction.DESC)
                .build();

        when(messageRepository.findByReceiverIdAndStatus(eq(1L), eq(MessageStatus.RECEIVED), any())).thenReturn(Optional.of(messagePage));

        PageResponse<MessageResponse> result = messageServiceImpl.getReceivedMessages(currentUser, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(messageRepository).findByReceiverIdAndStatus(eq(1L), eq(MessageStatus.RECEIVED), any());
    }

    @Test
    void getReadMessages() {
        User currentUser = createTestUser(1L, "user", "user@example.com");
        Message message = createTestMessage(createTestUser(2L, "sender", "sender@example.com"), currentUser,
                "привет", null, MessageStatus.READ, LocalDateTime.now(), LocalDateTime.now());
        List<Message> messages = List.of(message);
        Page<Message> messagePage = new PageImpl<>(messages);
        PageRequest pageRequest = PageRequest.builder()
                .pageNumber(0)
                .size(10)
                .sortBy("createdAt")
                .direction(org.springframework.data.domain.Sort.Direction.DESC)
                .build();

        when(messageRepository.findByReceiverIdAndStatus(eq(1L), eq(MessageStatus.READ), any())).thenReturn(Optional.of(messagePage));

        PageResponse<MessageResponse> result = messageServiceImpl.getReadMessages(currentUser, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(messageRepository).findByReceiverIdAndStatus(eq(1L), eq(MessageStatus.READ), any());
    }
}

