package ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.servicesImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.messaging.MessageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Message;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.custom.AccessDeniedException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.message.MessageContentEmptyException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.message.MessageContentTooLongException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.message.SelfMessageException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.user.UserNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.UserRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.MessageValidator;

@Component
@RequiredArgsConstructor
public class MessageValidatorImpl implements MessageValidator {
    private final UserRepository userRepository;

    @Override
    public void validate(MessageRequest request, User currentUser) {
        validateMessageCreation(request, currentUser);
    }

    @Override
    public void validateMessageCreation(MessageRequest request, User currentUser) {
        User receiver = userRepository.findById(request.getReceiverUserId())
                .orElseThrow(() -> new UserNotFoundException(ResponseMessageConstants.FAILURE_USER_NOT_FOUND));

        if (currentUser.getId().equals(receiver.getId())) {
            throw new SelfMessageException(ResponseMessageConstants.FAILURE_CREATE_SELF_MESSAGE);
        }

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new MessageContentEmptyException(ResponseMessageConstants.FAILURE_MESSAGE_CONTENT_CANNOT_BE_EMPTY);
        }

        if (request.getContent().length() > 2000) {
            throw new MessageContentTooLongException(ResponseMessageConstants.FAILURE_MESSAGE_CONTENT_TOO_LONG);
        }
    }

    @Override
    public void validateMessageOwnership(User currentUser, Message message) {
        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED);
        }
    }

    @Override
    public void validateMessageAccess(User currentUser, Message message) {
        if (!message.getSender().getId().equals(currentUser.getId()) &&
                !message.getReceiver().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED);
        }
    }

    @Override
    public void validateMessageReceiver(User currentUser, Message message) {
        if (!message.getReceiver().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED);
        }
    }

    @Override
    public void validateMessageUpdate(MessageRequest request, User currentUser) {
        if (request.getContent() != null && request.getContent().length() > 2000) {
            throw new MessageContentTooLongException(ResponseMessageConstants.FAILURE_MESSAGE_CONTENT_TOO_LONG);
        }
    }
}