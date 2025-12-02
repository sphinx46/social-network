package ru.cs.vsu.social_network.messaging_service.service.batch.batchImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.MessageResponse;
import ru.cs.vsu.social_network.messaging_service.entity.Message;
import ru.cs.vsu.social_network.messaging_service.entity.enums.MessageStatus;
import ru.cs.vsu.social_network.messaging_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.messaging_service.provider.MessageEntityProvider;
import ru.cs.vsu.social_network.messaging_service.service.batch.BatchMessageService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для пакетных операций с сообщениями.
 * Обеспечивает эффективное получение сообщений для множества бесед и пользователей.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BatchMessageServiceImpl implements BatchMessageService {

    private final MessageEntityProvider messageEntityProvider;
    private final EntityMapper entityMapper;

    private static final int MAX_BATCH_SIZE = 1000;
    private static final int BATCH_QUERY_SIZE = 100;
    private static final int DEFAULT_MESSAGES_LIMIT = 10;

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getUnreadMessagesCountByUser(UUID receiverId, MessageStatus status) {
        log.debug("BATCH_MESSAGE_SERVICE_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_НЕПРОЧИТАННЫХ_ПОЛЬЗОВАТЕЛЯ_НАЧАЛО: " +
                "для пользователя {}, статус: {}", receiverId, status);

        final Long count = messageEntityProvider.getUnreadMessagesCountByUser(receiverId, status);

        log.debug("BATCH_MESSAGE_SERVICE_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_НЕПРОЧИТАННЫХ_ПОЛЬЗОВАТЕЛЯ_УСПЕХ: " +
                        "для пользователя {} найдено {} сообщений со статусом {}",
                receiverId, count, status);

        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<UUID, Long> getUnreadMessagesCountInConversations(UUID receiverId,
                                                                 List<UUID> conversationIds,
                                                                 MessageStatus status) {
        log.debug("BATCH_MESSAGE_SERVICE_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_НЕПРОЧИТАННЫХ_БЕСЕД_НАЧАЛО: " +
                        "для пользователя {}, {} бесед, статус: {}",
                receiverId, conversationIds.size(), status);

        if (conversationIds.isEmpty()) {
            log.debug("BATCH_MESSAGE_SERVICE_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_НЕПРОЧИТАННЫХ_БЕСЕД_ПУСТОЙ_СПИСОК");
            return Collections.emptyMap();
        }

        final List<UUID> batchConversationIds = conversationIds.size() > MAX_BATCH_SIZE ?
                conversationIds.subList(0, MAX_BATCH_SIZE) : conversationIds;

        final Map<UUID, Long> result = new ConcurrentHashMap<>();

        for (int i = 0; i < batchConversationIds.size(); i += BATCH_QUERY_SIZE) {
            List<UUID> subList = batchConversationIds.subList(
                    i, Math.min(i + BATCH_QUERY_SIZE, batchConversationIds.size())
            );

            subList.forEach(conversationId -> {
                Long count = messageEntityProvider.getUnreadMessagesCountInConversation(
                        receiverId, conversationId, status
                );
                result.put(conversationId, count);
            });
        }

        log.debug("BATCH_MESSAGE_SERVICE_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_НЕПРОЧИТАННЫХ_БЕСЕД_УСПЕХ: " +
                "получено количество непрочитанных сообщений для {} бесед", result.size());

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<UUID, List<MessageResponse>> getRecentMessagesForConversations(List<UUID> conversationIds,
                                                                              int messagesLimit) {
        log.debug("BATCH_MESSAGE_SERVICE_ПОЛУЧЕНИЕ_ПОСЛЕДНИХ_СООБЩЕНИЙ_НАЧАЛО: " +
                "для {} бесед с лимитом {}", conversationIds.size(), messagesLimit);

        if (conversationIds.isEmpty()) {
            log.debug("BATCH_MESSAGE_SERVICE_ПОЛУЧЕНИЕ_ПОСЛЕДНИХ_СООБЩЕНИЙ_ПУСТОЙ_СПИСОК");
            return Collections.emptyMap();
        }

        final List<UUID> batchConversationIds = conversationIds.size() > MAX_BATCH_SIZE ?
                conversationIds.subList(0, MAX_BATCH_SIZE) : conversationIds;

        final int effectiveLimit = Math.max(1, Math.min(messagesLimit, DEFAULT_MESSAGES_LIMIT));

        final List<Message> allMessages =
                messageEntityProvider.getRecentMessagesForConversations(batchConversationIds,
                        effectiveLimit);

        final Map<UUID, List<MessageResponse>> result = new ConcurrentHashMap<>();
        batchConversationIds.forEach(conversationId -> result.put(conversationId,
                new ArrayList<>()));

        Map<UUID, List<Message>> messagesByConversationId = allMessages.stream()
                .filter(message -> message.getConversation() != null)
                .collect(Collectors.groupingBy(message -> message.getConversation().getId()));

        messagesByConversationId.forEach((conversationId, messages) -> {
            List<MessageResponse> responses = messages.stream()
                    .limit(effectiveLimit)
                    .map(message -> entityMapper.map(message, MessageResponse.class))
                    .collect(Collectors.toList());
            result.put(conversationId, responses);
        });

        log.debug("BATCH_MESSAGE_SERVICE_ПОЛУЧЕНИЕ_ПОСЛЕДНИХ_СООБЩЕНИЙ_УСПЕХ: " +
                "получены сообщения для {} бесед", result.size());

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MessageResponse> getMessagesByConversation(UUID conversationId, int page, int size) {
        log.debug("BATCH_MESSAGE_SERVICE_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_БЕСЕДЫ_НАЧАЛО: " +
                "для беседы {}, страница: {}, размер: {}", conversationId, page, size);

        final int effectivePage = Math.max(0, page);
        final int effectiveSize = Math.max(1, Math.min(size, MAX_BATCH_SIZE));

        final List<Message> messages =
                messageEntityProvider.getMessagesByConversation(conversationId,
                        effectivePage, effectiveSize);

        final List<MessageResponse> messageResponses = messages.stream()
                .map(message -> entityMapper.map(message, MessageResponse.class))
                .collect(Collectors.toList());

        log.debug("BATCH_MESSAGE_SERVICE_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_БЕСЕДЫ_УСПЕХ: " +
                "для беседы {} найдено {} сообщений", conversationId, messageResponses.size());

        return messageResponses;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MessageResponse> getMessagesBetweenUsers(UUID senderId,
                                                         UUID receiverId,
                                                         int page,
                                                         int size) {
        log.debug("BATCH_MESSAGE_SERVICE_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_МЕЖДУ_ПОЛЬЗОВАТЕЛЯМИ_НАЧАЛО: " +
                "от {} к {}, страница: {}, размер: {}", senderId, receiverId, page, size);

        final int effectivePage = Math.max(0, page);
        final int effectiveSize = Math.max(1, Math.min(size, MAX_BATCH_SIZE));

        final List<Message> messages =
                messageEntityProvider.getMessagesBetweenUsers(senderId, receiverId,
                        effectivePage, effectiveSize);

        final List<MessageResponse> messageResponses = messages.stream()
                .map(message -> entityMapper.map(message, MessageResponse.class))
                .collect(Collectors.toList());

        log.debug("BATCH_MESSAGE_SERVICE_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_МЕЖДУ_ПОЛЬЗОВАТЕЛЯМИ_УСПЕХ: " +
                        "между пользователями {} и {} найдено {} сообщений",
                senderId, receiverId, messageResponses.size());

        return messageResponses;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MessageResponse> getMessagesWithConversations(List<UUID> messageIds) {
        log.debug("BATCH_MESSAGE_SERVICE_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_С_БЕСЕДАМИ_НАЧАЛО: " +
                "для {} сообщений", messageIds.size());

        if (messageIds.isEmpty()) {
            log.debug("BATCH_MESSAGE_SERVICE_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_С_БЕСЕДАМИ_ПУСТОЙ_СПИСОК");
            return Collections.emptyList();
        }

        final List<UUID> batchMessageIds = messageIds.size() > MAX_BATCH_SIZE ?
                messageIds.subList(0, MAX_BATCH_SIZE) : messageIds;

        final List<Message> messages =
                messageEntityProvider.getMessagesWithConversations(batchMessageIds);

        final List<MessageResponse> messageResponses = messages.stream()
                .map(message -> entityMapper.map(message, MessageResponse.class))
                .collect(Collectors.toList());

        log.debug("BATCH_MESSAGE_SERVICE_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_С_БЕСЕДАМИ_УСПЕХ: " +
                "получено {} сообщений с беседами", messageResponses.size());

        return messageResponses;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MessageResponse> getUnreadMessagesInConversation(UUID receiverId,
                                                                 UUID conversationId,
                                                                 MessageStatus status) {
        log.debug("BATCH_MESSAGE_SERVICE_ПОЛУЧЕНИЕ_НЕПРОЧИТАННЫХ_В_БЕСЕДЕ_НАЧАЛО: " +
                        "для пользователя {} в беседе {} со статусом {}",
                receiverId, conversationId, status);

        final List<Message> messages =
                messageEntityProvider.getUnreadMessagesInConversation(receiverId, conversationId, status);

        final List<MessageResponse> messageResponses = messages.stream()
                .map(message -> entityMapper.map(message, MessageResponse.class))
                .collect(Collectors.toList());

        log.debug("BATCH_MESSAGE_SERVICE_ПОЛУЧЕНИЕ_НЕПРОЧИТАННЫХ_В_БЕСЕДЕ_УСПЕХ: " +
                        "найдено {} непрочитанных сообщений для пользователя {} в беседе {}",
                messageResponses.size(), receiverId, conversationId);

        return messageResponses;
    }
}