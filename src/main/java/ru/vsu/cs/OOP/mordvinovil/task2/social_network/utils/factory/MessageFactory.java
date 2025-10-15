package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory;

import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.MessageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Message;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.MessageStatus;

import java.time.LocalDateTime;

@Component
public class MessageFactory {

    public Message createMessage(User sender, User receiver, MessageRequest request) {
        return Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .status(MessageStatus.SENT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}