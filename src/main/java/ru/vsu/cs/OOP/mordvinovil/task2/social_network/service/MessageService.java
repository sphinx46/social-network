package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.MessageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.MessageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;

import java.util.List;

public interface MessageService extends Service<MessageRequest, User, MessageResponse> {
    MessageResponse create(MessageRequest request, User currentUser);
    MessageResponse getMessageById(Long messageId, User currentUser);
    List<MessageResponse> getConversation(Long otherUserId, User currentUser);
    List<MessageResponse> getSentMessages(User currentUser);
    List<MessageResponse> getReceivedMessages(User currentUser);
    List<MessageResponse> getReadMessages(User currentUser);
    MessageResponse markAsReceived(Long messageId, User currentUser);
    MessageResponse markAsRead(Long messageId, User currentUser);
    MessageResponse editMessage(Long messageId, MessageRequest request, User currentUser);
    void deleteMessage(Long messageId, User currentUser);
}
