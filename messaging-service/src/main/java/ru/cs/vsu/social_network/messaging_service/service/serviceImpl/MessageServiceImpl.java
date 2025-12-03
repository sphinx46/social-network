package ru.cs.vsu.social_network.messaging_service.service.serviceImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.cs.vsu.social_network.messaging_service.dto.request.messaging.*;
import ru.cs.vsu.social_network.messaging_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.MessageResponse;
import ru.cs.vsu.social_network.messaging_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.messaging_service.entity.Message;
import ru.cs.vsu.social_network.messaging_service.entity.enums.MessageStatus;
import ru.cs.vsu.social_network.messaging_service.exception.message.MessageUploadImageException;
import ru.cs.vsu.social_network.messaging_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.messaging_service.provider.ConversationEntityProvider;
import ru.cs.vsu.social_network.messaging_service.provider.MessageEntityProvider;
import ru.cs.vsu.social_network.messaging_service.repository.MessageRepository;
import ru.cs.vsu.social_network.messaging_service.service.MessageService;
import ru.cs.vsu.social_network.messaging_service.service.batch.BatchMessageService;
import ru.cs.vsu.social_network.messaging_service.service.cache.MessagingCacheEventPublisherService;
import ru.cs.vsu.social_network.messaging_service.utils.MessageConstants;
import ru.cs.vsu.social_network.messaging_service.utils.factory.messaging.MessageFactory;
import ru.cs.vsu.social_network.messaging_service.validation.ConversationValidator;
import ru.cs.vsu.social_network.messaging_service.validation.MessageValidator;

import java.util.List;
import java.util.UUID;

/**
 * Реализация сервиса для работы с сообщениями.
 * Обеспечивает бизнес-логику создания, редактирования, удаления и управления сообщениями.
 */
