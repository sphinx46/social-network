package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.providersImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Message;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.message.MessageNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.MessageRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.providers.MessageEntityProvider;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MessageEntityProviderImpl implements MessageEntityProvider {

    private final MessageRepository messageRepository;

    @Override
    public Message getById(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new MessageNotFoundException(ResponseMessageConstants.FAILURE_MESSAGE_NOT_FOUND));
    }

    @Override
    public Optional<Message> findById(Long id) {
        return messageRepository.findById(id);
    }
}