package ru.cs.vsu.social_network.messaging_service.service.serviceImpl.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.MessageResponse;
import ru.cs.vsu.social_network.messaging_service.entity.Message;
import ru.cs.vsu.social_network.messaging_service.event.CacheEventType;
import ru.cs.vsu.social_network.messaging_service.event.GenericMessagingCacheEvent;
import ru.cs.vsu.social_network.messaging_service.service.cache.MessagingCacheEventPublisherService;
import ru.cs.vsu.social_network.messaging_service.utils.factory.cache.MessagingCacheEventFactory;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Реализация сервиса публикации событий кеша мессенджера.
 * Обеспечивает асинхронную публикацию событий для инвалидации кэша переписок
 * с гарантией выполнения после коммита транзакции и интеграцией с FallbackService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessagingCacheEventPublisherServiceImpl implements MessagingCacheEventPublisherService {

    private final ApplicationEventPublisher eventPublisher;
    private final MessagingCacheEventFactory cacheEventFactory;
    private final CacheEventFallbackServiceImpl cacheEventFallbackService;

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishMessageCreated(Object source,
                                      Object target,
                                      UUID conversationId,
                                      UUID messageId,
                                      UUID senderId,
                                      UUID receiverId) {
        log.info("СОБЫТИЕ_МЕССЕНДЖЕР_СООБЩЕНИЕ_СОЗДАНИЕ_ПУБЛИКАЦИЯ: " +
                "публикация для сообщения {}", messageId);

        publishWithTransactionSync(() ->
                publishEventSafely(cacheEventFactory.createMessageEvent(
                                CacheEventType.MESSAGE_CREATED,
                                source, target, conversationId, messageId, senderId, receiverId),
                        conversationId));

        log.debug("СОБЫТИЕ_МЕССЕНДЖЕР_СООБЩЕНИЕ_СОЗДАНИЕ_ИНИЦИИРОВАНО: " +
                "событие создания сообщения {} инициировано", messageId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishMessageUpdated(Object source,
                                      Object target,
                                      UUID conversationId,
                                      UUID messageId) {
        log.info("СОБЫТИЕ_МЕССЕНДЖЕР_СООБЩЕНИЕ_ОБНОВЛЕНИЕ_ПУБЛИКАЦИЯ: " +
                "публикация для сообщения {}", messageId);

        UUID senderId = extractSenderId(target);
        UUID receiverId = extractReceiverId(target);

        log.debug("СОБЫТИЕ_МЕССЕНДЖЕР_СООБЩЕНИЕ_ОБНОВЛЕНИЕ_ДАННЫЕ: " +
                "получены senderId: {}, receiverId: {} из target", senderId, receiverId);

        publishWithTransactionSync(() ->
                publishEventSafely(cacheEventFactory.createMessageEvent(
                                CacheEventType.MESSAGE_UPDATED,
                                source, target, conversationId, messageId, senderId, receiverId),
                        conversationId));

        log.debug("СОБЫТИЕ_МЕССЕНДЖЕР_СООБЩЕНИЕ_ОБНОВЛЕНИЕ_ИНИЦИИРОВАНО: " +
                "событие обновления сообщения {} инициировано", messageId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishMessageDeleted(Object source,
                                      Object target,
                                      UUID conversationId,
                                      UUID messageId) {
        log.info("СОБЫТИЕ_МЕССЕНДЖЕР_СООБЩЕНИЕ_УДАЛЕНИЕ_ПУБЛИКАЦИЯ: " +
                "публикация для сообщения {}", messageId);

        UUID senderId = extractSenderId(target);
        UUID receiverId = extractReceiverId(target);

        log.debug("СОБЫТИЕ_МЕССЕНДЖЕР_СООБЩЕНИЕ_УДАЛЕНИЕ_ДАННЫЕ: " +
                "получены senderId: {}, receiverId: {} из target", senderId, receiverId);

        publishWithTransactionSync(() ->
                publishEventSafely(cacheEventFactory.createMessageEvent(
                                CacheEventType.MESSAGE_DELETED,
                                source, target, conversationId, messageId, senderId, receiverId),
                        conversationId));

        log.debug("СОБЫТИЕ_МЕССЕНДЖЕР_СООБЩЕНИЕ_УДАЛЕНИЕ_ИНИЦИИРОВАНО: " +
                "событие удаления сообщения {} инициировано", messageId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishConversationCreated(Object source,
                                           Object target,
                                           UUID conversationId,
                                           UUID user1Id,
                                           UUID user2Id) {
        log.info("СОБЫТИЕ_МЕССЕНДЖЕР_БЕСЕДА_СОЗДАНИЕ_ПУБЛИКАЦИЯ: " +
                "публикация для беседы {}", conversationId);

        publishWithTransactionSync(() ->
                publishEventSafely(cacheEventFactory.createConversationEvent(
                                CacheEventType.CONVERSATION_CREATED,
                                source, target, conversationId, user1Id, user2Id),
                        conversationId));

        log.debug("СОБЫТИЕ_МЕССЕНДЖЕР_БЕСЕДА_СОЗДАНИЕ_ИНИЦИИРОВАНО: " +
                "событие создания беседы {} инициировано", conversationId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishMessagesRead(Object source,
                                    Object target,
                                    UUID conversationId,
                                    UUID userId) {
        log.info("СОБЫТИЕ_МЕССЕНДЖЕР_СООБЩЕНИЯ_ПРОЧИТАНЫ_ПУБЛИКАЦИЯ: " +
                "публикация для беседы {}", conversationId);

        publishWithTransactionSync(() ->
                publishEventSafely(cacheEventFactory.createMessageStatusEvent(
                                CacheEventType.MESSAGES_READ,
                                source, target, conversationId, userId),
                        conversationId));

        log.debug("СОБЫТИЕ_МЕССЕНДЖЕР_СООБЩЕНИЯ_ПРОЧИТАНЫ_ИНИЦИИРОВАНО: " +
                "событие прочтения сообщений инициировано");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishMessageImageUploaded(Object source,
                                            Object target,
                                            UUID conversationId,
                                            UUID messageId) {
        log.info("СОБЫТИЕ_МЕССЕНДЖЕР_ИЗОБРАЖЕНИЕ_ЗАГРУЖЕНО_ПУБЛИКАЦИЯ: " +
                "публикация для сообщения {}", messageId);

        UUID senderId = extractSenderId(target);
        UUID receiverId = extractReceiverId(target);

        log.debug("СОБЫТИЕ_МЕССЕНДЖЕР_ИЗОБРАЖЕНИЕ_ЗАГРУЖЕНО_ДАННЫЕ: " +
                "получены senderId: {}, receiverId: {} из target", senderId, receiverId);

        publishWithTransactionSync(() ->
                publishEventSafely(cacheEventFactory.createMessageEvent(
                                CacheEventType.MESSAGE_IMAGE_UPLOADED,
                                source, target, conversationId, messageId, senderId, receiverId),
                        conversationId));

        log.debug("СОБЫТИЕ_МЕССЕНДЖЕР_ИЗОБРАЖЕНИЕ_ЗАГРУЖЕНО_ИНИЦИИРОВАНО: " +
                "событие загрузки изображения инициировано");
    }

    private UUID extractSenderId(Object target) {
        if (target instanceof Message message) {
            return message.getSenderId();
        } else if (target instanceof MessageResponse messageResponse) {
            return messageResponse.getSenderId();
        }
        return null;
    }

    private UUID extractReceiverId(Object target) {
        if (target instanceof Message message) {
            return message.getReceiverId();
        } else if (target instanceof MessageResponse messageResponse) {
            return messageResponse.getReceiverId();
        }
        return null;
    }

    /**
     * Публикует событие кеша с использованием TransactionSynchronization.
     * Гарантирует выполнение после коммита транзакции.
     *
     * @param action действие для выполнения после коммита
     */
    private void publishWithTransactionSync(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            log.warn("СОБЫТИЕ_МЕССЕНДЖЕР_ПУБЛИКАТОР_ТРАНЗАКЦИЯ_НЕ_АКТИВНА: " +
                    "публикация без транзакции");
            executeActionSafely(action);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    private final AtomicBoolean actionExecuted = new AtomicBoolean(false);

                    @Override
                    public void afterCommit() {
                        if (actionExecuted.compareAndSet(false, true)) {
                            log.debug("СОБЫТИЕ_МЕССЕНДЖЕР_ПУБЛИКАТОР_ПОСЛЕ_КОММИТА: " +
                                    "выполнение после коммита транзакции");
                            executeActionSafely(action);
                        }
                    }

                    @Override
                    public void afterCompletion(int status) {
                        if (status == STATUS_ROLLED_BACK) {
                            log.warn("СОБЫТИЕ_МЕССЕНДЖЕР_ПУБЛИКАТОР_ТРАНЗАКЦИЯ_ОТКАТ: " +
                                    "транзакция откатана, событие не публикуется");
                        } else if (!actionExecuted.get()) {
                            log.error("СОБЫТИЕ_МЕССЕНДЖЕР_ПУБЛИКАТОР_ДЕЙСТВИЕ_НЕ_ВЫПОЛНЕНО: " +
                                    "действие не выполнено после завершения транзакции");
                            executeActionSafely(action);
                        }
                    }
                }
        );
    }

    /**
     * Безопасно выполняет действие с обработкой исключений.
     *
     * @param action действие для выполнения
     */
    private void executeActionSafely(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            log.error("СОБЫТИЕ_МЕССЕНДЖЕР_ПУБЛИКАТОР_ОШИБКА_ВЫПОЛНЕНИЯ: " +
                    "ошибка при выполнении действия", e);
        }
    }

    /**
     * Публикует событие кеша асинхронно с обработкой ошибок и интеграцией с FallbackService.
     *
     * @param event          событие кеша для публикации
     * @param conversationId ID беседы для отложенной инвалидации в случае ошибки
     */
    @Async("cacheTaskExecutor")
    protected void publishEventSafely(GenericMessagingCacheEvent event, UUID conversationId) {
        try {
            log.debug("СОБЫТИЕ_МЕССЕНДЖЕР_ПУБЛИКАЦИЯ_НАЧАЛО: " +
                    "публикация события типа {}", event.getCacheEventType());

            eventPublisher.publishEvent(event);

            log.debug("СОБЫТИЕ_МЕССЕНДЖЕР_ПУБЛИКАЦИЯ_УСПЕХ: " +
                    "событие типа {} успешно опубликовано", event.getCacheEventType());
        } catch (Exception e) {
            log.error("СОБЫТИЕ_МЕССЕНДЖЕР_ПУБЛИКАЦИЯ_ОШИБКА: " +
                            "ошибка при публикации события типа {}",
                    event.getCacheEventType(), e);

            cacheEventFallbackService.registerPendingInvalidation(
                    conversationId, event.getCacheEventType());
        }
    }
}