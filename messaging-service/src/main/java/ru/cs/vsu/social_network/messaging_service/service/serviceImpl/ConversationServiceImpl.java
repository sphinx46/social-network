package ru.cs.vsu.social_network.messaging_service.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cs.vsu.social_network.messaging_service.config.cache.CacheConfig;
import ru.cs.vsu.social_network.messaging_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.ConversationDetailsResponse;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.ConversationResponse;
import ru.cs.vsu.social_network.messaging_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.messaging_service.entity.Conversation;
import ru.cs.vsu.social_network.messaging_service.exception.conversation.ConversationNotFoundException;
import ru.cs.vsu.social_network.messaging_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.messaging_service.provider.ConversationEntityProvider;
import ru.cs.vsu.social_network.messaging_service.repository.ConversationRepository;
import ru.cs.vsu.social_network.messaging_service.service.ConversationService;
import ru.cs.vsu.social_network.messaging_service.service.aggregator.ConversationDetailsAggregator;
import ru.cs.vsu.social_network.messaging_service.service.cache.MessagingCacheEventPublisherService;
import ru.cs.vsu.social_network.messaging_service.utils.MessageConstants;
import ru.cs.vsu.social_network.messaging_service.utils.factory.messaging.ConversationFactory;
import ru.cs.vsu.social_network.messaging_service.validation.ConversationValidator;

import java.util.UUID;

