package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.messaging;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.messaging.MessageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.common.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.messaging.MessageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.common.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.Service;

public interface MessageService extends Service<MessageRequest, User, MessageResponse> {
    MessageResponse create(MessageRequest request, User currentUser);
    MessageResponse getMessageById(Long messageId, User currentUser);
    PageResponse<MessageResponse> getConversation(Long otherUserId, User currentUser, PageRequest pageRequest);
    PageResponse<MessageResponse> getSentMessages(User currentUser, PageRequest pageRequest);
    PageResponse<MessageResponse> getReceivedMessages(User currentUser, PageRequest pageRequest);
    PageResponse<MessageResponse> getReadMessages(User currentUser, PageRequest pageRequest);
    MessageResponse markAsReceived(Long messageId, User currentUser);
    MessageResponse markAsRead(Long messageId, User currentUser);
    MessageResponse editMessage(Long messageId, MessageRequest request, User currentUser);
    void deleteMessage(Long messageId, User currentUser);
}
