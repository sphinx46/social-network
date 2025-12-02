package ru.cs.vsu.social_network.messaging_service.validation;


import ru.cs.vsu.social_network.messaging_service.entity.Message;

/**
 * Валидатор для проверки прав доступа к сообщениям.
 * Обеспечивает проверку владения и прав доступа пользователей к операциям с сообщениями.
 */
public interface MessageValidator extends GenericValidator<Message> {
}