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
import java.util.stream.Collectors;

/**
 * Реализация сервиса для пакетных операций с сообщениями.
 * Обеспечивает эффективное получение сообщений для множества бесед и пользователей
 * с использованием батч-запросов для оптимизации производительности.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BatchMessageServiceImpl implements BatchMessageService {

    private final MessageEntityProvider messageEntityProvider;
    private final EntityMapper entityMapper;

    private static final int MAX_BATCH_SIZE = 1000;
    private static final int BATCH_QUERY_SIZE = 50;
    private static final int MAX_MESSAGES_LIMIT = 100;

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getUnreadMessagesCountByUser(UUID receiverId, MessageStatus status) {
        final String logPrefix = "BATCH_MESSAGE_SERVICE_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_НЕПРОЧИТАННЫХ_ПОЛЬЗОВАТЕЛЯ";
        log.debug("{}_НАЧАЛО: для пользователя {}, статус: {}", logPrefix, receiverId, status);

        try {
            final Long count = messageEntityProvider.getUnreadMessagesCountByUser(receiverId, status);
            log.debug("{}_УСПЕХ: для пользователя {} найдено {} сообщений со статусом {}",
                    logPrefix, receiverId, count, status);
            return count;
        } catch (Exception e) {
            log.error("{}_ОШИБКА: для пользователя {}, статус: {}, ошибка: {}",
                    logPrefix, receiverId, status, e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<UUID, Long> getUnreadMessagesCountInConversations(UUID receiverId,
                                                                 List<UUID> conversationIds,
                                                                 MessageStatus status) {
        final String logPrefix = "BATCH_MESSAGE_SERVICE_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_НЕПРОЧИТАННЫХ_БЕСЕД";
        log.debug("{}_НАЧАЛО: для пользователя {}, бесед: {}, статус: {}",
                logPrefix, receiverId, conversationIds.size(), status);

        if (conversationIds.isEmpty()) {
            log.debug("{}_ПУСТОЙ_СПИСОК", logPrefix);
            return Collections.emptyMap();
        }

        try {
            final Map<UUID, Long> result = new HashMap<>();
            final List<UUID> batchConversationIds = conversationIds.size() > MAX_BATCH_SIZE ?
                    conversationIds.subList(0, MAX_BATCH_SIZE) : conversationIds;

            for (int i = 0; i < batchConversationIds.size(); i += BATCH_QUERY_SIZE) {
                int endIndex = Math.min(i + BATCH_QUERY_SIZE, batchConversationIds.size());
                List<UUID> batch = batchConversationIds.subList(i, endIndex);

                for (UUID conversationId : batch) {
                    Long count = messageEntityProvider.getUnreadMessagesCountInConversation(
                            receiverId, conversationId, status
                    );
                    result.put(conversationId, count);
                }
            }

            log.debug("{}_УСПЕХ: получено количество непрочитанных сообщений для {} бесед",
                    logPrefix, result.size());
            return result;
        } catch (Exception e) {
            log.error("{}_ОШИБКА: для пользователя {}, бесед: {}, ошибка: {}",
                    logPrefix, receiverId, conversationIds.size(), e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<UUID, List<MessageResponse>> getRecentMessagesForConversations(List<UUID> conversationIds,
                                                                              int messagesLimit) {
        final String logPrefix = "BATCH_MESSAGE_SERVICE_ПОЛУЧЕНИЕ_ПОСЛЕДНИХ_СООБЩЕНИЙ";
        log.debug("{}_НАЧАЛО: для {} бесед с лимитом {}", logPrefix, conversationIds.size(),
                messagesLimit);

        if (conversationIds.isEmpty()) {
            log.debug("{}_ПУСТОЙ_СПИСОК", logPrefix);
            return Collections.emptyMap();
        }

        try {
            final List<UUID> batchConversationIds = conversationIds.size() > MAX_BATCH_SIZE ?
                    conversationIds.subList(0, MAX_BATCH_SIZE) : conversationIds;

            final int effectiveLimit = Math.max(1, Math.min(messagesLimit, MAX_MESSAGES_LIMIT));

            final List<Message> allMessages =
                    messageEntityProvider.getRecentMessagesForConversations(batchConversationIds,
                            effectiveLimit);

            final Map<UUID, List<Message>> messagesByConversation = allMessages.stream()
                    .filter(message -> message.getConversation() != null &&
                            message.getConversation().getId() != null)
                    .collect(Collectors.groupingBy(message ->
                            message.getConversation().getId()));

            final Map<UUID, List<MessageResponse>> result = new HashMap<>();

            for (UUID conversationId : batchConversationIds) {
                List<Message> messages = messagesByConversation.get(conversationId);
                if (messages != null && !messages.isEmpty()) {
                    List<MessageResponse> responses = messages.stream()
                            .sorted((m1, m2) -> m2.getCreatedAt().compareTo(m1.getCreatedAt()))
                            .limit(effectiveLimit)
                            .map(message -> entityMapper.map(message, MessageResponse.class))
                            .collect(Collectors.toList());
                    result.put(conversationId, responses);
                } else {
                    result.put(conversationId, Collections.emptyList());
                }
            }

            log.debug("{}_УСПЕХ: получены сообщения для {} бесед", logPrefix, result.size());
            return result;
        } catch (Exception e) {
            log.error("{}_ОШИБКА: для {} бесед, ошибка: {}",
                    logPrefix, conversationIds.size(), e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MessageResponse> getMessagesByConversation(UUID conversationId, int page, int size) {
        final String logPrefix = "BATCH_MESSAGE_SERVICE_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_БЕСЕДЫ";
        log.debug("{}_НАЧАЛО: для беседы {}, страница: {}, размер: {}", logPrefix, conversationId, page, size);

        try {
            final int effectivePage = Math.max(0, page);
            final int effectiveSize = Math.max(1, Math.min(size, MAX_BATCH_SIZE));

            final List<Message> messages =
                    messageEntityProvider.getMessagesByConversation(conversationId,
                            effectivePage, effectiveSize);

            final List<MessageResponse> messageResponses = messages.stream()
                    .map(message -> entityMapper.map(message, MessageResponse.class))
                    .collect(Collectors.toList());

            log.debug("{}_УСПЕХ: для беседы {} найдено {} сообщений",
                    logPrefix, conversationId, messageResponses.size());
            return messageResponses;
        } catch (Exception e) {
            log.error("{}_ОШИБКА: для беседы {}, страница: {}, ошибка: {}",
                    logPrefix, conversationId, page, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MessageResponse> getMessagesBetweenUsers(UUID senderId,
                                                         UUID receiverId,
                                                         int page,
                                                         int size) {
        final String logPrefix = "BATCH_MESSAGE_SERVICE_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_МЕЖДУ_ПОЛЬЗОВАТЕЛЯМИ";
        log.debug("{}_НАЧАЛО: от {} к {}, страница: {}, размер: {}",
                logPrefix, senderId, receiverId, page, size);

        try {
            final int effectivePage = Math.max(0, page);
            final int effectiveSize = Math.max(1, Math.min(size, MAX_BATCH_SIZE));

            final List<Message> messages =
                    messageEntityProvider.getMessagesBetweenUsers(senderId, receiverId, effectivePage, effectiveSize);

            final List<MessageResponse> messageResponses = messages.stream()
                    .map(message -> entityMapper.map(message, MessageResponse.class))
                    .collect(Collectors.toList());

            log.debug("{}_УСПЕХ: между пользователями {} и {} найдено {} сообщений",
                    logPrefix, senderId, receiverId, messageResponses.size());
            return messageResponses;
        } catch (Exception e) {
            log.error("{}_ОШИБКА: от {} к {}, страница: {}, ошибка: {}",
                    logPrefix, senderId, receiverId, page, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MessageResponse> getMessagesWithConversations(List<UUID> messageIds) {
        final String logPrefix = "BATCH_MESSAGE_SERVICE_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_С_БЕСЕДАМИ";
        log.debug("{}_НАЧАЛО: для {} сообщений", logPrefix, messageIds.size());

        if (messageIds.isEmpty()) {
            log.debug("{}_ПУСТОЙ_СПИСОК", logPrefix);
            return Collections.emptyList();
        }

        try {
            final List<UUID> batchMessageIds = messageIds.size() > MAX_BATCH_SIZE ?
                    messageIds.subList(0, MAX_BATCH_SIZE) : messageIds;

            final List<Message> messages =
                    messageEntityProvider.getMessagesWithConversations(batchMessageIds);

            final List<MessageResponse> messageResponses = messages.stream()
                    .map(message -> entityMapper.map(message, MessageResponse.class))
                    .collect(Collectors.toList());

            log.debug("{}_УСПЕХ: получено {} сообщений с беседами",
                    logPrefix, messageResponses.size());
            return messageResponses;
        } catch (Exception e) {
            log.error("{}_ОШИБКА: для {} сообщений, ошибка: {}",
                    logPrefix, messageIds.size(), e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MessageResponse> getUnreadMessagesInConversation(UUID receiverId,
                                                                 UUID conversationId,
                                                                 MessageStatus status) {
        final String logPrefix = "BATCH_MESSAGE_SERVICE_ПОЛУЧЕНИЕ_НЕПРОЧИТАННЫХ_В_БЕСЕДЕ";
        log.debug("{}_НАЧАЛО: для пользователя {} в беседе {} со статусом {}",
                logPrefix, receiverId, conversationId, status);

        try {
            final List<Message> messages =
                    messageEntityProvider.getUnreadMessagesInConversation(receiverId, conversationId, status);

            final List<MessageResponse> messageResponses = messages.stream()
                    .map(message -> entityMapper.map(message, MessageResponse.class))
                    .collect(Collectors.toList());

            log.debug("{}_УСПЕХ: найдено {} непрочитанных сообщений для пользователя {} в беседе {}",
                    logPrefix, messageResponses.size(), receiverId, conversationId);
            return messageResponses;
        } catch (Exception e) {
            log.error("{}_ОШИБКА: для пользователя {} в беседе {}, ошибка: {}",
                    logPrefix, receiverId, conversationId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}