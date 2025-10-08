package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.MessageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.MessageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Message;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;


    @Transactional
    public MessageResponse create(MessageRequest request, User currentUser) {
        User receiver = userRepository.findById(request.getReceiverUserId())
                .orElseThrow(() -> new UserNotFoundException(ResponseMessageConstants.NOT_FOUND));


        if (currentUser.getId().equals(receiver.getId())) {
            throw new SelfMessageException(ResponseMessageConstants.FAILURE_CREATE_SELF_MESSAGE);
        }

        var message = Message.builder()
                .sender(currentUser)
                .receiver(receiver)
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .status(MessageStatus.SENT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();


        Message savedMessage = messageRepository.save(message);
        MessageResponse response = modelMapper.map(savedMessage, MessageResponse.class);
        return response;
    }

    public MessageResponse getMessageById(Long messageId, User currentUser) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException(ResponseMessageConstants.NOT_FOUND));


        if (!message.getSender().getId().equals(currentUser.getId()) &&
                message.getReceiver().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED);
        }

        return modelMapper.map(message, MessageResponse.class);
    }

    public List<MessageResponse> getConversation(Long otherUserId, User currentUser) {
        userRepository.findById(otherUserId)
                .orElseThrow(() -> new UserNotFoundException(ResponseMessageConstants.NOT_FOUND));

        List<Message> messageList = messageRepository.findMessagesBetweenUsers(currentUser.getId(), otherUserId)
                .orElse(List.of());

        return messageList.stream()
                .map(message -> modelMapper.map(message, MessageResponse.class))
                .toList();
    }

    public List<MessageResponse> getSentMessages(User currentUser) {
        List<Message> messageList = messageRepository.findBySenderId(currentUser.getId())
                .orElse(List.of());

        return messageList.stream()
                .map(message -> modelMapper.map(message, MessageResponse.class))
                .toList();
    }

    public List<MessageResponse> getReceivedMessages(User currentUser) {
        List<Message> messageList = messageRepository.findByReceiverIdAndStatus(currentUser.getId(), MessageStatus.RECEIVED)
                .orElse(List.of());

        return messageList.stream()
                .map(message -> modelMapper.map(message, MessageResponse.class))
                .toList();
    }

    public List<MessageResponse> getReadMessages(User currentUser) {
        List<Message> messageList = messageRepository.findByReceiverIdAndStatus(currentUser.getId(), MessageStatus.READ)
                .orElse(List.of());

        return messageList.stream()
                .map(message -> modelMapper.map(message, MessageResponse.class))
                .toList();
    }

    @Transactional
    public MessageResponse markAsReceived(Long messageId, User currentUser) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException(ResponseMessageConstants.NOT_FOUND));

        if (!message.getReceiver().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED);
        }

        if (message.getStatus() == MessageStatus.SENT) {
            message.setStatus(MessageStatus.RECEIVED);
            message.setUpdatedAt(LocalDateTime.now());
            messageRepository.save(message);
        }
        return modelMapper.map(message, MessageResponse.class);
    }

    @Transactional
    public MessageResponse markAsRead(Long messageId, User currentUser) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException(ResponseMessageConstants.NOT_FOUND));

        if (!message.getReceiver().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED);
        }

        if (message.getStatus() != MessageStatus.READ) {
            message.setStatus(MessageStatus.READ);
            message.setUpdatedAt(LocalDateTime.now());
            messageRepository.save(message);
        }

        return modelMapper.map(message, MessageResponse.class);
    }

    @Transactional
    public MessageResponse editMessage(Long messageId, MessageRequest request, User currentUser) {
        Message existingMessage = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException(ResponseMessageConstants.NOT_FOUND));

        if (!existingMessage.getSender().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED);
        }

        existingMessage.setContent(request.getContent());
        existingMessage.setImageUrl(request.getImageUrl());
        existingMessage.setUpdatedAt(LocalDateTime.now());
        Message updatedMessage = messageRepository.save(existingMessage);

        return modelMapper.map(updatedMessage, MessageResponse.class);
    }

    @Transactional
    public void deleteMessage(Long messageId, User currentUser) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException(ResponseMessageConstants.NOT_FOUND));

        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED);
        }
        messageRepository.delete(message);
    }
}