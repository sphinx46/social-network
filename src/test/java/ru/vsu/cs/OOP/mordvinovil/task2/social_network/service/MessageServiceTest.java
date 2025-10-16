package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.MessageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.MessageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Message;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.MessageStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.MessageNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.UserNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.MessageRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.UserRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.MessageFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.AccessValidator;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.MessageValidator;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private MessageFactory messageFactory;

    @Mock
    private MessageValidator messageValidator;

    @Mock
    private AccessValidator accessValidator;

    @InjectMocks
    private MessageService messageService;

    @Test
    void createMessage_whenRequestIsValid() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        User receiverUser = createTestUser(2L, "receiver", "123@example.com");
        MessageRequest request = createTestRequest(receiverUser.getId(), "привет", null);
        Message message = createTestMessage(currentUser, receiverUser, "привет", null,
                MessageStatus.SENT, LocalDateTime.now(), LocalDateTime.now());
        MessageResponse expectedResponse = createTestResponse(message);

        when(userRepository.findById(receiverUser.getId())).thenReturn(Optional.of(receiverUser));
        when(messageFactory.createMessage(currentUser, receiverUser, request)).thenReturn(message);
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        when(entityMapper.map(message, MessageResponse.class)).thenReturn(expectedResponse);

        MessageResponse result = messageService.create(request, currentUser);

        assertNotNull(result);

        verify(messageValidator).validateMessageCreation(request, currentUser);
        verify(userRepository).findById(receiverUser.getId());
        verify(messageFactory).createMessage(currentUser, receiverUser, request);
        verify(messageRepository).save(any(Message.class));
        verify(entityMapper).map(message, MessageResponse.class);
    }

    @Test
    void createMessage_whenReceiverUserNotFound() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        MessageRequest request = createTestRequest(2L, "привет", null);

        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> messageService.create(request, currentUser));

        assertEquals(ResponseMessageConstants.NOT_FOUND, exception.getMessage());
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

        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        when(entityMapper.map(message, MessageResponse.class)).thenReturn(expectedResponse);

        MessageResponse result = messageService.markAsReceived(1L, receiver);

        assertNotNull(result);
        assertEquals(MessageStatus.RECEIVED, result.getStatus());

        verify(messageRepository).findById(1L);
        verify(messageRepository).save(any(Message.class));
        verify(entityMapper).map(message, MessageResponse.class);
        verify(accessValidator).validateMessageReceiver(receiver, message);
    }

    @Test
    void markAsReceived_whenMessageNotFound() {
        User user = createTestUser(1L, "user", "user@example.com");

        when(messageRepository.findById(1L)).thenReturn(Optional.empty());

        MessageNotFoundException exception = assertThrows(MessageNotFoundException.class,
                () -> messageService.markAsReceived(1L, user));

        assertEquals(ResponseMessageConstants.NOT_FOUND, exception.getMessage());
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

        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        when(entityMapper.map(message, MessageResponse.class)).thenReturn(expectedResponse);

        MessageResponse result = messageService.markAsRead(1L, receiver);

        assertNotNull(result);
        assertEquals(MessageStatus.READ, result.getStatus());

        verify(messageRepository).findById(1L);
        verify(messageRepository).save(any(Message.class));
        verify(entityMapper).map(message, MessageResponse.class);
        verify(accessValidator).validateMessageReceiver(receiver, message);
    }

    @Test
    void markAsRead_whenMessageNotFound() {
        User user = createTestUser(1L, "user", "user@example.com");

        when(messageRepository.findById(1L)).thenReturn(Optional.empty());

        MessageNotFoundException exception = assertThrows(MessageNotFoundException.class,
                () -> messageService.markAsRead(1L, user));

        assertEquals(ResponseMessageConstants.NOT_FOUND, exception.getMessage());
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

        when(messageRepository.findById(1L)).thenReturn(Optional.of(originalMessage));
        when(messageRepository.save(any(Message.class))).thenReturn(updatedMessage);
        when(entityMapper.map(updatedMessage, MessageResponse.class)).thenReturn(expectedResponse);

        MessageResponse result = messageService.editMessage(1L, updateRequest, owner);

        assertNotNull(result);
        assertEquals("пока", result.getContent());
        assertEquals("image.jpg", result.getImageUrl());

        verify(messageValidator).validateMessageUpdate(updateRequest, owner);
        verify(messageRepository).findById(1L);
        verify(messageRepository).save(any(Message.class));
        verify(entityMapper).map(updatedMessage, MessageResponse.class);
        verify(accessValidator).validateMessageOwnership(owner, originalMessage);
    }

    @Test
    void editMessage_whenMessageNotFound() {
        User user = createTestUser(1L, "user", "user@example.com");
        MessageRequest request = createTestRequest(2L, "пока", "image.jpg");

        when(messageRepository.findById(1L)).thenReturn(Optional.empty());

        MessageNotFoundException exception = assertThrows(MessageNotFoundException.class,
                () -> messageService.editMessage(1L, request, user));

        assertEquals(ResponseMessageConstants.NOT_FOUND, exception.getMessage());
    }

    @Test
    void deleteMessage_whenMessageExistsAndUserIsOwner() {
        User owner = createTestUser(1L, "owner", "owner@example.com");
        User receiver = createTestUser(2L, "receiver", "receiver@example.com");
        Message message = createTestMessage(owner, receiver, "привет", null,
                MessageStatus.SENT, LocalDateTime.now(), LocalDateTime.now());
        message.setId(1L);

        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        assertDoesNotThrow(() -> messageService.deleteMessage(1L, owner));

        verify(messageRepository).findById(1L);
        verify(messageRepository).delete(message);
        verify(accessValidator).validateMessageOwnership(owner, message);
    }

    @Test
    void deleteMessage_whenMessageNotFound() {
        User user = createTestUser(1L, "user", "user@example.com");

        when(messageRepository.findById(1L)).thenReturn(Optional.empty());

        MessageNotFoundException exception = assertThrows(MessageNotFoundException.class,
                () -> messageService.deleteMessage(1L, user));

        assertEquals(ResponseMessageConstants.NOT_FOUND, exception.getMessage());
    }
}