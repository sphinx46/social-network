package ru.cs.vsu.social_network.messaging_service.service.serviceImpl.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.cs.vsu.social_network.messaging_service.event.CacheEventType;
import ru.cs.vsu.social_network.messaging_service.service.cache.CacheEventFallbackService;
import ru.cs.vsu.social_network.messaging_service.service.cache.MessagingCacheService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Реализация сервиса для обработки отложенной инвалидации кэша мессенджера при ошибках.
 * Использует периодическую задачу для обработки накопленных инвалидаций.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheEventFallbackServiceImpl implements CacheEventFallbackService {

    private final MessagingCacheService messagingCacheService;

    private final Map<UUID, CacheEventType> pendingConversationInvalidations = new ConcurrentHashMap<>();
    private final Map<UUID, CacheEventType> pendingMessageInvalidations = new ConcurrentHashMap<>();
    private final Map<UUID, CacheEventType> pendingUserInvalidations = new ConcurrentHashMap<>();

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY_MS = 100;

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerPendingInvalidation(UUID conversationId, CacheEventType eventType) {
        if (conversationId == null) {
            log.warn("FALLBACK_МЕССЕНДЖЕР_РЕГИСТРАЦИЯ_ОШИБКА: " +
                    "conversationId не может быть null");
            return;
        }

        pendingConversationInvalidations.put(conversationId, eventType);
        log.warn("FALLBACK_МЕССЕНДЖЕР_РЕГИСТРАЦИЯ_БЕСЕДА: " +
                        "отложенная инвалидация зарегистрирована для беседы {}, тип события {}",
                conversationId, eventType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerPendingInvalidationForMessage(UUID messageId, CacheEventType eventType) {
        if (messageId == null) {
            log.warn("FALLBACK_МЕССЕНДЖЕР_РЕГИСТРАЦИЯ_ОШИБКА: " +
                    "messageId не может быть null");
            return;
        }

        pendingMessageInvalidations.put(messageId, eventType);
        log.warn("FALLBACK_МЕССЕНДЖЕР_РЕГИСТРАЦИЯ_СООБЩЕНИЕ: " +
                        "отложенная инвалидация зарегистрирована для сообщения {}, тип события {}",
                messageId, eventType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerPendingInvalidationForUser(UUID userId, CacheEventType eventType) {
        if (userId == null) {
            log.warn("FALLBACK_МЕССЕНДЖЕР_РЕГИСТРАЦИЯ_ОШИБКА: " +
                    "userId не может быть null");
            return;
        }

        pendingUserInvalidations.put(userId, eventType);
        log.warn("FALLBACK_МЕССЕНДЖЕР_РЕГИСТРАЦИЯ_ПОЛЬЗОВАТЕЛЬ: " +
                        "отложенная инвалидация зарегистрирована для пользователя {}, тип события {}",
                userId, eventType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeImmediateInvalidation(UUID conversationId) {
        if (conversationId == null) {
            log.warn("FALLBACK_МЕССЕНДЖЕР_НЕМЕДЛЕННАЯ_ОШИБКА: " +
                    "conversationId не может быть null");
            return;
        }

        log.info("FALLBACK_МЕССЕНДЖЕР_НЕМЕДЛЕННАЯ_ИНВАЛИДАЦИЯ: " +
                "выполнение немедленной инвалидации для беседы {}", conversationId);
        executeConversationInvalidationWithRetry(conversationId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPendingInvalidationsCount() {
        return pendingConversationInvalidations.size()
                + pendingMessageInvalidations.size()
                + pendingUserInvalidations.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearAllPendingInvalidations() {
        int totalCount = getPendingInvalidationsCount();

        pendingConversationInvalidations.clear();
        pendingMessageInvalidations.clear();
        pendingUserInvalidations.clear();

        log.info("FALLBACK_МЕССЕНДЖЕР_ОЧИСТКА: " +
                "удалено {} ожидающих инвалидаций", totalCount);
    }

    /**
     * Периодически обрабатывает накопленные инвалидации для бесед.
     * Выполняется каждые 30 секунд для очистки очереди отложенных инвалидаций.
     */
    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    public void processPendingConversationInvalidations() {
        if (pendingConversationInvalidations.isEmpty()) {
            return;
        }

        log.info("FALLBACK_МЕССЕНДЖЕР_ОБРАБОТКА_БЕСЕД: " +
                "обработка {} отложенных инвалидаций бесед", pendingConversationInvalidations.size());

        pendingConversationInvalidations.keySet().forEach(conversationId -> {
            try {
                executeConversationInvalidationWithRetry(conversationId);
                pendingConversationInvalidations.remove(conversationId);
                log.info("FALLBACK_МЕССЕНДЖЕР_ОБРАБОТКА_УСПЕХ: " +
                        "кэш для беседы {} успешно инвалидирован", conversationId);
            } catch (Exception e) {
                log.error("FALLBACK_МЕССЕНДЖЕР_ОБРАБОТКА_ОШИБКА: " +
                        "ошибка при инвалидации кэша для беседы {}", conversationId, e);
            }
        });
    }

    /**
     * Периодически обрабатывает накопленные инвалидации для сообщений.
     * Выполняется каждые 30 секунд для очистки очереди отложенных инвалидаций.
     */
    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    public void processPendingMessageInvalidations() {
        if (pendingMessageInvalidations.isEmpty()) {
            return;
        }

        log.info("FALLBACK_МЕССЕНДЖЕР_ОБРАБОТКА_СООБЩЕНИЙ: " +
                "обработка {} отложенных инвалидаций сообщений", pendingMessageInvalidations.size());

        pendingMessageInvalidations.keySet().forEach(messageId -> {
            try {
                executeMessageInvalidationWithRetry(messageId);
                pendingMessageInvalidations.remove(messageId);
                log.info("FALLBACK_МЕССЕНДЖЕР_ОБРАБОТКА_УСПЕХ: " +
                        "кэш для сообщения {} успешно инвалидирован", messageId);
            } catch (Exception e) {
                log.error("FALLBACK_МЕССЕНДЖЕР_ОБРАБОТКА_ОШИБКА: " +
                        "ошибка при инвалидации кэша для сообщения {}", messageId, e);
            }
        });
    }

    /**
     * Периодически обрабатывает накопленные инвалидации для пользователей.
     * Выполняется каждые 30 секунд для очистки очереди отложенных инвалидаций.
     */
    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    public void processPendingUserInvalidations() {
        if (pendingUserInvalidations.isEmpty()) {
            return;
        }

        log.info("FALLBACK_МЕССЕНДЖЕР_ОБРАБОТКА_ПОЛЬЗОВАТЕЛЕЙ: " +
                "обработка {} отложенных инвалидаций пользователей", pendingUserInvalidations.size());

        pendingUserInvalidations.keySet().forEach(userId -> {
            try {
                executeUserInvalidationWithRetry(userId);
                pendingUserInvalidations.remove(userId);
                log.info("FALLBACK_МЕССЕНДЖЕР_ОБРАБОТКА_УСПЕХ: " +
                        "кэш для пользователя {} успешно инвалидирован", userId);
            } catch (Exception e) {
                log.error("FALLBACK_МЕССЕНДЖЕР_ОБРАБОТКА_ОШИБКА: " +
                        "ошибка при инвалидации кэша для пользователя {}", userId, e);
            }
        });
    }

    /**
     * Выполняет инвалидацию кэша для беседы с повторными попытками.
     *
     * @param conversationId ID беседы для инвалидации
     */
    private void executeConversationInvalidationWithRetry(UUID conversationId) {
        int attempt = 0;
        boolean success = false;

        while (attempt < MAX_RETRY_ATTEMPTS && !success) {
            attempt++;
            try {
                log.debug("FALLBACK_МЕССЕНДЖЕР_ПОПЫТКА_{}_БЕСЕДА: " +
                        "инвалидация кэша для беседы {}", attempt, conversationId);

                messagingCacheService.evictConversationDetails(conversationId);
                messagingCacheService.evictConversationMessages(conversationId);

                messagingCacheService.evictFirstPages();

                success = true;
                log.debug("FALLBACK_МЕССЕНДЖЕР_ПОПЫТКА_УСПЕХ_БЕСЕДА: " +
                        "кэш для беседы {} успешно инвалидирован с попытки {}", conversationId, attempt);

            } catch (Exception e) {
                log.warn("FALLBACK_МЕССЕНДЖЕР_ПОПЫТКА_ОШИБКА_{}_БЕСЕДА: " +
                                "ошибка при инвалидации кэша для беседы {}: {}",
                        attempt, conversationId, e.getMessage());

                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        if (!success) {
            log.error("FALLBACK_МЕССЕНДЖЕР_ПОПЫТКИ_ИСЧЕРПАНЫ_БЕСЕДА: " +
                            "не удалось инвалидировать кэш для беседы {} после {} попыток",
                    conversationId, MAX_RETRY_ATTEMPTS);
        }
    }

    /**
     * Выполняет инвалидацию кэша для сообщения с повторными попытками.
     *
     * @param messageId ID сообщения для инвалидации
     */
    private void executeMessageInvalidationWithRetry(UUID messageId) {
        int attempt = 0;
        boolean success = false;

        while (attempt < MAX_RETRY_ATTEMPTS && !success) {
            attempt++;
            try {
                log.debug("FALLBACK_МЕССЕНДЖЕР_ПОПЫТКА_{}_СООБЩЕНИЕ: " +
                        "инвалидация кэша для сообщения {}", attempt, messageId);

                messagingCacheService.evictMessage(messageId);

                success = true;
                log.debug("FALLBACK_МЕССЕНДЖЕР_ПОПЫТКА_УСПЕХ_СООБЩЕНИЕ: " +
                        "кэш для сообщения {} успешно инвалидирован с попытки {}", messageId, attempt);

            } catch (Exception e) {
                log.warn("FALLBACK_МЕССЕНДЖЕР_ПОПЫТКА_ОШИБКА_{}_СООБЩЕНИЕ: " +
                                "ошибка при инвалидации кэша для сообщения {}: {}",
                        attempt, messageId, e.getMessage());

                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        if (!success) {
            log.error("FALLBACK_МЕССЕНДЖЕР_ПОПЫТКИ_ИСЧЕРПАНЫ_СООБЩЕНИЕ: " +
                            "не удалось инвалидировать кэш для сообщения {} после {} попыток",
                    messageId, MAX_RETRY_ATTEMPTS);
        }
    }

    /**
     * Выполняет инвалидацию кэша для пользователя с повторными попытками.
     *
     * @param userId ID пользователя для инвалидации
     */
    private void executeUserInvalidationWithRetry(UUID userId) {
        int attempt = 0;
        boolean success = false;

        while (attempt < MAX_RETRY_ATTEMPTS && !success) {
            attempt++;
            try {
                log.debug("FALLBACK_МЕССЕНДЖЕР_ПОПЫТКА_{}_ПОЛЬЗОВАТЕЛЬ: " +
                        "инвалидация кэша для пользователя {}", attempt, userId);

                messagingCacheService.evictUserConversations(userId);
                messagingCacheService.evictFirstPages();

                success = true;
                log.debug("FALLBACK_МЕССЕНДЖЕР_ПОПЫТКА_УСПЕХ_ПОЛЬЗОВАТЕЛЬ: " +
                        "кэш для пользователя {} успешно инвалидирован с попытки {}", userId, attempt);

            } catch (Exception e) {
                log.warn("FALLBACK_МЕССЕНДЖЕР_ПОПЫТКА_ОШИБКА_{}_ПОЛЬЗОВАТЕЛЬ: " +
                                "ошибка при инвалидации кэша для пользователя {}: {}",
                        attempt, userId, e.getMessage());

                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        if (!success) {
            log.error("FALLBACK_МЕССЕНДЖЕР_ПОПЫТКИ_ИСЧЕРПАНЫ_ПОЛЬЗОВАТЕЛЬ: " +
                            "не удалось инвалидировать кэш для пользователя {} после {} попыток",
                    userId, MAX_RETRY_ATTEMPTS);
        }
    }
}