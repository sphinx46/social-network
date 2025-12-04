package ru.cs.vsu.social_network.messaging_service.event.handler.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.messaging_service.event.GenericMessagingCacheEvent;
import ru.cs.vsu.social_network.messaging_service.service.cache.MessagingCacheService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Обработчик событий инвалидации кеша мессенджера.
 * Обеспечивает асинхронную обработку событий и инвалидацию кеша сообщений и бесед
 * в соответствии с бизнес-логикой приложения.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessagingCacheEventHandlerImpl implements MessagingCacheEventHandler {

    private final MessagingCacheService messagingCacheService;

    private final Map<UUID, AtomicLong> messageOperationCounters = new ConcurrentHashMap<>();
    private final Map<UUID, AtomicLong> conversationOperationCounters = new ConcurrentHashMap<>();

    private static final int BATCH_MESSAGE_INVALIDATION_THRESHOLD = 10;
    private static final int BATCH_CONVERSATION_INVALIDATION_THRESHOLD = 5;

    /**
     * {@inheritDoc}
     *
     * @param event событие инвалидации кеша мессенджера
     * @throws Exception если произошла ошибка при обработке события
     */
    @Async("cacheTaskExecutor")
    @EventListener
    @Override
    public void handleMessagingCacheEvent(final GenericMessagingCacheEvent event) {
        final String logPrefix = "ОБРАБОТЧИК_КЕША_МЕССЕНДЖЕР";

        try {
            switch (event.getCacheEventType()) {
                case MESSAGE_CREATED:
                    handleMessageCreated(event);
                    break;
                case MESSAGE_UPDATED:
                    handleMessageUpdated(event);
                    break;
                case MESSAGE_DELETED:
                    handleMessageDeleted(event);
                    break;
                case CONVERSATION_CREATED:
                    handleConversationCreated(event);
                    break;
                case MESSAGES_READ:
                    handleMessagesRead(event);
                    break;
                case MESSAGE_IMAGE_UPLOADED:
                    handleMessageImageUploaded(event);
                    break;
                default:
                    log.warn("{}: неизвестный тип события {}", logPrefix, event.getCacheEventType());
            }

        } catch (Exception e) {
            log.error("{}: ошибка при обработке события типа {}",
                    logPrefix, event.getCacheEventType(), e);
        }
    }

    /**
     * Обрабатывает событие создания сообщения.
     * Инвалидирует кеши сообщения и беседы, запускает batch инвалидацию.
     *
     * @param event событие создания сообщения
     */
    private void handleMessageCreated(final GenericMessagingCacheEvent event) {
        final String logPrefix = "ОБРАБОТЧИК_КЕША_СООБЩЕНИЕ_СОЗДАНИЕ";
        handleMessageChange(event, logPrefix);
        handleBatchMessageInvalidation(event.getData("conversationId", UUID.class));
        handleBatchConversationInvalidation(event.getData("conversationId", UUID.class));
    }

    /**
     * Обрабатывает событие обновления сообщения.
     * Инвалидирует кеши сообщения и связанных сущностей.
     *
     * @param event событие обновления сообщения
     */
    private void handleMessageUpdated(final GenericMessagingCacheEvent event) {
        final String logPrefix = "ОБРАБОТЧИК_КЕША_СООБЩЕНИЕ_ОБНОВЛЕНИЕ";
        handleMessageChange(event, logPrefix);
    }

    /**
     * Обрабатывает событие удаления сообщения.
     * Инвалидирует кеши сообщения и связанных сущностей.
     *
     * @param event событие удаления сообщения
     */
    private void handleMessageDeleted(final GenericMessagingCacheEvent event) {
        final String logPrefix = "ОБРАБОТЧИК_КЕША_СООБЩЕНИЕ_УДАЛЕНИЕ";
        handleMessageChange(event, logPrefix);
    }

    /**
     * Общий метод для обработки изменений сообщений.
     * Извлекает данные из события и инвалидирует соответствующие кеши.
     *
     * @param event событие изменения сообщения
     * @param logPrefix префикс для логирования
     */
    private void handleMessageChange(final GenericMessagingCacheEvent event, final String logPrefix) {
        final UUID conversationId = event.getData("conversationId", UUID.class);
        final UUID messageId = event.getData("messageId", UUID.class);
        final UUID senderId = event.getData("senderId", UUID.class);
        final UUID receiverId = event.getData("receiverId", UUID.class);

        if (conversationId == null || messageId == null) {
            log.warn("{}: отсутствует conversationId или messageId в событии", logPrefix);
            return;
        }

        messagingCacheService.evictMessage(messageId);
        evictAllConversationCaches(conversationId, senderId, receiverId, logPrefix);
    }

    /**
     * Обрабатывает событие создания беседы.
     * Инвалидирует все кеши связанные с новой беседой.
     *
     * @param event событие создания беседы
     */
    private void handleConversationCreated(final GenericMessagingCacheEvent event) {
        final String logPrefix = "ОБРАБОТЧИК_КЕША_БЕСЕДА_СОЗДАНИЕ";

        final UUID conversationId = event.getData("conversationId", UUID.class);
        final UUID user1Id = event.getData("user1Id", UUID.class);
        final UUID user2Id = event.getData("user2Id", UUID.class);

        if (conversationId == null || user1Id == null || user2Id == null) {
            log.warn("{}: отсутствуют обязательные данные в событии", logPrefix);
            return;
        }

        evictAllConversationCaches(conversationId, user1Id, user2Id, logPrefix);
    }

    /**
     * Обрабатывает событие прочтения сообщений.
     * Инвалидирует кеши сообщений беседы и бесед пользователя.
     *
     * @param event событие прочтения сообщений
     */
    private void handleMessagesRead(final GenericMessagingCacheEvent event) {
        final String logPrefix = "ОБРАБОТЧИК_КЕША_СООБЩЕНИЯ_ПРОЧИТАНЫ";

        final UUID conversationId = event.getData("conversationId", UUID.class);
        final UUID userId = event.getData("userId", UUID.class);

        if (conversationId == null || userId == null) {
            log.warn("{}: отсутствует conversationId или userId в событии", logPrefix);
            return;
        }

        messagingCacheService.evictConversationMessages(conversationId);
        messagingCacheService.evictConversationDetails(conversationId);
        messagingCacheService.evictUserConversations(userId);
    }

    /**
     * Обрабатывает событие загрузки изображения в сообщение.
     * Инвалидирует кеши сообщения, сообщений беседы, деталей беседы и бесед пользователей.
     *
     * @param event событие загрузки изображения
     */
    private void handleMessageImageUploaded(final GenericMessagingCacheEvent event) {
        final String logPrefix = "ОБРАБОТЧИК_КЕША_ИЗОБРАЖЕНИЕ_ЗАГРУЖЕНО";

        final UUID conversationId = event.getData("conversationId", UUID.class);
        final UUID messageId = event.getData("messageId", UUID.class);
        final UUID senderId = event.getData("senderId", UUID.class);
        final UUID receiverId = event.getData("receiverId", UUID.class);

        if (conversationId == null || messageId == null) {
            log.warn("{}: отсутствует conversationId или messageId в событии", logPrefix);
            return;
        }

        log.debug("{}: обработка загрузки изображения в сообщение {} беседы {}, отправитель: {}, получатель: {}",
                logPrefix, messageId, conversationId, senderId, receiverId);

        messagingCacheService.evictMessage(messageId);

        messagingCacheService.evictConversationMessages(conversationId);

        messagingCacheService.evictConversationDetails(conversationId);

        if (senderId != null) {
            messagingCacheService.evictUserConversations(senderId);
        }

        if (receiverId != null) {
            messagingCacheService.evictUserConversations(receiverId);
        }

        if (senderId != null && receiverId != null) {
            messagingCacheService.evictConversationBetweenUsers(senderId, receiverId);
        }

        messagingCacheService.evictFirstPages();

        log.debug("{}: полная инвалидация завершена для сообщения {} беседы {}",
                logPrefix, messageId, conversationId);
    }

    /**
     * Инвалидирует ВСЕ типы кешей для беседы.
     * Включает кеши сообщений, деталей беседы, пользователей и первых страниц.
     *
     * @param conversationId идентификатор беседы
     * @param user1Id идентификатор первого пользователя
     * @param user2Id идентификатор второго пользователя
     * @param logPrefix префикс для логирования
     */
    private void evictAllConversationCaches(final UUID conversationId,
                                            final UUID user1Id,
                                            final UUID user2Id,
                                            final String logPrefix) {

        messagingCacheService.evictConversationMessages(conversationId);
        messagingCacheService.evictConversationDetails(conversationId);

        if (user1Id != null) {
            messagingCacheService.evictUserConversations(user1Id);
        }
        if (user2Id != null) {
            messagingCacheService.evictUserConversations(user2Id);
        }

        if (user1Id != null && user2Id != null) {
            messagingCacheService.evictConversationBetweenUsers(user1Id, user2Id);
        }

        messagingCacheService.evictFirstPages();
    }

    /**
     * Обрабатывает batch инвалидацию для частых операций с сообщениями.
     * Счетчик операций увеличивается при каждом изменении сообщения в беседе.
     * При достижении порога инвалидируются детали беседы.
     *
     * @param conversationId идентификатор беседы для batch инвалидации
     */
    private void handleBatchMessageInvalidation(final UUID conversationId) {
        if (conversationId == null) return;

        final long operationCount = messageOperationCounters
                .computeIfAbsent(conversationId, k -> new AtomicLong(0))
                .incrementAndGet();

        if (operationCount >= BATCH_MESSAGE_INVALIDATION_THRESHOLD) {
            messagingCacheService.evictConversationDetails(conversationId);
            messageOperationCounters.get(conversationId).set(0);
        }
    }

    /**
     * Обрабатывает batch инвалидацию для частых операций с беседой.
     * Счетчик операций увеличивается при каждом изменении в беседе.
     * При достижении порога инвалидируются первые страницы бесед.
     *
     * @param conversationId идентификатор беседы для batch инвалидации
     */
    private void handleBatchConversationInvalidation(final UUID conversationId) {
        if (conversationId == null) return;

        final long operationCount = conversationOperationCounters
                .computeIfAbsent(conversationId, k -> new AtomicLong(0))
                .incrementAndGet();

        if (operationCount >= BATCH_CONVERSATION_INVALIDATION_THRESHOLD) {
            messagingCacheService.evictFirstPages();
            conversationOperationCounters.get(conversationId).set(0);
        }
    }
}