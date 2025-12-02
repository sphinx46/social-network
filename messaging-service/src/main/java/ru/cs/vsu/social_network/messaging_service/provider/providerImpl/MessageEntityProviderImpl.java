package ru.cs.vsu.social_network.messaging_service.provider.providerImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.messaging_service.entity.Message;
import ru.cs.vsu.social_network.messaging_service.entity.enums.MessageStatus;
import ru.cs.vsu.social_network.messaging_service.exception.MessageNotFoundException;
import ru.cs.vsu.social_network.messaging_service.provider.AbstractEntityProvider;
import ru.cs.vsu.social_network.messaging_service.provider.MessageEntityProvider;
import ru.cs.vsu.social_network.messaging_service.repository.MessageRepository;
import ru.cs.vsu.social_network.messaging_service.utils.MessageConstants;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Реализация провайдера для получения сущности Message.
 * Обеспечивает доступ к данным сообщений с обработкой исключительных ситуаций.
 * Оптимизирован для работы с большими объемами данных в мессенджере.
 */
@Slf4j
@Component
public final class MessageEntityProviderImpl extends AbstractEntityProvider<Message>
        implements MessageEntityProvider {
    private static final String ENTITY_NAME = "СООБЩЕНИЕ";
    private final MessageRepository messageRepository;

    public MessageEntityProviderImpl(MessageRepository messageRepository) {
        super(messageRepository, ENTITY_NAME, () ->
                new MessageNotFoundException(MessageConstants.MESSAGE_NOT_FOUND_FAILURE));
        this.messageRepository = messageRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getUnreadMessagesCountByUser(UUID receiverId, MessageStatus status) {
        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_НЕПРОЧИТАННЫХ_НАЧАЛО: " +
                        "для пользователя с ID: {}, статус: {}",
                ENTITY_NAME, receiverId, status);

        final long count = messageRepository.countByReceiverIdAndStatus(receiverId, status);

        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_НЕПРОЧИТАННЫХ_УСПЕХ: " +
                        "для пользователя с ID: {} найдено {} сообщений со статусом {}",
                ENTITY_NAME, receiverId, count, status);

        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<UUID, Long> getMessagesCountsForConversations(List<UUID> conversationIds) {
        log.info("{}_ПРОВАЙДЕР_ПАКЕТНОЕ_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_НАЧАЛО: " +
                "для {} бесед", conversationIds.size());

        if (conversationIds.isEmpty()) {
            log.info("{}_ПРОВАЙДЕР_ПАКЕТНОЕ_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_ПУСТОЙ_СПИСОК", ENTITY_NAME);
            return Collections.emptyMap();
        }

        final List<Object[]> counts =
                messageRepository.findMessagesCountByConversationIds(conversationIds);

        final Map<UUID, Long> result = counts.stream()
                .collect(Collectors.toMap(
                        tuple -> (UUID) tuple[0],
                        tuple -> (Long) tuple[1]
                ));

        conversationIds.forEach(conversationId -> result.putIfAbsent(conversationId, 0L));

        log.info("{}_ПРОВАЙДЕР_ПАКЕТНОЕ_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_УСПЕХ: " +
                "получено количество сообщений для {} бесед", ENTITY_NAME, result.size());

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Message> getRecentMessagesForConversation(UUID conversationId, int limit) {
        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_ПОСЛЕДНИХ_СООБЩЕНИЙ_НАЧАЛО: " +
                "для беседы {} с лимитом {}", conversationId, limit);

        final int effectiveLimit = Math.max(1, Math.min(limit, 100));
        final PageRequest pageRequest = PageRequest.of(0, effectiveLimit);

        final List<Message> messages = messageRepository
                .findByConversationIdOrderByCreatedAtDesc(conversationId, pageRequest)
                .getContent();

        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_ПОСЛЕДНИХ_СООБЩЕНИЙ_УСПЕХ: " +
                "для беседы {} найдено {} сообщений", conversationId, messages.size());

        return messages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Message> getMessagesWithConversations(List<UUID> messageIds, int limit) {
        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_С_БЕСЕДАМИ_НАЧАЛО: " +
                "для {} сообщений с лимитом {}", messageIds.size(), limit);

        if (messageIds.isEmpty()) {
            log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_С_БЕСЕДАМИ_ПУСТОЙ_СПИСОК", ENTITY_NAME);
            return Collections.emptyList();
        }

        final int effectiveLimit = Math.max(1, Math.min(limit, 50));
        final PageRequest pageRequest = PageRequest.of(0, effectiveLimit);

        final List<Message> messages = messageRepository
                .findMessagesWithConversations(messageIds, pageRequest);

        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_С_БЕСЕДАМИ_УСПЕХ: " +
                "получено {} сообщений с беседами", messages.size());

        return messages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Message> getRecentMessagesForConversations(List<UUID> conversationIds, int limit) {
        log.info("{}_ПРОВАЙДЕР_ПАКЕТНОЕ_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_НАЧАЛО: " +
                "для {} бесед с лимитом {}", conversationIds.size(), limit);

        if (conversationIds.isEmpty()) {
            log.info("{}_ПРОВАЙДЕР_ПАКЕТНОЕ_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_ПУСТОЙ_СПИСОК", ENTITY_NAME);
            return Collections.emptyList();
        }

        final int effectiveLimit = Math.max(1, Math.min(limit, 50));
        final List<Message> messages = messageRepository
                .findRecentMessagesForConversations(conversationIds, effectiveLimit);

        log.info("{}_ПРОВАЙДЕР_ПАКЕТНОЕ_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_УСПЕХ: " +
                "получено {} сообщений", messages.size());

        return messages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Message> getMessagesBetweenUsers(UUID senderId, UUID receiverId, int page, int size) {
        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_МЕЖДУ_ПОЛЬЗОВАТЕЛЯМИ_НАЧАЛО: " +
                "от {} к {}, страница: {}, размер: {}", senderId, receiverId, page, size);

        final int effectivePage = Math.max(0, page);
        final int effectiveSize = Math.max(1, Math.min(size, 100));
        final PageRequest pageRequest = PageRequest.of(effectivePage, effectiveSize);

        final List<Message> messages = messageRepository
                .findMessagesBetweenUsers(senderId, receiverId, pageRequest)
                .getContent();

        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_МЕЖДУ_ПОЛЬЗОВАТЕЛЯМИ_УСПЕХ: " +
                        "найдено {} сообщений между пользователями {} и {}",
                messages.size(), senderId, receiverId);

        return messages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Message> getMessagesBySender(UUID userId, int page, int size) {
        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_ОТПРАВИТЕЛЯ_НАЧАЛО: " +
                "для пользователя {}, страница: {}, размер: {}", userId, page, size);

        final int effectivePage = Math.max(0, page);
        final int effectiveSize = Math.max(1, Math.min(size, 100));
        final PageRequest pageRequest = PageRequest.of(effectivePage, effectiveSize);

        final List<Message> messages = messageRepository
                .findBySenderIdOrderByCreatedAtDesc(userId, pageRequest)
                .getContent();

        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_ОТПРАВИТЕЛЯ_УСПЕХ: " +
                "для пользователя {} найдено {} сообщений", userId, messages.size());

        return messages;
    }
}