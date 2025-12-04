package ru.cs.vsu.social_network.messaging_service.provider.providerImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.messaging_service.entity.Conversation;
import ru.cs.vsu.social_network.messaging_service.exception.conversation.ConversationNotFoundException;
import ru.cs.vsu.social_network.messaging_service.provider.AbstractEntityProvider;
import ru.cs.vsu.social_network.messaging_service.provider.ConversationEntityProvider;
import ru.cs.vsu.social_network.messaging_service.repository.ConversationRepository;
import ru.cs.vsu.social_network.messaging_service.utils.MessageConstants;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Реализация провайдера для получения сущности Conversation.
 * Обеспечивает доступ к данным бесед с обработкой исключительных ситуаций.
 * Оптимизирован для работы с большими объемами данных в мессенджере.
 * Поддерживает батч-операции и эффективные запросы.
 */
@Slf4j
@Component
public final class ConversationEntityProviderImpl extends AbstractEntityProvider<Conversation>
        implements ConversationEntityProvider {

    private static final String ENTITY_NAME = "БЕСЕДА";
    private static final int FIRST_PAGES_LIMIT = 10;
    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_LIMIT = 20;

    private final ConversationRepository conversationRepository;

    public ConversationEntityProviderImpl(ConversationRepository conversationRepository) {
        super(conversationRepository, ENTITY_NAME, () ->
                new ConversationNotFoundException(MessageConstants.CONVERSATION_NOT_FOUND_FAILURE));
        this.conversationRepository = conversationRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Conversation> getConversationBetweenUsers(UUID user1Id, UUID user2Id) {
        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_БЕСЕДЫ_МЕЖДУ_ПОЛЬЗОВАТЕЛЯМИ_НАЧАЛО: " +
                "между пользователями {} и {}", user1Id, user2Id);

        final Optional<Conversation> conversation = conversationRepository
                .findConversationBetweenUsers(user1Id, user2Id);

        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_БЕСЕДЫ_МЕЖДУ_ПОЛЬЗОВАТЕЛЯМИ_УСПЕХ: " +
                        "беседа {} найдена между пользователями {} и {}",
                conversation.isPresent() ? "успешно" : "не", user1Id, user2Id);

        return conversation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsConversationBetweenUsers(UUID user1Id, UUID user2Id) {
        log.info("{}_ПРОВАЙДЕР_ПРОВЕРКА_СУЩЕСТВОВАНИЯ_БЕСЕДЫ_НАЧАЛО: " +
                "между пользователями {} и {}", user1Id, user2Id);

        final boolean exists = conversationRepository.existsConversationBetweenUsers(user1Id, user2Id);

        log.info("{}_ПРОВАЙДЕР_ПРОВЕРКА_СУЩЕСТВОВАНИЯ_БЕСЕДЫ_УСПЕХ: " +
                        "беседа между пользователями {} и {} {}",
                user1Id, user2Id, exists ? "существует" : "не существует");

        return exists;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Conversation> getConversationsByUser(UUID userId, int page, int size) {
        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_БЕСЕД_ПОЛЬЗОВАТЕЛЯ_НАЧАЛО: " +
                "для пользователя {}, страница: {}, размер: {}", userId, page, size);

        final int effectivePage = Math.max(0, page);
        final int effectiveSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        final PageRequest pageRequest = PageRequest.of(effectivePage, effectiveSize);

        final List<Conversation> conversations = conversationRepository
                .findByUserIdOrderByUpdatedAtDesc(userId, pageRequest)
                .getContent();

        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_БЕСЕД_ПОЛЬЗОВАТЕЛЯ_УСПЕХ: " +
                "для пользователя {} найдено {} бесед", userId, conversations.size());

        return conversations;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getConversationsCountByUser(UUID userId) {
        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_БЕСЕД_НАЧАЛО: " +
                "для пользователя {}", userId);

        final Long count = conversationRepository.countByUserId(userId);

        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_БЕСЕД_УСПЕХ: " +
                "для пользователя {} найдено {} бесед", userId, count);

        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Conversation> getConversationsByIds(List<UUID> conversationIds) {
        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_БЕСЕД_ПО_ID_НАЧАЛО: " +
                "для {} ID", conversationIds.size());

        if (conversationIds.isEmpty()) {
            log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_БЕСЕД_ПО_ID_ПУСТОЙ_СПИСОК", ENTITY_NAME);
            return Collections.emptyList();
        }

        final List<Conversation> conversations = conversationRepository.findByIdIn(conversationIds);

        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_БЕСЕД_ПО_ID_УСПЕХ: " +
                        "получено {} бесед из {} запрошенных ID",
                conversations.size(), conversationIds.size());

        return conversations;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<UUID> getInterlocutorId(UUID conversationId, UUID currentUserId) {
        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_СОБЕСЕДНИКА_НАЧАЛО: " +
                "для беседы {} и пользователя {}", conversationId, currentUserId);

        final Optional<UUID> interlocutorId = conversationRepository
                .findInterlocutorId(conversationId, currentUserId);

        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_СОБЕСЕДНИКА_УСПЕХ: " +
                        "собеседник {} найден для беседы {} и пользователя {}",
                interlocutorId.isPresent() ? "успешно" : "не", conversationId, currentUserId);

        return interlocutorId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Conversation> getRecentConversationsByUser(UUID userId, int limit) {
        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_ПОСЛЕДНИХ_БЕСЕД_НАЧАЛО: " +
                "для пользователя {} с лимитом {}", userId, limit);

        final int effectiveLimit = Math.max(1, Math.min(limit, MAX_LIMIT));
        final PageRequest pageRequest = PageRequest.of(0, effectiveLimit);

        final List<Conversation> conversations = conversationRepository
                .findRecentConversationsByUserId(userId, FIRST_PAGES_LIMIT,
                        pageRequest);

        log.info("{}_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_ПОСЛЕДНИХ_БЕСЕД_УСПЕХ: " +
                "для пользователя {} найдено {} бесед", userId, conversations.size());

        return conversations;
    }
}