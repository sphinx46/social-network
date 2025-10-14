package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.MessageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.MessageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Message;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Role;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.MessageStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.custom.AccessDeniedException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.MessageNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.SelfMessageException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.UserNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.MessageRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.UserRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {
    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private MessageService messageService;

    @Test
    void create_whenRequestIsValid() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        User receiverUser = createTestUser(2L, "receiver", "123@example.com");

        Message message = createTestMessage(currentUser, receiverUser, "привет", null,
                MessageStatus.SENT, LocalDateTime.now(), LocalDateTime.now());

        MessageRequest request = createTestRequest(receiverUser.getId(), "привет", null);
        MessageResponse expectedResponse = createTestResponse(message);

        when(userRepository.findById(receiverUser.getId())).thenReturn(Optional.of(receiverUser));
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        when(modelMapper.map(message, MessageResponse.class)).thenReturn(expectedResponse);

        MessageResponse result = messageService.create(request, currentUser);

        assertNotNull(result);

        verify(userRepository).findById(receiverUser.getId());
        verify(messageRepository).save(any(Message.class));
        verify(modelMapper).map(message, MessageResponse.class);
    }

    @Test
    void create_whenUserIsNotExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");

        MessageRequest request = createTestRequest(2L, "привет", null);

        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class,
                () -> messageService.create(request, currentUser));

        assertEquals(ResponseMessageConstants.NOT_FOUND, userNotFoundException.getMessage());
    }

    @Test
    void create_whenMessageForSelf() {
        User currentUser = createTestUser(1L, "user", "example@example.com");

        MessageRequest request = createTestRequest(currentUser.getId(), "привет", null);

        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));

        SelfMessageException selfMessageException = assertThrows(SelfMessageException.class,
                () -> messageService.create(request, currentUser));

        assertEquals(ResponseMessageConstants.FAILURE_CREATE_SELF_MESSAGE, selfMessageException.getMessage());
    }

    @Test
    void markAsReceived_whenMessageIsExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        User receiverUser = createTestUser(2L, "receiver", "123@example.com");

        Message message = createTestMessage(currentUser, receiverUser, "привет", null,
                MessageStatus.SENT, LocalDateTime.now(), LocalDateTime.now());
        message.setId(1L);

        MessageResponse expectedResponse = createTestResponse(message);
        expectedResponse.setStatus(MessageStatus.RECEIVED);

        when(messageRepository.findById(message.getId())).thenReturn(Optional.of(message));
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        when(modelMapper.map(message, MessageResponse.class)).thenReturn(expectedResponse);

        MessageResponse result = messageService.markAsReceived(1L, receiverUser);

        assertNotNull(result);
        assertEquals(MessageStatus.RECEIVED, result.getStatus());

        verify(messageRepository).findById(1L);
        verify(messageRepository).save(any(Message.class));
        verify(modelMapper).map(message, MessageResponse.class);
    }

    @Test
    void markAsReceived_whenMessageNotExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");

        MessageNotFoundException messageNotFoundException = assertThrows(MessageNotFoundException.class,
                () -> messageService.markAsReceived(1L, currentUser));

        assertEquals(ResponseMessageConstants.NOT_FOUND, messageNotFoundException.getMessage());
    }

    @Test
    void markAsRead_whenMessageNotExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");

        MessageNotFoundException messageNotFoundException = assertThrows(MessageNotFoundException.class,
                () -> messageService.markAsRead(1L, currentUser));

        assertEquals(ResponseMessageConstants.NOT_FOUND, messageNotFoundException.getMessage());
    }


    @Test
    void markAsRead_whenMessageIsExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        User receiverUser = createTestUser(2L, "receiver", "123@example.com");

        Message message = createTestMessage(currentUser, receiverUser, "привет", null,
                MessageStatus.SENT, LocalDateTime.now(), LocalDateTime.now());
        message.setId(1L);

        MessageResponse expectedResponse = createTestResponse(message);
        expectedResponse.setStatus(MessageStatus.READ);

        when(messageRepository.findById(message.getId())).thenReturn(Optional.of(message));
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        when(modelMapper.map(message, MessageResponse.class)).thenReturn(expectedResponse);

        MessageResponse result = messageService.markAsRead(1L, receiverUser);

        assertNotNull(result);
        assertEquals(MessageStatus.READ, result.getStatus());

        verify(messageRepository).findById(1L);
        verify(messageRepository).save(any(Message.class));
        verify(modelMapper).map(message, MessageResponse.class);
    }

    @Test
    void editMessage_whenMessageIsValid() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        User receiverUser = createTestUser(2L, "receiver", "123@example.com");

        Message message = createTestMessage(currentUser, receiverUser, "привет", null,
                MessageStatus.SENT, LocalDateTime.now(), LocalDateTime.now());
        message.setId(1L);

        MessageRequest request = createTestRequest(receiverUser.getId(), "пока",
                "image.jpg");

        MessageResponse response = createTestResponse(message);
        response.setContent("пока");
        response.setImageUrl("image.jpg");


        when(messageRepository.findById(message.getId())).thenReturn(Optional.of(message));
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        when(modelMapper.map(message, MessageResponse.class)).thenReturn(response);

        MessageResponse updatedMessageResponse = messageService.editMessage(message.getId(),
                request, currentUser);

        assertNotNull(response);
        assertEquals(updatedMessageResponse.getImageUrl(), response.getImageUrl());
        assertEquals(updatedMessageResponse.getContent(), response.getContent());

        verify(messageRepository).findById(1L);
        verify(messageRepository).save(any(Message.class));
        verify(modelMapper).map(message, MessageResponse.class);
    }

    @Test
    void editMessage_whenMessageIsNotExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        User receiverUser = createTestUser(2L, "receiver", "123@example.com");
        MessageRequest request = createTestRequest(receiverUser.getId(), "пока",
                "image.jpg");

        MessageNotFoundException messageNotFoundException = assertThrows(MessageNotFoundException.class,
                () -> messageService.editMessage(1L, request, currentUser));

        assertEquals(ResponseMessageConstants.NOT_FOUND, messageNotFoundException.getMessage());
    }

    @Test
    void editMessage_whenUserIsNotOwner() {
        User senderUser = createTestUser(1L, "user", "example@example.com");
        User receiverUser = createTestUser(2L, "receiver", "123@example.com");

        Message message = createTestMessage(senderUser, receiverUser, "привет",
                null, MessageStatus.SENT, LocalDateTime.now(), LocalDateTime.now());
        message.setId(1L);

        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        User isNotOwner = createTestUser(3L, "not owner", "not@example.com");

        MessageRequest request = createTestRequest(receiverUser.getId(), "пока",
                "image.jpg");

        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class,
                () -> messageService.editMessage(message.getId(), request, isNotOwner));

        assertEquals(ResponseMessageConstants.ACCESS_DENIED, accessDeniedException.getMessage());
    }

    @Test
    void deleteMessage_whenAllIsValid() {
        User senderUser = createTestUser(1L, "user", "example@example.com");
        User receiverUser = createTestUser(2L, "receiver", "123@example.com");

        Message message = createTestMessage(senderUser, receiverUser, "привет",
                null, MessageStatus.SENT, LocalDateTime.now(), LocalDateTime.now());

        message.setId(1L);

        when(messageRepository.findById(message.getId())).thenReturn(Optional.of(message));

        assertDoesNotThrow(() -> messageService.deleteMessage(1L, senderUser));

        verify(messageRepository).findById(message.getId());
        verify(messageRepository).delete(message);
    }

    private User createTestUser(Long id, String username, String email) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("password");
        user.setCity("Moscow");
        user.setRole(Role.ROLE_USER);
        user.setCreatedAt(LocalDateTime.now());
        user.setOnline(false);
        return user;
    }

    private MessageRequest createTestRequest(Long receiverUserId, String content, String imageUrl) {
        return MessageRequest.builder()
                .receiverUserId(receiverUserId)
                .content(content)
                .imageUrl(imageUrl)
                .build();
    }

    private Message createTestMessage(User currentUser, User receiverUser, String content, String imageUrl,
                                      MessageStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return Message.builder()
                .sender(currentUser)
                .receiver(receiverUser)
                .content(content)
                .imageUrl(imageUrl)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    private MessageResponse createTestResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .senderUsername(message.getSender().getUsername())
                .receiverUsername(message.getReceiver().getUsername())
                .content(message.getContent())
                .imageUrl(message.getImageUrl())
                .status(message.getStatus())
                .createdAt(message.getCreatedAt())
                .build();
    }
}