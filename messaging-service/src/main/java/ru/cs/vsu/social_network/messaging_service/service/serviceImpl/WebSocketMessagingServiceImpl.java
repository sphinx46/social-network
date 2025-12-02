package ru.cs.vsu.social_network.messaging_service.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cs.vsu.social_network.messaging_service.dto.request.messaging.MessageCreateRequest;
import ru.cs.vsu.social_network.messaging_service.dto.request.messaging.MessageUploadImageRequest;
import ru.cs.vsu.social_network.messaging_service.dto.request.websocket.MessageStatusUpdate;
import ru.cs.vsu.social_network.messaging_service.dto.request.websocket.TypingIndicator;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.MessageResponse;
import ru.cs.vsu.social_network.messaging_service.entity.enums.MessageStatus;
import ru.cs.vsu.social_network.messaging_service.exception.conversation.InterlocutorNotFoundException;
import ru.cs.vsu.social_network.messaging_service.provider.ConversationEntityProvider;
import ru.cs.vsu.social_network.messaging_service.service.MessagingService;
import ru.cs.vsu.social_network.messaging_service.service.WebSocketMessagingService;
import ru.cs.vsu.social_network.messaging_service.utils.MessageConstants;

import java.util.UUID;

/**
 * Реализация WebSocket сервиса для отправки уведомлений в реальном времени.
 * Обеспечивает интеграцию WebSocket уведомлений с основной бизнес-логикой мессенджера.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketMessagingServiceImpl implements WebSocketMessagingService {

    private final MessagingService messagingService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationEntityProvider conversationEntityProvider;

    private static final String USER_QUEUE_MESSAGES = "/queue/messages";
    private static final String USER_QUEUE_STATUS = "/queue/message-status";
    private static final String TOPIC_CONVERSATION = "/topic/conversation/";
    private static final String TOPIC_TYPING = "/topic/typing/";

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MessageResponse sendMessageWithNotification(UUID senderId, MessageCreateRequest request) {
        final String logPrefix = "WEBSOCKET_ОТПРАВКА_С_УВЕДОМЛЕНИЕМ";
        log.info("{}_НАЧАЛО: отправка сообщения от {} к {}", logPrefix, senderId, request.getReceiverId());

        MessageResponse messageResponse = messagingService.sendMessage(senderId, request);

        try {
            sendMessageToReceiver(request.getReceiverId(), messageResponse);
            notifyConversationParticipants(messageResponse.getConversationId(), messageResponse);

            log.info("{}_УСПЕХ: сообщение {} отправлено с WebSocket уведомлениями",
                    logPrefix, messageResponse.getMessageId());
        } catch (Exception e) {
            log.error("{}_ОШИБКА_WEBSOCKET: сообщение сохранено, " +
                            "но WebSocket уведомления не отправлены: {}",
                    logPrefix, e.getMessage());
        }

        return messageResponse;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public int markConversationAsReadWithNotification(UUID userId, UUID conversationId) {
        final String logPrefix = "WEBSOCKET_ОТМЕТКА_ПРОЧИТАННЫМИ_С_УВЕДОМЛЕНИЕМ";
        log.info("{}_НАЧАЛО: отметка беседы {} прочитанной пользователем {}", logPrefix, conversationId, userId);

        int markedCount = messagingService.markConversationAsRead(userId, conversationId);

        if (markedCount > 0) {
            try {
                UUID interlocutorId = conversationEntityProvider.getInterlocutorId(conversationId, userId)
                        .orElseThrow(() ->
                                new InterlocutorNotFoundException(MessageConstants.CONVERSATION_INTERLOCUTOR_NOT_FOUND_FAILURE));
                if (interlocutorId != null) {
                    sendReadStatusToSender(interlocutorId, conversationId, markedCount);
                    log.info("{}_УВЕДОМЛЕНИЕ_ОТПРАВЛЕНО: отправитель " +
                                    "{} уведомлен о прочтении {} сообщений",
                            logPrefix, interlocutorId, markedCount);
                }
            } catch (Exception e) {
                log.error("{}_ОШИБКА_WEBSOCKЕТ: сообщения отмечены прочитанными, " +
                                "но уведомление не отправлено: {}",
                        logPrefix, e.getMessage());
            }
        }

        log.info("{}_УСПЕХ: отмечено {} сообщений как прочитанные", logPrefix, markedCount);
        return markedCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MessageResponse uploadMessageImageWithNotification(UUID userId, MessageUploadImageRequest request) {
        final String logPrefix = "WEBSOCKET_ЗАГРУЗКА_ИЗОБРАЖЕНИЯ_С_УВЕДОМЛЕНИЕМ";
        log.info("{}_НАЧАЛО: загрузка изображения для сообщения {} пользователем {}",
                logPrefix, request.getMessageId(), userId);

        MessageResponse messageResponse = messagingService.uploadMessageImage(userId, request);

        try {
            notifyConversationParticipants(messageResponse.getConversationId(), messageResponse);
            log.info("{}_УВЕДОМЛЕНИЕ_ОТПРАВЛЕНО: участники беседы уведомлены об обновлении изображения", logPrefix);
        } catch (Exception e) {
            log.error("{}_ОШИБКА_WEBSOCKET: изображение загружено, но уведомление не отправлено: {}",
                    logPrefix, e.getMessage());
        }

        log.info("{}_УСПЕХ: изображение загружено для сообщения {}", logPrefix, request.getMessageId());
        return messageResponse;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendTypingNotification(UUID conversationId, UUID userId, boolean isTyping) {
        final String logPrefix = "WEBSOCKET_ИНДИКАТОР_ПЕЧАТАНИЯ";
        log.debug("{}_НАЧАЛО: отправка индикатора печатания в беседу {} от пользователя {}, isTyping: {}",
                logPrefix, conversationId, userId, isTyping);

        try {
            TypingIndicator typingIndicator = TypingIndicator.builder()
                    .userId(userId)
                    .conversationId(conversationId)
                    .isTyping(isTyping)
                    .build();

            String destination = TOPIC_TYPING + conversationId;
            messagingTemplate.convertAndSend(destination, typingIndicator);

            log.debug("{}_УСПЕХ: индикатор печатания отправлен в беседу {}", logPrefix, conversationId);
        } catch (Exception e) {
            log.error("{}_ОШИБКА: не удалось отправить индикатор печатания в беседу {}: {}",
                    logPrefix, conversationId, e.getMessage());
        }
    }

    /**
     * Отправляет сообщение конкретному получателю через приватную очередь.
     *
     * @param receiverId      идентификатор получателя
     * @param messageResponse DTO сообщения
     */
    private void sendMessageToReceiver(UUID receiverId, MessageResponse messageResponse) {
        messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                USER_QUEUE_MESSAGES,
                messageResponse
        );
    }

    /**
     * Отправляет уведомление всем участникам беседы через публичную тему.
     *
     * @param conversationId  идентификатор беседы
     * @param messageResponse DTO сообщения
     */
    private void notifyConversationParticipants(UUID conversationId, MessageResponse messageResponse) {
        String destination = TOPIC_CONVERSATION + conversationId;
        messagingTemplate.convertAndSend(destination, messageResponse);
    }

    /**
     * Отправляет уведомление отправителю о прочтении его сообщений.
     *
     * @param senderId       идентификатор отправителя
     * @param conversationId идентификатор беседы
     * @param readCount      количество прочитанных сообщений
     */
    private void sendReadStatusToSender(UUID senderId, UUID conversationId, int readCount) {
        MessageStatusUpdate statusUpdate = MessageStatusUpdate.builder()
                .conversationId(conversationId)
                .readCount(readCount)
                .status(MessageStatus.READ.name())
                .timestamp(System.currentTimeMillis())
                .build();

        messagingTemplate.convertAndSendToUser(
                senderId.toString(),
                USER_QUEUE_STATUS,
                statusUpdate
        );
    }
}

