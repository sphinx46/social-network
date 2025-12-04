package ru.cs.vsu.social_network.messaging_service.service;

import ru.cs.vsu.social_network.messaging_service.dto.request.messaging.MessageCreateRequest;
import ru.cs.vsu.social_network.messaging_service.dto.request.messaging.MessageUploadImageRequest;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.MessageResponse;

import java.util.UUID;

/**
 * WebSocket сервис для отправки уведомлений в реальном времени.
 * Интегрируется с MessagingService для отправки уведомлений о событиях через WebSocket.
 */
public interface WebSocketMessagingService {

    /**
     * Обрабатывает отправку нового сообщения с WebSocket уведомлениями.
     * Создает сообщение и отправляет уведомления отправителю и получателю.
     *
     * @param senderId идентификатор отправителя
     * @param request запрос на создание сообщения
     * @return DTO созданного сообщения
     */
    MessageResponse sendMessageWithNotification(UUID senderId, MessageCreateRequest request);

    /**
     * Обрабатывает отметку сообщений как прочитанных с WebSocket уведомлениями.
     * Отмечает сообщения как прочитанные и отправляет уведомление отправителю.
     *
     * @param userId идентификатор пользователя, читающего сообщения
     * @param conversationId идентификатор беседы
     * @return количество отмеченных сообщений
     */
    int markConversationAsReadWithNotification(UUID userId, UUID conversationId);

    /**
     * Обрабатывает загрузку изображения в сообщение с WebSocket уведомлениями.
     * Загружает изображение и отправляет уведомление получателю.
     *
     * @param userId идентификатор пользователя
     * @param request запрос на загрузку изображения
     * @return DTO обновленного сообщения
     */
    MessageResponse uploadMessageImageWithNotification(UUID userId, MessageUploadImageRequest request);

    /**
     * Отправляет уведомление о том, что пользователь начал или закончил печатать.
     *
     * @param conversationId идентификатор беседы
     * @param userId идентификатор пользователя
     * @param isTyping true если пользователь печатает, false если закончил
     */
    void sendTypingNotification(UUID conversationId, UUID userId, boolean isTyping);
}