@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final MessageEntityProvider messageEntityProvider;
    private final MessageValidator messageValidator;
    private final EntityMapper entityMapper;
    private final MessageFactory messageFactory;
    private final BatchMessageService batchMessageService;
    private final ConversationValidator conversationValidator;
    private final ConversationEntityProvider conversationEntityProvider;
    private final MessagingCacheEventPublisherService cacheEventPublisherService;

    public MessageServiceImpl(final MessageRepository messageRepository,
                              final MessageEntityProvider messageEntityProvider,
                              final MessageValidator messageValidator,
                              final EntityMapper entityMapper,
                              final MessageFactory messageFactory,
                              final BatchMessageService batchMessageService,
                              final ConversationValidator conversationValidator,
                              final ConversationEntityProvider conversationEntityProvider,
                              final MessagingCacheEventPublisherService cacheEventPublisherService) {
        this.messageRepository = messageRepository;
        this.messageEntityProvider = messageEntityProvider;
        this.messageValidator = messageValidator;
        this.entityMapper = entityMapper;
        this.messageFactory = messageFactory;
        this.batchMessageService = batchMessageService;
        this.conversationValidator = conversationValidator;
        this.conversationEntityProvider = conversationEntityProvider;
        this.cacheEventPublisherService = cacheEventPublisherService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MessageResponse createMessage(final UUID keycloakUserId,
                                         final MessageCreateRequest messageCreateRequest) {
        log.info("СООБЩЕНИЕ_СЕРВИС_СОЗДАНИЕ_НАЧАЛО: " +
                        "создание сообщения пользователем: {}, " +
                        "получатель: {}, длина контента: {}",
                keycloakUserId, messageCreateRequest.getReceiverId(),
                messageCreateRequest.getContent().length());

        final Message message = messageFactory.create(keycloakUserId, messageCreateRequest);
        final Message savedMessage = messageRepository.save(message);

        cacheEventPublisherService.publishMessageCreated(
                this,
                savedMessage,
                message.getConversation().getId(),
                savedMessage.getId(),
                keycloakUserId,
                messageCreateRequest.getReceiverId()
        );

        log.info("СООБЩЕНИЕ_СЕРВИС_СОЗДАНИЕ_УСПЕХ: " +
                        "сообщение успешно создано с ID: {} пользователем: {}, получатель: {}",
                savedMessage.getId(), keycloakUserId, messageCreateRequest.getReceiverId());

        return entityMapper.map(savedMessage, MessageResponse.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MessageResponse editMessage(final UUID keycloakUserId,
                                       final MessageEditRequest messageEditRequest) {
        log.info("СООБЩЕНИЕ_СЕРВИС_РЕДАКТИРОВАНИЕ_НАЧАЛО: " +
                        "редактирование сообщения с ID: {} пользователем: {}, " +
                        "новая длина контента: {}",
                messageEditRequest.getMessageId(), keycloakUserId,
                messageEditRequest.getContent().length());

        messageValidator.validateOwnership(keycloakUserId, messageEditRequest.getMessageId());

        final Message message = messageEntityProvider.getById(messageEditRequest.getMessageId());
        message.setContent(messageEditRequest.getContent());
        final Message updatedMessage = messageRepository.save(message);

        cacheEventPublisherService.publishMessageUpdated(
                this,
                updatedMessage,
                message.getConversation().getId(),
                updatedMessage.getId()
        );

        log.info("СООБЩЕНИЕ_СЕРВИС_РЕДАКТИРОВАНИЕ_УСПЕХ: " +
                        "сообщение с ID: {} успешно обновлено пользователем: {}",
                messageEditRequest.getMessageId(), keycloakUserId);

        return entityMapper.map(updatedMessage, MessageResponse.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MessageResponse deleteMessage(final UUID keycloakUserId,
                                         final MessageDeleteRequest messageDeleteRequest) {
        log.info("СООБЩЕНИЕ_СЕРВИС_УДАЛЕНИЕ_НАЧАЛО: " +
                        "удаление сообщения с ID: {} пользователем: {}",
                messageDeleteRequest.getMessageId(), keycloakUserId);

        messageValidator.validateOwnership(keycloakUserId, messageDeleteRequest.getMessageId());

        final Message message = messageEntityProvider.getById(messageDeleteRequest.getMessageId());
        final UUID conversationId = message.getConversation().getId();
        messageRepository.delete(message);

        cacheEventPublisherService.publishMessageDeleted(
                this,
                message,
                conversationId,
                messageDeleteRequest.getMessageId()
        );


        log.info("СООБЩЕНИЕ_СЕРВИС_УДАЛЕНИЕ_УСПЕХ: " +
                        "сообщение с ID: {} успешно удалено пользователем: {}",
                messageDeleteRequest.getMessageId(), keycloakUserId);

        return entityMapper.map(message, MessageResponse.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MessageResponse uploadImage(final UUID keycloakUserId,
                                       final MessageUploadImageRequest request) {
        log.info("СООБЩЕНИЕ_СЕРВИС_ЗАГРУЗКА_ИЗОБРАЖЕНИЯ_НАЧАЛО: " +
                        "загрузка изображения для сообщения с ID: {} пользователем: {}, " +
                        "URL изображения: {}",
                request.getMessageId(), keycloakUserId, request.getImageUrl());

        messageValidator.validateOwnership(keycloakUserId, request.getMessageId());

        final Message message = messageEntityProvider.getById(request.getMessageId());

        final String imageUrl = request.getImageUrl();
        if (!StringUtils.hasText(imageUrl)) {
            log.error("СООБЩЕНИЕ_СЕРВИС_ЗАГРУЗКА_ИЗОБРАЖЕНИЯ_ОШИБКА: " +
                            "URL изображения пустой для сообщения с ID: {}, пользователь: {}",
                    request.getMessageId(), keycloakUserId);
            throw new MessageUploadImageException(MessageConstants.MESSAGE_UPLOAD_IMAGE_FAILURE);
        }

        message.setImageUrl(imageUrl);
        final Message updatedMessage = messageRepository.save(message);

        cacheEventPublisherService.publishMessageImageUploaded(
                this,
                updatedMessage,
                message.getConversation().getId(),
                request.getMessageId()
        );

        log.info("СООБЩЕНИЕ_СЕРВИС_ЗАГРУЗКА_ИЗОБРАЖЕНИЯ_УСПЕХ: " +
                        "изображение загружено для сообщения с ID: {} пользователем: {}, " +
                        "URL: {}",
                request.getMessageId(), keycloakUserId, request.getImageUrl());

        return entityMapper.map(updatedMessage, MessageResponse.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public MessageResponse removeImage(final UUID keycloakUserId,
                                       final MessageRemoveImageRequest messageRemoveImageRequest) {
        log.info("СООБЩЕНИЕ_СЕРВИС_УДАЛЕНИЕ_ИЗОБРАЖЕНИЯ_НАЧАЛО: " +
                        "удаление изображения у сообщения с ID: {} пользователем: {}",
                messageRemoveImageRequest.getMessageId(), keycloakUserId);

        messageValidator.validateOwnership(keycloakUserId, messageRemoveImageRequest.getMessageId());

        final Message message = messageEntityProvider.getById(messageRemoveImageRequest.getMessageId());
        message.setImageUrl(null);
        final Message updatedMessage = messageRepository.save(message);

        cacheEventPublisherService.publishMessageUpdated(
                this,
                updatedMessage,
                message.getConversation().getId(),
                messageRemoveImageRequest.getMessageId()
        );

        log.info("СООБЩЕНИЕ_СЕРВИС_УДАЛЕНИЕ_ИЗОБРАЖЕНИЯ_УСПЕХ: " +
                        "изображение удалено у сообщения с ID: {} пользователем: {}",
                messageRemoveImageRequest.getMessageId(), keycloakUserId);

        return entityMapper.map(updatedMessage, MessageResponse.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public MessageResponse getMessageById(final UUID messageId) {
        log.info("СООБЩЕНИЕ_СЕРВИС_ПОЛУЧЕНИЕ_ПО_ID_НАЧАЛО: " +
                "запрос сообщения с ID: {}", messageId);

        final Message message = messageEntityProvider.getById(messageId);

        log.info("СООБЩЕНИЕ_СЕРВИС_ПОЛУЧЕНИЕ_ПО_ID_УСПЕХ: " +
                        "сообщение с ID: {} найдено, отправитель: {}, получатель: {}",
                messageId, message.getSenderId(), message.getReceiverId());

        return entityMapper.map(message, MessageResponse.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public int markMessagesAsDelivered(final UUID receiverId, final List<UUID> messageIds) {
        log.info("СООБЩЕНИЕ_СЕРВИС_ОТМЕТКА_ДОСТАВЛЕННЫМИ_НАЧАЛО: " +
                "для пользователя {}, {} сообщений", receiverId, messageIds.size());

        final int updatedCount = batchMessageService.batchUpdateMessagesStatus(
                receiverId, messageIds, MessageStatus.SENT, MessageStatus.DELIVERED);

        log.info("СООБЩЕНИЕ_СЕРВИС_ОТМЕТКА_ДОСТАВЛЕННЫМИ_УСПЕХ: " +
                "отмечено как доставлено {} сообщений для пользователя {}", updatedCount, receiverId);

        return updatedCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public int markConversationAsRead(final UUID userId, final UUID conversationId) {
        final String logPrefix = "СООБЩЕНИЕ_СЕРВИС_ОТМЕТКА_БЕСЕДЫ_ПРОЧИТАННОЙ";
        log.info("{}_НАЧАЛО: отметка беседы {} как прочитанной пользователем {}",
                logPrefix, conversationId, userId);

        try {
            conversationValidator.validateAccessToChat(conversationId, userId);

            final int markedCount =
                    batchMessageService.batchMarkConversationAsRead(userId, conversationId);

            if (markedCount > 0) {
                cacheEventPublisherService.publishMessagesRead(
                        this,
                        conversationEntityProvider.getById(conversationId),
                        conversationId,
                        userId
                );
            }

            log.info("{}_УСПЕХ: отмечено {} сообщений как прочитанные в беседе {}",
                    logPrefix, markedCount, conversationId);

            return markedCount;

        } catch (Exception e) {
            log.error("{}_ОШИБКА: при отметке беседы {} как прочитанной пользователем {}, ошибка: {}",
                    logPrefix, conversationId, userId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Long getUnreadMessagesCount(final UUID receiverId) {
        log.info("СООБЩЕНИЕ_СЕРВИС_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_НЕПРОЧИТАННЫХ_НАЧАЛО: " +
                "для пользователя {}", receiverId);

        final Long sentCount = batchMessageService.getUnreadMessagesCountByUser(receiverId,
                MessageStatus.SENT);
        final Long deliveredCount = batchMessageService.getUnreadMessagesCountByUser(receiverId,
                MessageStatus.DELIVERED);
        final Long totalUnreadCount = sentCount + deliveredCount;

        log.info("СООБЩЕНИЕ_СЕРВИС_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_НЕПРОЧИТАННЫХ_УСПЕХ: " +
                        "для пользователя {} найдено {} непрочитанных сообщений (SENT: {}, DELIVERED: {})",
                receiverId, totalUnreadCount, sentCount, deliveredCount);

        return totalUnreadCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<MessageResponse> getMessagesBetweenUsers(final UUID senderId,
                                                                 final UUID receiverId,
                                                                 final PageRequest pageRequest) {
        log.info("СООБЩЕНИЕ_СЕРВИС_ПОЛУЧЕНИЕ_МЕЖДУ_ПОЛЬЗОВАТЕЛЯМИ_НАЧАЛО: " +
                        "запрос сообщений от {} к {}, страница: {}, размер: {}",
                senderId, receiverId, pageRequest.getPageNumber(), pageRequest.getSize());

        final Pageable pageable = pageRequest.toPageable();
        final Page<Message> messagesPage = messageRepository
                .findMessagesBetweenUsers(senderId, receiverId, pageable);

        final PageResponse<MessageResponse> response = PageResponse.of(
                messagesPage.map(message -> entityMapper.map(message, MessageResponse.class))
        );

        log.info("СООБЩЕНИЕ_СЕРВИС_ПОЛУЧЕНИЕ_МЕЖДУ_ПОЛЬЗОВАТЕЛЯМИ_УСПЕХ: " +
                        "найдено {} сообщений между пользователями {} и {}, всего страниц: {}",
                messagesPage.getTotalElements(), senderId, receiverId, messagesPage.getTotalPages());

        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<MessageResponse> getMessagesBySender(final UUID userId,
                                                             final PageRequest pageRequest) {
        log.info("СООБЩЕНИЕ_СЕРВИС_ПОЛУЧЕНИЕ_ОТПРАВИТЕЛЯ_НАЧАЛО: " +
                        "запрос сообщений отправителя: {}, страница: {}, размер: {}",
                userId, pageRequest.getPageNumber(), pageRequest.getSize());

        final Pageable pageable = pageRequest.toPageable();
        final Page<Message> messagesPage = messageRepository
                .findBySenderIdOrderByCreatedAtDesc(userId, pageable);

        final PageResponse<MessageResponse> response = PageResponse.of(
                messagesPage.map(message -> entityMapper.map(message, MessageResponse.class))
        );

        log.info("СООБЩЕНИЕ_СЕРВИС_ПОЛУЧЕНИЕ_ОТПРАВИТЕЛЯ_УСПЕХ: " +
                        "найдено {} сообщений отправителя: {}, всего страниц: {}",
                messagesPage.getTotalElements(), userId, messagesPage.getTotalPages());

        return response;
    }
}