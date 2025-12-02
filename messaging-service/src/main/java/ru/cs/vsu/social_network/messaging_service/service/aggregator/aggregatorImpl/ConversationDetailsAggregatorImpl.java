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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public ConversationDetailsResponse aggregateConversationDetails(final Conversation conversation,
                                                                    final boolean includeMessages,
                                                                    final int messagesLimit) {
        log.debug("CONVERSATION_DETAILS_AGGREGATOR_АГРЕГАЦИЯ_БЕСЕДЫ_НАЧАЛО: " +
                "для беседы ID: {}", conversation.getId());

        final ConversationDetailsResponse conversationDetailsResponse =
                mapper.map(conversation, ConversationDetailsResponse.class);

        final UUID conversationId = conversation.getId();

        if (includeMessages && messagesLimit > 0) {
            final List<MessageResponse> messages =
                    batchMessageService.getMessagesByConversation(conversationId, 0,
                            messagesLimit);
            conversationDetailsResponse.setMessages(messages);
            conversationDetailsResponse.setMessagesCount((long) messages.size());

            if (!messages.isEmpty()) {
                conversationDetailsResponse.setLastMessageId(messages.get(0).getMessageId());
            }
        } else {
            conversationDetailsResponse.setMessages(Collections.emptyList());
            conversationDetailsResponse.setMessagesCount(0L);
        }

        log.debug("CONVERSATION_DETAILS_AGGREGATOR_АГРЕГАЦИЯ_БЕСЕДЫ_УСПЕХ: " +
                "для беседы ID: {}", conversation.getId());
        return conversationDetailsResponse;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<ConversationDetailsResponse> aggregateConversationsPage(final Page<Conversation> conversationsPage,
                                                                        final boolean includeMessages,
                                                                        final int messagesLimit) {
        log.debug("CONVERSATION_DETAILS_AGGREGATOR_АГРЕГАЦИЯ_СТРАНИЦЫ_БЕСЕД_НАЧАЛО:" +
                " для {} бесед", conversationsPage.getNumberOfElements());

        final List<Conversation> conversations = conversationsPage.getContent();
        if (conversations.isEmpty()) {
            log.debug("CONVERSATION_DETAILS_AGGREGATOR_АГРЕГАЦИЯ_СТРАНИЦЫ_БЕСЕД_ПУСТАЯ_СТРАНИЦА");
            return new PageImpl<>(Collections.emptyList(),
                    conversationsPage.getPageable(), conversationsPage.getTotalElements());
        }

        final List<UUID> conversationIds = conversations.stream()
                .map(Conversation::getId)
                .collect(Collectors.toList());

        final Map<UUID, List<MessageResponse>> messagesMap = includeMessages && messagesLimit > 0 ?
                batchMessageService.getRecentMessagesForConversations(conversationIds, messagesLimit) :
                Collections.emptyMap();

        final List<ConversationDetailsResponse> detailedConversations = conversations.stream()
                .map(conversation -> buildConversationDetailsResponse(conversation,
                        messagesMap, includeMessages))
                .collect(Collectors.toList());

        log.debug("CONVERSATION_DETAILS_AGGREGATOR_АГРЕГАЦИЯ_СТРАНИЦЫ_БЕСЕД_УСПЕХ: " +
                "агрегировано {} бесед", detailedConversations.size());
        return new PageImpl<>(detailedConversations, conversationsPage.getPageable(),
                conversationsPage.getTotalElements());
    }

    /**
     * Строит детальный ответ для беседы.
     * Включает сообщения, количество сообщений и определяет последнее сообщение.
     *
     * @param conversation сущность беседы
     * @param messagesMap маппинг сообщений по ID беседы
     * @param includeMessages флаг включения сообщений
     * @return детальный ответ беседы
     */
    private ConversationDetailsResponse buildConversationDetailsResponse(final Conversation conversation,
                                                                         final Map<UUID, List<MessageResponse>> messagesMap,
                                                                         final boolean includeMessages) {
        final ConversationDetailsResponse response =
                mapper.map(conversation, ConversationDetailsResponse.class);
        final UUID conversationId = conversation.getId();

        if (includeMessages) {
            final List<MessageResponse> messages =
                    messagesMap.getOrDefault(conversationId, Collections.emptyList());
            response.setMessages(messages);
            response.setMessagesCount((long) messages.size());

            if (!messages.isEmpty()) {
                response.setLastMessageId(messages.get(0).getMessageId());
            }
        } else {
            response.setMessages(Collections.emptyList());
            response.setMessagesCount(0L);
        }

        return response;
    }
}