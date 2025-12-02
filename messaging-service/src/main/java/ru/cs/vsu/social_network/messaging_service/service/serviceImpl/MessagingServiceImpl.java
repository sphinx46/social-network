package ru.cs.vsu.social_network.messaging_service.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cs.vsu.social_network.messaging_service.dto.request.messaging.MessageCreateRequest;
import ru.cs.vsu.social_network.messaging_service.dto.request.messaging.MessageUploadImageRequest;
import ru.cs.vsu.social_network.messaging_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.ConversationDetailsResponse;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.ConversationResponse;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.MessageResponse;
import ru.cs.vsu.social_network.messaging_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.messaging_service.entity.Conversation;
import ru.cs.vsu.social_network.messaging_service.exception.conversation.ConversationNotFoundException;
import ru.cs.vsu.social_network.messaging_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.messaging_service.provider.ConversationEntityProvider;
import ru.cs.vsu.social_network.messaging_service.service.ConversationService;
import ru.cs.vsu.social_network.messaging_service.service.MessageService;
import ru.cs.vsu.social_network.messaging_service.service.MessagingService;
import ru.cs.vsu.social_network.messaging_service.service.aggregator.ConversationDetailsAggregator;
import ru.cs.vsu.social_network.messaging_service.service.batch.BatchMessageService;
import ru.cs.vsu.social_network.messaging_service.utils.MessageConstants;
import ru.cs.vsu.social_network.messaging_service.validation.ConversationValidator;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Реализация комплексного сервиса для работы с мессенджером.
 * Объединяет функциональность работы с сообщениями и беседами.
 * Предоставляет высокоуровневые методы для типичных сценариев использования мессенджера.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessagingServiceImpl implements MessagingService {

    private final MessageService messageService;
    private final ConversationService conversationService;
    private final BatchMessageService batchMessageService;
    private final ConversationDetailsAggregator conversationDetailsAggregator;
    private final ConversationEntityProvider conversationEntityProvider;
    private final EntityMapper entityMapper;
    private final ConversationValidator conversationValidator;

    private static final int DEFAULT_PREVIEW_LIMIT = 3;
    private static final int MAX_PREVIEW_LIMIT = 10;

    @Override
    @Transactional
    public MessageResponse sendMessage(final UUID senderId, final MessageCreateRequest request) {
        final String logPrefix = "МЕССЕНДЖЕР_СЕРВИС_ОТПРАВКА_СООБЩЕНИЯ";
        log.info("{}_НАЧАЛО: отправка сообщения от пользователя {} получателю {}",
                logPrefix, senderId, request.getReceiverId());

        conversationService.createOrGetConversation(senderId, request.getReceiverId());

        final MessageResponse messageResponse = messageService.createMessage(senderId, request);

        log.info("{}_УСПЕХ: сообщение отправлено с ID: {}", logPrefix, messageResponse.getMessageId());

        return messageResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ConversationDetailsResponse> getConversationWithUser(final UUID user1Id,
                                                                             final UUID user2Id,
                                                                             final PageRequest pageRequest) {
        final String logPrefix = "МЕССЕНДЖЕР_СЕРВИС_ПОЛУЧЕНИЕ_ПЕРЕПИСКИ_С_ПОЛЬЗОВАТЕЛЕМ";
        log.info("{}_НАЧАЛО: получение переписки между пользователями {} и {}, страница: {}",
                logPrefix, user1Id, user2Id, pageRequest.getPageNumber());

        final PageResponse<ConversationDetailsResponse> response = conversationService
                .getConversationBetweenUsersWithMessages(user1Id, user2Id, pageRequest);

        if (!response.getContent().isEmpty()) {
            log.info("{}_УСПЕХ: получено {} сообщений в переписке",
                    logPrefix, response.getContent().get(0).getMessagesCount());
        } else {
            log.info("{}_УСПЕХ: переписка пуста", logPrefix);
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ConversationDetailsResponse> getUserConversationsWithPreview(final UUID userId,
                                                                                     final PageRequest pageRequest,
                                                                                     final int previewMessagesLimit) {
        final String logPrefix = "МЕССЕНДЖЕР_СЕРВИС_ПОЛУЧЕНИЕ_БЕСЕД_С_ПРЕДПРОСМОТРОМ";
        log.info("{}_НАЧАЛО: получение бесед пользователя {} с предпросмотром, лимит: {}, страница: {}",
                logPrefix, userId, previewMessagesLimit, pageRequest.getPageNumber());

        final int effectiveLimit = Math.min(previewMessagesLimit, MAX_PREVIEW_LIMIT);

        final PageResponse<ConversationResponse> conversationsPage = conversationService
                .getUserConversations(userId, pageRequest);

        final List<ConversationDetailsResponse> detailedConversations =
                conversationsPage.getContent().stream()
                .map(conversationResponse -> {
                    final ConversationDetailsResponse details =
                            entityMapper.map(conversationResponse, ConversationDetailsResponse.class);

                    final List<MessageResponse> previewMessages = batchMessageService
                            .getMessagesByConversation(conversationResponse.getConversationId(),
                                    0, effectiveLimit);

                    details.setMessages(previewMessages);

                    if (!previewMessages.isEmpty()) {
                        details.setLastMessageId(previewMessages.get(0).getMessageId());
                    }

                    return details;
                })
                .collect(Collectors.toList());

        final PageResponse<ConversationDetailsResponse> response = PageResponse.of(
                pageRequest,
                detailedConversations,
                conversationsPage.getTotalElements()
        );

        log.info("{}_УСПЕХ: получено {} бесед с предпросмотром сообщений",
                logPrefix, detailedConversations.size());

        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public int markConversationAsRead(final UUID userId, final UUID conversationId) {
        final String logPrefix = "МЕССЕНДЖЕР_СЕРВИС_ОТМЕТКА_БЕСЕДЫ_ПРОЧИТАННОЙ";
        log.info("{}_НАЧАЛО: отметка беседы {} как прочитанной пользователем {}",
                logPrefix, conversationId, userId);

        try {
            conversationValidator.validateAccessToChat(conversationId, userId);

            final int markedCount = messageService.markConversationAsRead(userId, conversationId);

            log.info("{}_УСПЕХ: отмечено {} сообщений как прочитанные в беседе {}",
                    logPrefix, markedCount, conversationId);

            return markedCount;

        } catch (Exception e) {
            log.error("{}_ОШИБКА: при отметке беседы {} как прочитанной пользователем {}, ошибка: {}",
                    logPrefix, conversationId, userId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public MessageResponse uploadMessageImage(final UUID userId,
                                              final MessageUploadImageRequest request) {
        final String logPrefix = "МЕССЕНДЖЕР_СЕРВИС_ЗАГРУЗКА_ИЗОБРАЖЕНИЯ";
        log.info("{}_НАЧАЛО: загрузка изображения для сообщения {} пользователем {}",
                logPrefix, request.getMessageId(), userId);

        final MessageResponse messageResponse = messageService.uploadImage(userId, request);

        log.info("{}_УСПЕХ: изображение загружено для сообщения {}",
                logPrefix, request.getMessageId());

        return messageResponse;
    }

    @Override
    @Transactional
    public void deleteConversationWithUser(final UUID userId, final UUID otherUserId) {
        final String logPrefix = "МЕССЕНДЖЕР_СЕРВИС_УДАЛЕНИЕ_ПЕРЕПИСКИ";
        log.info("{}_НАЧАЛО: удаление переписки пользователя {} с пользователем {}",
                logPrefix, userId, otherUserId);

        final Conversation conversation = conversationEntityProvider
                .getConversationBetweenUsers(userId, otherUserId)
                .orElseThrow(() -> {
                    log.error("{}_НЕ_НАЙДЕНА: беседа между пользователями {} и {} не найдена",
                            logPrefix, userId, otherUserId);
                    return new ConversationNotFoundException(MessageConstants.CONVERSATION_NOT_FOUND_FAILURE);
                });

        conversationService.deleteConversation(userId, conversation.getId());

        log.info("{}_УСПЕХ: переписка между пользователями {} и {} удалена",
                logPrefix, userId, otherUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationDetailsResponse getChatInfo(final UUID userId, final UUID conversationId) {
        final String logPrefix = "МЕССЕНДЖЕР_СЕРВИС_ПОЛУЧЕНИЕ_ИНФОРМАЦИИ_О_ЧАТЕ";
        log.info("{}_НАЧАЛО: получение информации о чате {} для пользователя {}",
                logPrefix, conversationId, userId);

        conversationValidator.validateAccessToChat(conversationId, userId);

        final ConversationDetailsResponse response = conversationService
                .getConversationWithMessages(conversationId, DEFAULT_PREVIEW_LIMIT);

        log.info("{}_УСПЕХ: информация о чате {} получена",
                logPrefix, conversationId);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ConversationDetailsResponse> getUserConversationsDetailed(final UUID userId,
                                                                                  final PageRequest pageRequest) {
        final String logPrefix = "МЕССЕНДЖЕР_СЕРВИС_ПОЛУЧЕНИЕ_ДЕТАЛЬНЫХ_БЕСЕД";
        log.info("{}_НАЧАЛО: получение детальных бесед пользователя {}, страница: {}",
                logPrefix, userId, pageRequest.getPageNumber());

        final Pageable pageable = pageRequest.toPageable();
        final List<Conversation> conversations = conversationEntityProvider
                .getConversationsByUser(userId, pageRequest.getPageNumber(), pageRequest.getSize());

        final Long totalConversations = conversationEntityProvider.getConversationsCountByUser(userId);
        final Page<Conversation> conversationsPage = new PageImpl<>(conversations, pageable, totalConversations);

        final Page<ConversationDetailsResponse> aggregatedPage = conversationDetailsAggregator
                .aggregateConversationsPage(conversationsPage, true, DEFAULT_PREVIEW_LIMIT);

        final PageResponse<ConversationDetailsResponse> response = PageResponse.of(aggregatedPage);

        log.info("{}_УСПЕХ: получено {} детальных бесед",
                logPrefix, aggregatedPage.getTotalElements());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUnreadMessagesCountInConversation(final UUID userId, final UUID conversationId) {
        final String logPrefix = "МЕССЕНДЖЕР_СЕРВИС_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_НЕПРОЧИТАННЫХ_В_БЕСЕДЕ";
        log.info("{}_НАЧАЛО: получение количества непрочитанных в беседе {} для пользователя {}",
                logPrefix, conversationId, userId);

        conversationValidator.validateAccessToChat(conversationId, userId);

        final Long unreadCount = batchMessageService
                .getAllUnreadMessagesCountInConversation(userId, conversationId);

        log.info("{}_УСПЕХ: в беседе {} найдено {} непрочитанных сообщений",
                logPrefix, conversationId, unreadCount);

        return unreadCount;
    }
}