/**
 * Реализация сервиса для работы с беседами (переписками).
 * Обеспечивает бизнес-логику создания, получения и управления беседами между пользователями.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationEntityProvider conversationEntityProvider;
    private final ConversationValidator conversationValidator;
    private final ConversationDetailsAggregator conversationDetailsAggregator;
    private final EntityMapper entityMapper;
    private final ConversationFactory conversationFactory;
    private final MessagingCacheEventPublisherService cacheEventPublisherService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    value = CacheConfig.USER_CONVERSATIONS_CACHE,
                    key = "'user:' + #user1Id + '*'"
            ),
            @CacheEvict(
                    value = CacheConfig.USER_CONVERSATIONS_CACHE,
                    key = "'user:' + #user2Id + '*'"
            ),
            @CacheEvict(
                    value = CacheConfig.CONVERSATION_BETWEEN_USERS_CACHE,
                    key = "'user1:' + #user1Id + ':user2:' + #user2Id"
            ),
            @CacheEvict(
                    value = CacheConfig.CONVERSATION_BETWEEN_USERS_CACHE,
                    key = "'user1:' + #user2Id + ':user2:' + #user1Id"
            )
    })
    public ConversationResponse createOrGetConversation(final UUID user1Id, final UUID user2Id) {
        final String logPrefix = "БЕСЕДА_СЕРВИС_СОЗДАНИЕ_ИЛИ_ПОЛУЧЕНИЕ";
        log.info("{}_НАЧАЛО: создание или получение беседы между пользователями {} и {}",
                logPrefix, user1Id, user2Id);

        conversationValidator.validateUsersNotSame(user1Id, user2Id);

        final Conversation existingConversation = conversationEntityProvider
                .getConversationBetweenUsers(user1Id, user2Id)
                .orElse(null);

        if (existingConversation != null) {
            log.info("{}_НАЙДЕНА_СУЩЕСТВУЮЩАЯ: найдена существующая беседа с ID: {}",
                    logPrefix, existingConversation.getId());
            return entityMapper.map(existingConversation, ConversationResponse.class);
        }

        final Conversation newConversation = conversationFactory.buildNewConversation(user1Id, user2Id);
        final Conversation savedConversation = conversationRepository.save(newConversation);

        cacheEventPublisherService.publishConversationCreated(this,
                savedConversation,
                savedConversation.getId(),
                user1Id,
                user2Id);

        log.info("{}_УСПЕХ: создана новая беседа с ID: {}",
                logPrefix, savedConversation.getId());

        return entityMapper.map(savedConversation, ConversationResponse.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheConfig.CONVERSATION_DETAILS_CACHE,
            key = "'conversation:' + #conversationId",
            unless = "#result == null"
    )
    public ConversationResponse getConversationById(final UUID conversationId) {
        final String logPrefix = "БЕСЕДА_СЕРВИС_ПОЛУЧЕНИЕ_ПО_ID";
        log.info("{}_НАЧАЛО: запрос беседы с ID: {}", logPrefix, conversationId);

        final Conversation conversation = conversationEntityProvider.getById(conversationId);

        log.info("{}_УСПЕХ: беседа с ID: {} найдена", logPrefix, conversationId);

        return entityMapper.map(conversation, ConversationResponse.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheConfig.CONVERSATION_BETWEEN_USERS_CACHE,
            key = "'user1:' + #user1Id + ':user2:' + #user2Id",
            unless = "#result == null"
    )
    public ConversationResponse getConversationBetweenUsers(final UUID user1Id, final UUID user2Id) {
        final String logPrefix = "БЕСЕДА_СЕРВИС_ПОЛУЧЕНИЕ_МЕЖДУ_ПОЛЬЗОВАТЕЛЯМИ";
        log.info("{}_НАЧАЛО: запрос беседы между пользователями {} и {}", logPrefix, user1Id, user2Id);

        final Conversation conversation = conversationEntityProvider
                .getConversationBetweenUsers(user1Id, user2Id)
                .orElseThrow(() -> {
                    log.error("{}_НЕ_НАЙДЕНА: беседа между пользователями не найдена", logPrefix);
                    return new ConversationNotFoundException(MessageConstants.CONVERSATION_NOT_FOUND_FAILURE);
                });

        log.info("{}_УСПЕХ: беседа с ID: {} найдена", logPrefix, conversation.getId());

        return entityMapper.map(conversation, ConversationResponse.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheConfig.USER_CONVERSATIONS_CACHE,
            key = "'user:' + #userId + ':page:' + #pageRequest.pageNumber + ':size:' + #pageRequest.size + ':sort:' + #pageRequest.sortBy + ':dir:' + #pageRequest.direction",
            unless = "#result == null or #result.content.isEmpty()"
    )
    public PageResponse<ConversationResponse> getUserConversations(final UUID userId,
                                                                   final PageRequest pageRequest) {
        final String logPrefix = "БЕСЕДА_СЕРВИС_ПОЛУЧЕНИЕ_БЕСЕД_ПОЛЬЗОВАТЕЛЯ";
        log.info("{}_НАЧАЛО: запрос бесед пользователя {}, страница: {}",
                logPrefix, userId, pageRequest.getPageNumber());

        final Pageable pageable = pageRequest.toPageable();
        final Page<Conversation> conversationsPage = conversationRepository
                .findByUserIdOrderByUpdatedAtDesc(userId, pageable);

        final PageResponse<ConversationResponse> response = PageResponse.of(
                conversationsPage.map(conversation ->
                        entityMapper.map(conversation, ConversationResponse.class))
        );

        log.info("{}_УСПЕХ: найдено {} бесед, всего страниц: {}",
                logPrefix, conversationsPage.getTotalElements(), conversationsPage.getTotalPages());

        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheConfig.CONVERSATION_DETAILS_CACHE,
            key = "'conversation:' + #conversationId + ':messagesLimit:' + #messagesLimit",
            unless = "#result == null"
    )
    public ConversationDetailsResponse getConversationWithMessages(final UUID conversationId,
                                                                   final int messagesLimit) {
        final String logPrefix = "БЕСЕДА_СЕРВИС_ПОЛУЧЕНИЕ_С_СООБЩЕНИЯМИ";
        log.info("{}_НАЧАЛО: запрос беседы с ID: {}, лимит: {}",
                logPrefix, conversationId, messagesLimit);

        final Conversation conversation = conversationEntityProvider.getById(conversationId);
        final ConversationDetailsResponse response = conversationDetailsAggregator
                .aggregateConversationDetails(conversation, true, messagesLimit);

        log.info("{}_УСПЕХ: беседа получена с {} сообщениями",
                logPrefix, response.getMessagesCount());

        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheConfig.CONVERSATION_MESSAGES_CACHE,
            key = "'user1:' + #user1Id + ':user2:' + #user2Id + ':page:' + #pageRequest.pageNumber + ':size:' + #pageRequest.size",
            unless = "#result == null or #result.content.isEmpty()"
    )
    public PageResponse<ConversationDetailsResponse> getConversationBetweenUsersWithMessages(final UUID user1Id,
                                                                                             final UUID user2Id,
                                                                                             final PageRequest pageRequest) {
        final String logPrefix = "БЕСЕДА_СЕРВИС_ПОЛУЧЕНИЕ_МЕЖДУ_ПОЛЬЗОВАТЕЛЯМИ_С_СООБЩЕНИЯМИ";
        log.info("{}_НАЧАЛО: запрос беседы между пользователями с сообщениями, страница: {}",
                logPrefix, pageRequest.getPageNumber());

        final Conversation conversation = conversationEntityProvider
                .getConversationBetweenUsers(user1Id, user2Id)
                .orElseThrow(() -> {
                    log.error("{}_НЕ_НАЙДЕНА: беседа между пользователями не найдена", logPrefix);
                    return new ConversationNotFoundException(MessageConstants.CONVERSATION_NOT_FOUND_FAILURE);
                });

        final Pageable pageable = pageRequest.toPageable();
        final Page<Conversation> conversationPage = new PageImpl<>(
                java.util.List.of(conversation),
                pageable,
                1
        );

        final Page<ConversationDetailsResponse> aggregatedPage = conversationDetailsAggregator
                .aggregateConversationsPage(conversationPage, true,
                        pageRequest.getSize());

        log.info("{}_УСПЕХ: беседа получена с сообщениями", logPrefix);

        return PageResponse.of(aggregatedPage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    value = CacheConfig.CONVERSATION_DETAILS_CACHE,
                    key = "'conversation:' + #conversationId + '*'"
            ),
            @CacheEvict(
                    value = CacheConfig.CONVERSATION_MESSAGES_CACHE,
                    key = "'*conversation:' + #conversationId + '*'"
            ),
            @CacheEvict(
                    value = CacheConfig.USER_CONVERSATIONS_CACHE,
                    allEntries = true
            )
    })
    public void deleteConversation(final UUID keycloakUserId,
                                   final UUID conversationId) {
        final String logPrefix = "БЕСЕДА_СЕРВИС_УДАЛЕНИЕ";
        log.info("{}_НАЧАЛО: удаление беседы с ID: {}", logPrefix, conversationId);

        conversationValidator.validateOwnership(keycloakUserId, conversationId);

        final Conversation conversation = conversationEntityProvider.getById(conversationId);

        log.info("{}_УДАЛЕНИЕ_СООБЩЕНИЙ: удаление всех сообщений беседы {}",
                logPrefix, conversationId);
        int deletedMessagesCount = conversationRepository.deleteAllByConversationId(conversationId);
        log.info("{}_УДАЛЕНО_СООБЩЕНИЙ: удалено {} сообщений беседы {}",
                logPrefix, deletedMessagesCount, conversationId);

        conversationRepository.delete(conversation);

        log.info("{}_УСПЕХ: беседа успешно удалена", logPrefix);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            value = CacheConfig.CONVERSATION_BETWEEN_USERS_CACHE,
            key = "'exists:user1:' + #user1Id + ':user2:' + #user2Id",
            unless = "#result == null"
    )
    public boolean existsConversationBetweenUsers(final UUID user1Id, final UUID user2Id) {
        final String logPrefix = "БЕСЕДА_СЕРВИС_ПРОВЕРКА_СУЩЕСТВОВАНИЯ";
        log.info("{}_НАЧАЛО: проверка существования беседы между пользователями", logPrefix);

        final boolean exists = conversationEntityProvider.existsConversationBetweenUsers(user1Id, user2Id);

        log.info("{}_УСПЕХ: беседа {}", logPrefix, exists ? "существует" : "не существует");

        return exists;
    }
}