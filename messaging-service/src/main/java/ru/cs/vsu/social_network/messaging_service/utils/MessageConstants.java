package ru.cs.vsu.social_network.messaging_service.utils;

/**
 * Константы сообщений для всего приложения.
 * Содержит стандартизированные тексты сообщений и ошибок.
 */
public class MessageConstants {

    // Message
    public static final String MESSAGE_NOT_FOUND_FAILURE = "Ошибка! Сообщение не найдено";
    public static final String MESSAGE_UPLOAD_IMAGE_FAILURE = "Ошибка! Ссылка на изображение для сообщения невалидна.";

    // SECURITY
    public static final String ACCESS_DENIED = "Доступ запрещен";

    // Conversation
    public static final String CONVERSATION_NOT_FOUND_FAILURE = "Ошибка! Переписка не найдена";
    public static final String CONVERSATION_TO_SELF_FAILURE = "Ошибка! Нельзя начать переписку с собой.";
    public static final String CONVERSATION_INTERLOCUTOR_NOT_FOUND_FAILURE = "Ошибка! Собеседник не найден";
}