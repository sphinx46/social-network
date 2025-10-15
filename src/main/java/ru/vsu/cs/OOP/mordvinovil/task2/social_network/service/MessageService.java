package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.MessageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.MessageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Message;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.MessageStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.MessageNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.UserNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.MessageRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.UserRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.AccessValidator;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.MessageFactory;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final EntityMapper entityMapper;
    private final MessageFactory messageFactory;
    private final AccessValidator accessValidator;

    @Transactional
    public MessageResponse create(MessageRequest request, User currentUser) {
        User receiver = getUserEntity(request.getReceiverUserId());
        accessValidator.validateSelfMessage(currentUser, receiver);

        Message message = messageFactory.createMessage(currentUser, receiver, request);
        Message savedMessage = messageRepository.save(message);

        return entityMapper.map(savedMessage, MessageResponse.class);
    }

    public MessageResponse getMessageById(Long messageId, User currentUser) {
        Message message = getMessageEntity(messageId);
        accessValidator.validateMessageAccess(currentUser, message);
        return entityMapper.map(message, MessageResponse.class);
    }

    public List<MessageResponse> getConversation(Long otherUserId, User currentUser) {
        getUserEntity(otherUserId);
        List<Message> messages = messageRepository.findMessagesBetweenUsers(currentUser.getId(), otherUserId)
                .orElse(List.of());
        return entityMapper.mapList(messages, MessageResponse.class);
    }

    public List<MessageResponse> getSentMessages(User currentUser) {
        List<Message> messages = messageRepository.findBySenderId(currentUser.getId())
                .orElse(List.of());
        return entityMapper.mapList(messages, MessageResponse.class);
    }

    public List<MessageResponse> getReceivedMessages(User currentUser) {
        return getMessagesByStatus(currentUser, MessageStatus.RECEIVED);
    }

    public List<MessageResponse> getReadMessages(User currentUser) {
        return getMessagesByStatus(currentUser, MessageStatus.READ);
    }

    @Transactional
    public MessageResponse markAsReceived(Long messageId, User currentUser) {
        return updateMessageStatus(messageId, currentUser, MessageStatus.RECEIVED, MessageStatus.SENT);
    }

    @Transactional
    public MessageResponse markAsRead(Long messageId, User currentUser) {
        return updateMessageStatus(messageId, currentUser, MessageStatus.READ, MessageStatus.RECEIVED, MessageStatus.SENT);
    }

    @Transactional
    public MessageResponse editMessage(Long messageId, MessageRequest request, User currentUser) {
        Message message = getMessageEntity(messageId);
        accessValidator.validateMessageOwnership(currentUser, message);

        message.setContent(request.getContent());
        message.setImageUrl(request.getImageUrl());
        message.setUpdatedAt(LocalDateTime.now());
        Message updatedMessage = messageRepository.save(message);

        return entityMapper.map(updatedMessage, MessageResponse.class);
    }

    @Transactional
    public void deleteMessage(Long messageId, User currentUser) {
        Message message = getMessageEntity(messageId);
        accessValidator.validateMessageOwnership(currentUser, message);
        messageRepository.delete(message);
    }

    private List<MessageResponse> getMessagesByStatus(User currentUser, MessageStatus status) {
        List<Message> messages = messageRepository.findByReceiverIdAndStatus(currentUser.getId(), status)
                .orElse(List.of());
        return entityMapper.mapList(messages, MessageResponse.class);
    }

    private MessageResponse updateMessageStatus(Long messageId, User currentUser, MessageStatus newStatus, MessageStatus... allowedCurrentStatuses) {
        Message message = getMessageEntity(messageId);
        accessValidator.validateMessageReceiver(currentUser, message);

        if (isStatusAllowed(message.getStatus(), allowedCurrentStatuses)) {
            message.setStatus(newStatus);
            message.setUpdatedAt(LocalDateTime.now());
            messageRepository.save(message);
        }

        return entityMapper.map(message, MessageResponse.class);
    }

    private boolean isStatusAllowed(MessageStatus currentStatus, MessageStatus... allowedStatuses) {
        for (MessageStatus allowed : allowedStatuses) {
            if (currentStatus == allowed) {
                return true;
            }
        }
        return false;
    }

    private User getUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(ResponseMessageConstants.NOT_FOUND));
    }

    private Message getMessageEntity(Long messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException(ResponseMessageConstants.NOT_FOUND));
    }
}