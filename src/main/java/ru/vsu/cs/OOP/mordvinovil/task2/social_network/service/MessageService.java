package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.MessageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.MessageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Message;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.MessageStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.EventPublisherService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.MessageRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.MessageFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.MessageValidator;

import java.time.LocalDateTime;
import java.util.List;

import static ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.MessageStatusValidator.isStatusAllowed;


@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final EntityMapper entityMapper;
    private final MessageFactory messageFactory;
    private final MessageValidator messageValidator;
    private final EntityUtils entityUtils;
    private final EventPublisherService eventPublisherService;

    @Transactional
    public MessageResponse create(MessageRequest request, User currentUser) {
        messageValidator.validateMessageCreation(request, currentUser);

        User receiver = entityUtils.getUser(request.getReceiverUserId());
        Message message = messageFactory.createMessage(currentUser, receiver, request);
        Message savedMessage = messageRepository.save(message);

        eventPublisherService.publishMessageReceived(this, request.getReceiverUserId(),
                currentUser.getId(), message.getContent());

        return entityMapper.map(savedMessage, MessageResponse.class);
    }

    public MessageResponse getMessageById(Long messageId, User currentUser) {
        Message message = entityUtils.getMessage(messageId);
        messageValidator.validateMessageAccess(currentUser, message);
        return entityMapper.map(message, MessageResponse.class);
    }

    public List<MessageResponse> getConversation(Long otherUserId, User currentUser) {
        entityUtils.getUser(otherUserId);
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
        messageValidator.validateMessageUpdate(request, currentUser);

        Message message = entityUtils.getMessage(messageId);
        messageValidator.validateMessageOwnership(currentUser, message);

        message.setContent(request.getContent());
        message.setImageUrl(request.getImageUrl());
        message.setUpdatedAt(LocalDateTime.now());
        Message updatedMessage = messageRepository.save(message);

        return entityMapper.map(updatedMessage, MessageResponse.class);
    }

    @Transactional
    public void deleteMessage(Long messageId, User currentUser) {
        Message message = entityUtils.getMessage(messageId);
        messageValidator.validateMessageOwnership(currentUser, message);
        messageRepository.delete(message);

        eventPublisherService.publishMessageDeleted(this, message.getReceiver().getId(), currentUser.getId());
    }

    private List<MessageResponse> getMessagesByStatus(User currentUser, MessageStatus status) {
        List<Message> messages = messageRepository.findByReceiverIdAndStatus(currentUser.getId(), status)
                .orElse(List.of());
        return entityMapper.mapList(messages, MessageResponse.class);
    }

    private MessageResponse updateMessageStatus(Long messageId, User currentUser, MessageStatus newStatus, MessageStatus... allowedCurrentStatuses) {
        Message message = entityUtils.getMessage(messageId);
        messageValidator.validateMessageReceiver(currentUser, message);

        if (isStatusAllowed(message.getStatus(), allowedCurrentStatuses)) {
            message.setStatus(newStatus);
            message.setUpdatedAt(LocalDateTime.now());
            messageRepository.save(message);
        }

        return entityMapper.map(message, MessageResponse.class);
    }
}