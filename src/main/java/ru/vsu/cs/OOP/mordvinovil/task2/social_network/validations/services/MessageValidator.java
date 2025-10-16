package ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.MessageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;

public interface MessageValidator extends Validator<MessageRequest, User> {
    void validateMessageCreation(MessageRequest request, User currentUser);
    void validateMessageUpdate(MessageRequest request, User currentUser);
}
