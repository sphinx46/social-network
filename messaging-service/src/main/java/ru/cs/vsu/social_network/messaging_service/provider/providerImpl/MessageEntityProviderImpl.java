package ru.cs.vsu.social_network.messaging_service.provider.providerImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.messaging_service.entity.Message;
import ru.cs.vsu.social_network.messaging_service.entity.enums.MessageStatus;
import ru.cs.vsu.social_network.messaging_service.exception.message.MessageNotFoundException;
import ru.cs.vsu.social_network.messaging_service.provider.AbstractEntityProvider;
import ru.cs.vsu.social_network.messaging_service.provider.MessageEntityProvider;
import ru.cs.vsu.social_network.messaging_service.repository.MessageRepository;
import ru.cs.vsu.social_network.messaging_service.utils.MessageConstants;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Реализация провайдера для получения сущности Message.
 * Обеспечивает доступ к данным сообщений с обработкой исключительных ситуаций.
 * Оптимизирован для работы с большими объемами данных в мессенджере.
 * Поддерживает батч-операции и эффективные запросы с предзагрузкой.
 */
@Slf4j
@Component
public final class  MessageEntityProviderImpl extends AbstractEntityProvider<Message>
        implements MessageEntityProvider {

    private static final String ENTITY_NAME = "СООБЩЕНИЕ";
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_LIMIT = 50;

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
    public Long getUnreadMessagesCountInConversation(UUID receiverId,
                                                     UUID conversationId,
                                                     MessageStatus status) {
        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_НЕПРОЧИТАННЫХ_В_БЕСЕДЕ_НАЧАЛО: " +
                        "для пользователя с ID: {}, беседа: {}, статус: {}",
                ENTITY_NAME, receiverId, conversationId, status);

        final long count = messageRepository.countByReceiverIdAndConversationIdAndStatus(
                receiverId, conversationId, status);

        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_НЕПРОЧИТАННЫХ_В_БЕСЕДЕ_УСПЕХ: " +
                        "для пользователя с ID: {} в беседе {} найдено {} сообщений со статусом {}",
                ENTITY_NAME, receiverId, conversationId, count, status);

        return count;
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

        final int effectiveLimit = Math.max(1, Math.min(limit, MAX_LIMIT));
        final List<Message> messages = messageRepository
                .findRecentMessagesForConversations(conversationIds, effectiveLimit);

        log.info("{}_ПРОВАЙДЕР_ПАКЕТНОЕ_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_УСПЕХ: " +
                "получено {} сообщений для {} бесед", messages.size(), conversationIds.size());

        return messages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Message> getMessagesWithConversations(List<UUID> messageIds) {
        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_С_БЕСЕДАМИ_НАЧАЛО: " +
                "для {} сообщений", messageIds.size());

        if (messageIds.isEmpty()) {
            log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_С_БЕСЕДАМИ_ПУСТОЙ_СПИСОК", ENTITY_NAME);
            return Collections.emptyList();
        }

        final List<Message> messages = messageRepository.findMessagesWithConversations(messageIds);

        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_С_БЕСЕДАМИ_УСПЕХ: " +
                "получено {} сообщений с беседами", messages.size());

        return messages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Message> getMessagesByConversation(UUID conversationId, int page, int size) {
        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_БЕСЕДЫ_НАЧАЛО: " +
                "для беседы {}, страница: {}, размер: {}", conversationId, page, size);

        final int effectivePage = Math.max(0, page);
        final int effectiveSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        final PageRequest pageRequest = PageRequest.of(effectivePage, effectiveSize);

        final List<Message> messages = messageRepository
                .findAllByConversationId(conversationId, pageRequest)
                .getContent();

        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_БЕСЕДЫ_УСПЕХ: " +
                "для беседы {} найдено {} сообщений", conversationId, messages.size());

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
        final int effectiveSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
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
        final int effectiveSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        final PageRequest pageRequest = PageRequest.of(effectivePage, effectiveSize);

        final List<Message> messages = messageRepository
                .findBySenderIdOrderByCreatedAtDesc(userId, pageRequest)
                .getContent();

        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_ОТПРАВИТЕЛЯ_УСПЕХ: " +
                "для пользователя {} найдено {} сообщений", userId, messages.size());

        return messages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Message> getUnreadMessagesInConversation(UUID receiverId,
                                                         UUID conversationId,
                                                         MessageStatus status) {
        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_НЕПРОЧИТАННЫХ_В_БЕСЕДЕ_НАЧАЛО: " +
                        "для пользователя {} в беседе {} со статусом {}",
                receiverId, conversationId, status);

        final List<Message> messages = messageRepository
                .findByReceiverIdAndConversationIdAndStatus(receiverId, conversationId, status);

        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_НЕПРОЧИТАННЫХ_В_БЕСЕДЕ_УСПЕХ: " +
                        "найдено {} непрочитанных сообщений для пользователя {} в беседе {}",
                messages.size(), receiverId, conversationId);

        return messages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Message> getMessagesByIds(List<UUID> messageIds, int page, int size) {
        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_ПО_ID_НАЧАЛО: " +
                "для {} ID, страница: {}, размер: {}", messageIds.size(), page, size);

        if (messageIds.isEmpty()) {
            log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_ПО_ID_ПУСТОЙ_СПИСОК", ENTITY_NAME);
            return Collections.emptyList();
        }

        final int effectivePage = Math.max(0, page);
        final int effectiveSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        final PageRequest pageRequest = PageRequest.of(effectivePage, effectiveSize);

        final List<Message> messages = messageRepository
                .findByIdIn(messageIds, pageRequest)
                .getContent();

        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_СООБЩЕНИЙ_ПО_ID_УСПЕХ: " +
                        "получено {} сообщений из {} запрошенных ID",
                messages.size(), messageIds.size());

        return messages;
    }
}