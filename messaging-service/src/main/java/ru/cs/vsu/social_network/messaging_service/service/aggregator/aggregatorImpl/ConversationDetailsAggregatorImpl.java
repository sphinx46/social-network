package ru.cs.vsu.social_network.messaging_service.service.aggregator.aggregatorImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.ConversationDetailsResponse;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.MessageResponse;
import ru.cs.vsu.social_network.messaging_service.entity.Conversation;
import ru.cs.vsu.social_network.messaging_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.messaging_service.service.aggregator.ConversationDetailsAggregator;
import ru.cs.vsu.social_network.messaging_service.service.batch.BatchMessageService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация агрегатора данных для бесед.
 * Обеспечивает сборку расширенных данных бесед с загрузкой сообщений и метаданных.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConversationDetailsAggregatorImpl implements ConversationDetailsAggregator {

    private final EntityMapper mapper;
    private final BatchMessageService batchMessageService;

    private static final int MAX_MESSAGES_LIMIT = 50;

    /**
     * {@inheritDoc}
     * Оптимизированная реализация с обработкой граничных случаев.
     */
    @Override
    public ConversationDetailsResponse aggregateConversationDetails(final Conversation conversation,
                                                                    final boolean includeMessages,
                                                                    final int messagesLimit) {
        final String logPrefix = "CONVERSATION_DETAILS_AGGREGATOR_АГРЕГАЦИЯ_БЕСЕДЫ";
        log.debug("{}_НАЧАЛО: для беседы ID: {}, включить сообщения: {}, лимит: {}",
                logPrefix, conversation.getId(), includeMessages, messagesLimit);

        try {
            if (conversation == null) {
                log.error("{}_ОШИБКА: беседа не может быть null", logPrefix);
                throw new IllegalArgumentException("Conversation cannot be null");
            }

            final ConversationDetailsResponse conversationDetailsResponse =
                    mapper.map(conversation, ConversationDetailsResponse.class);

            final UUID conversationId = conversation.getId();

            if (includeMessages && messagesLimit > 0) {
                final int effectiveLimit = Math.min(messagesLimit, MAX_MESSAGES_LIMIT);
                final List<MessageResponse> messages =
                        batchMessageService.getMessagesByConversation(conversationId,
                                0, effectiveLimit);

                conversationDetailsResponse.setMessages(messages);
                conversationDetailsResponse.setMessagesCount((long) messages.size());

                if (!messages.isEmpty()) {
                    final MessageResponse lastMessage = messages.stream()
                            .max(Comparator.comparing(MessageResponse::getCreatedAt))
                            .orElse(messages.get(0));
                    conversationDetailsResponse.setLastMessageId(lastMessage.getMessageId());
                } else {
                    conversationDetailsResponse.setLastMessageId(null);
                }
            } else {
                conversationDetailsResponse.setMessages(Collections.emptyList());
                conversationDetailsResponse.setMessagesCount(0L);
                conversationDetailsResponse.setLastMessageId(null);
            }

            log.debug("{}_УСПЕХ: для беседы ID: {} агрегировано {} сообщений",
                    logPrefix, conversation.getId(), conversationDetailsResponse.getMessagesCount());
            return conversationDetailsResponse;
        } catch (Exception e) {
            log.error("{}_ОШИБКА: для беседы ID: {}, ошибка: {}",
                    logPrefix, conversation != null ? conversation.getId() : "null", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<ConversationDetailsResponse> aggregateConversationsPage(final Page<Conversation> conversationsPage,
                                                                        final boolean includeMessages,
                                                                        final int messagesLimit) {
        final String logPrefix = "CONVERSATION_DETAILS_AGGREGATOR_АГРЕГАЦИЯ_СТРАНИЦЫ_БЕСЕД";
        log.debug("{}_НАЧАЛО: для {} бесед, включить сообщения: {}, лимит: {}",
                logPrefix, conversationsPage.getNumberOfElements(), includeMessages, messagesLimit);

        try {
            final List<Conversation> conversations = conversationsPage.getContent();
            if (conversations.isEmpty()) {
                log.debug("{}_ПУСТАЯ_СТРАНИЦА", logPrefix);
                return new PageImpl<>(Collections.emptyList(),
                        conversationsPage.getPageable(), conversationsPage.getTotalElements());
            }

            final Map<UUID, Conversation> conversationMap = conversations.stream()
                    .collect(Collectors.toMap(Conversation::getId, conversation -> conversation));

            final List<UUID> conversationIds = new ArrayList<>(conversationMap.keySet());

            Map<UUID, List<MessageResponse>> messagesMap = Collections.emptyMap();
            if (includeMessages && messagesLimit > 0) {
                final int effectiveLimit = Math.min(messagesLimit, MAX_MESSAGES_LIMIT);
                messagesMap = batchMessageService.getRecentMessagesForConversations(conversationIds,
                        effectiveLimit);

                log.debug("{}_ПОЛУЧЕНО_СООБЩЕНИЙ: для {} из {} бесед",
                        logPrefix, messagesMap.size(), conversationIds.size());
            }

            Map<UUID, List<MessageResponse>> finalMessagesMap = messagesMap;
            final List<ConversationDetailsResponse> detailedConversations = conversationIds.stream()
                    .map(conversationId -> buildConversationDetailsResponse(
                            conversationMap.get(conversationId),
                            finalMessagesMap.getOrDefault(conversationId, Collections.emptyList()),
                            includeMessages))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            log.debug("{}_УСПЕХ: агрегировано {} бесед из {} запрошенных",
                    logPrefix, detailedConversations.size(), conversations.size());
            return new PageImpl<>(detailedConversations, conversationsPage.getPageable(),
                    conversationsPage.getTotalElements());
        } catch (Exception e) {
            log.error("{}_ОШИБКА: для {} бесед, ошибка: {}",
                    logPrefix, conversationsPage.getNumberOfElements(), e.getMessage(), e);
            return new PageImpl<>(Collections.emptyList(),
                    conversationsPage.getPageable(), conversationsPage.getTotalElements());
        }
    }

    /**
     * Строит детальный ответ для беседы.
     * Включает сообщения, количество сообщений и определяет последнее сообщение.
     * Оптимизировано для обработки граничных случаев.
     *
     * @param conversation сущность беседы
     * @param messages список сообщений для беседы
     * @param includeMessages флаг включения сообщений
     * @return детальный ответ беседы или null при ошибке
     */
    private ConversationDetailsResponse buildConversationDetailsResponse(final Conversation conversation,
                                                                         final List<MessageResponse> messages,
                                                                         final boolean includeMessages) {
        if (conversation == null) {
            log.warn("CONVERSATION_DETAILS_AGGREGATOR_ОПУСКАЕМ_БЕСЕДУ: беседа не может быть null");
            return null;
        }

        try {
            final ConversationDetailsResponse response =
                    mapper.map(conversation, ConversationDetailsResponse.class);

            if (includeMessages) {
                response.setMessages(messages != null ? messages : Collections.emptyList());
                response.setMessagesCount((long) (messages != null ? messages.size() : 0));

                if (messages != null && !messages.isEmpty()) {
                    final MessageResponse lastMessage = messages.stream()
                            .max(Comparator.comparing(MessageResponse::getCreatedAt))
                            .orElse(messages.get(0));
                    response.setLastMessageId(lastMessage.getMessageId());
                } else {
                    response.setLastMessageId(null);
                }
            } else {
                response.setMessages(Collections.emptyList());
                response.setMessagesCount(0L);
                response.setLastMessageId(null);
            }

            return response;
        } catch (Exception e) {
            log.error("CONVERSATION_DETAILS_AGGREGATOR_ОШИБКА_ПОСТРОЕНИЯ_ОТВЕТА: " +
                    "для беседы ID: {}, ошибка: {}", conversation.getId(), e.getMessage(), e);
            return null;
        }
    }
}