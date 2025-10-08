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

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public MessageResponse create(MessageRequest request) {
        User sender = userRepository.findById(request.getSenderUserId())
                .orElseThrow(() -> new UserNotFoundException(ResponseMessageConstants.NOT_FOUND));

        User receiver = userRepository.findById(request.getReceiverUserId())
                .orElseThrow(() -> new UserNotFoundException(ResponseMessageConstants.NOT_FOUND));

        if (sender.getId().equals(receiver.getId())) {
            throw new SelfMessageException(ResponseMessageConstants.FAILURE_CREATE_SELF_MESSAGE);
        }

        var message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(request.getContent())
                .imageUrl(request.getImageUrl() != null ? request.getImageUrl() : null)
                .status(MessageStatus.SENT)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();

        return modelMapper.map(message, MessageResponse.class);
    }

    public List<MessageResponse> getMessageListBetweenUsers(MessageRequest request, User currentUser) {
        User sender = userRepository.findById(request.getSenderUserId())
                .orElseThrow(() -> new UserNotFoundException(ResponseMessageConstants.NOT_FOUND));

        User receiver = userRepository.findById(request.getReceiverUserId())
                .orElseThrow(() -> new UserNotFoundException(ResponseMessageConstants.NOT_FOUND));
        List<Message> messageList = messageRepository.findMessagesBetweenUsers(sender.getId(), receiver.getId())
                .orElseThrow(() -> new MessageNotFoundException(ResponseMessageConstants.NOT_FOUND));

        if (!request.getSenderUserId().equals(currentUser.getId()) || !request.getReceiverUserId().equals(currentUser.getId())) {
            throw new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED);
        }

        return messageList.stream()
                .map(message -> modelMapper.map(message, MessageResponse.class))
                .toList();
    }

    private List<MessageResponse> getMessagesByStatus(MessageRequest request, MessageStatus status, User currentUser) {
        List<Message> messageList = messageRepository.findMessagesBetweenUsersByStatus(request.getSenderUserId(),
                request.getReceiverUserId(), status)
                .orElseThrow(() -> new MessageNotFoundException(ResponseMessageConstants.NOT_FOUND));

        if (!request.getSenderUserId().equals(currentUser.getId())) {
            throw new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED);
        }

        return messageList.stream()
                .map(message -> modelMapper.map(message, MessageResponse.class))
                .toList();
    }

    public List<MessageResponse> getSentMessages(MessageRequest request, User currentUser) {
        return getMessagesByStatus(request, MessageStatus.SENT, currentUser);
    }

    public List<MessageResponse> getReceivedMessages(MessageRequest request, User currentUser) {
        return getMessagesByStatus(request, MessageStatus.RECEIVED, currentUser);
    }

    public List<MessageResponse> getReadMessages(MessageRequest request, User currentUser) {
        return getMessagesByStatus(request, MessageStatus.READ, currentUser);
    }

    @Transactional
    private List<MessageResponse> updateStatusMessage(MessageRequest request, MessageStatus status, User currentUser) {
        List<Message> messageList = messageRepository.findMessagesBetweenUsers(request.getSenderUserId(), request.getReceiverUserId())
                .orElseThrow(() -> new MessageNotFoundException(ResponseMessageConstants.NOT_FOUND));;

        if (!request.getReceiverUserId().equals(currentUser.getId())) {
            throw new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED);
        }

        messageRepository.updateStatusMessages(request.getSenderUserId(), request.getReceiverUserId(), status);

        for (Message message : messageList) {
            message.setStatus(status);
            message.setUpdatedAt(LocalDate.now());
        }

        return messageList.stream()
                .map(message -> modelMapper.map(message, MessageResponse.class))
                .toList();
    }

    public List<MessageResponse> receiveMessages(MessageRequest request, User currentUser) {
        return updateStatusMessage(request, MessageStatus.RECEIVED, currentUser);
    }

    public List<MessageResponse> readMessages(MessageRequest request, User currentUser) {
        return updateStatusMessage(request, MessageStatus.READ, currentUser);
    }
}
