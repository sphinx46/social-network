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
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.MessageRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.MessageFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.MessageValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private MessageFactory messageFactory;

    @Mock
    private MessageValidator messageValidator;

    @Mock
    private EntityUtils entityUtils;

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

        when(entityUtils.getUser(receiverUser.getId())).thenReturn(receiverUser);
        when(messageFactory.createMessage(currentUser, receiverUser, request)).thenReturn(message);
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        when(entityMapper.map(message, MessageResponse.class)).thenReturn(expectedResponse);

        MessageResponse result = messageService.create(request, currentUser);

        assertNotNull(result);

        verify(messageValidator).validateMessageCreation(request, currentUser);
        verify(entityUtils).getUser(receiverUser.getId());
        verify(messageFactory).createMessage(currentUser, receiverUser, request);
        verify(messageRepository).save(any(Message.class));
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

        MessageResponse result = messageService.markAsReceived(1L, receiver);

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

        MessageResponse result = messageService.markAsRead(1L, receiver);

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

        MessageResponse result = messageService.editMessage(1L, updateRequest, owner);

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

        assertDoesNotThrow(() -> messageService.deleteMessage(1L, owner));

        verify(entityUtils).getMessage(1L);
        verify(messageRepository).delete(message);
        verify(messageValidator).validateMessageOwnership(owner, message);
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
        List<MessageResponse> responses = messages.stream().map(TestDataFactory::createTestResponse).toList();

        when(entityUtils.getUser(2L)).thenReturn(otherUser);
        when(messageRepository.findMessagesBetweenUsers(1L, 2L)).thenReturn(Optional.of(messages));
        when(entityMapper.mapList(messages, MessageResponse.class)).thenReturn(responses);

        List<MessageResponse> result = messageService.getConversation(2L, currentUser);

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(entityUtils).getUser(2L);
        verify(messageRepository).findMessagesBetweenUsers(1L, 2L);
        verify(entityMapper).mapList(messages, MessageResponse.class);
    }

    @Test
    void getSentMessages() {
        User currentUser = createTestUser(1L, "user", "user@example.com");
        Message message = createTestMessage(currentUser, createTestUser(2L, "receiver", "receiver@example.com"),
                "привет", null, MessageStatus.SENT, LocalDateTime.now(), LocalDateTime.now());
        List<Message> messages = List.of(message);
        List<MessageResponse> responses = messages.stream().map(TestDataFactory::createTestResponse).toList();

        when(messageRepository.findBySenderId(1L)).thenReturn(Optional.of(messages));
        when(entityMapper.mapList(messages, MessageResponse.class)).thenReturn(responses);

        List<MessageResponse> result = messageService.getSentMessages(currentUser);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(messageRepository).findBySenderId(1L);
        verify(entityMapper).mapList(messages, MessageResponse.class);
    }
}