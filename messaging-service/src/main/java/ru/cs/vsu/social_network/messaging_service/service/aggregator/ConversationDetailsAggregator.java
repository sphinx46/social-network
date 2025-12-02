package ru.cs.vsu.social_network.messaging_service.service.aggregator;

import org.springframework.data.domain.Page;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.ConversationDetailsResponse;
import ru.cs.vsu.social_network.messaging_service.entity.Conversation;

/**
 * Агрегатор данных для бесед (переписок).
 * Обеспечивает сборку расширенных данных бесед с загрузкой связанных сущностей и сообщений.
 */
public interface ConversationDetailsAggregator {

    /**
     * Агрегирует расширенные данные для одной беседы.
     * Включает сообщения, количество сообщений и метаданные беседы.
     *
     * @param conversation сущность беседы
     * @param includeMessages флаг включения сообщений
     * @param messagesLimit ограничение количества сообщений
     * @return расширенный ответ с данными беседы
     */
    ConversationDetailsResponse aggregateConversationDetails(Conversation conversation,
                                                             boolean includeMessages,
                                                             int messagesLimit);

    /**
     * Агрегирует расширенные данные для страницы бесед.
     * Использует батч-запросы для оптимизации производительности при работе с множеством бесед.
     *
     * @param conversationsPage страница с беседами
     * @param includeMessages флаг включения сообщений
     * @param messagesLimit ограничение количества сообщений на беседу
     * @return страница с расширенными данными бесед
     */
    Page<ConversationDetailsResponse> aggregateConversationsPage(Page<Conversation> conversationsPage,
                                                                 boolean includeMessages,
                                                                 int messagesLimit);
}