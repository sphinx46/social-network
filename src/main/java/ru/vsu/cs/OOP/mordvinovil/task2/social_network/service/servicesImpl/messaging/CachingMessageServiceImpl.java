package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.messaging;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.messaging.MessageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.common.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.messaging.MessageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.common.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Message;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.MessageStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.notification.NotificationEventPublisherService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.MessageRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.messaging.MessageCacheService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.messaging.MessageService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.MessageFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.MessageValidator;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging.CentralLogger;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.MessageStatusValidator.isStatusAllowed;

@RequiredArgsConstructor
@Service
public class CachingMessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final EntityMapper entityMapper;
    private final MessageFactory messageFactory;
    private final MessageValidator messageValidator;
    private final EntityUtils entityUtils;
    private final NotificationEventPublisherService notificationEventPublisherService;
    private final MessageCacheService messageCacheService;
    private final CentralLogger centralLogger;

    /**
     * Создает новое сообщение
     *
     * @param request запрос на создание сообщения
     * @param currentUser текущий пользователь-отправитель
     * @return ответ с созданным сообщением
     */
    @Transactional
    @Override
    public MessageResponse create(MessageRequest request, User currentUser) {
        Map<String, Object> context = new HashMap<>();
        context.put("senderId", currentUser.getId());
        context.put("receiverId", request.getReceiverUserId());
        context.put("contentLength", request.getContent() != null ? request.getContent().length() : 0);
        context.put("hasImage", request.getImageUrl() != null);

        centralLogger.logInfo("СООБЩЕНИЕ_СОЗДАНИЕ",
                "Создание нового сообщения", context);

        try {
            messageValidator.validateMessageCreation(request, currentUser);

            User receiver = entityUtils.getUser(request.getReceiverUserId());
            Message message = messageFactory.createMessage(currentUser, receiver, request);
            Message savedMessage = messageRepository.save(message);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("messageId", savedMessage.getId());

            centralLogger.logInfo("СООБЩЕНИЕ_СОЗДАНО",
                    "Сообщение успешно создано", successContext);

            messageCacheService.evictConversationCache(currentUser.getId(), receiver.getId());

            notificationEventPublisherService.publishMessageReceived(this, request.getReceiverUserId(),
                    currentUser.getId(), message.getContent());

            return entityMapper.map(savedMessage, MessageResponse.class);
        } catch (Exception e) {
            centralLogger.logError("СООБЩЕНИЕ_ОШИБКА_СОЗДАНИЯ",
                    "Ошибка при создании сообщения", context, e);
            throw e;
        }
    }

    /**
     * Получает сообщение по идентификатору
     *
     * @param messageId идентификатор сообщения
     * @param currentUser текущий пользователь
     * @return ответ с данными сообщения
     */
    @Override
    public MessageResponse getMessageById(Long messageId, User currentUser) {
        Map<String, Object> context = new HashMap<>();
        context.put("messageId", messageId);
        context.put("userId", currentUser.getId());

        centralLogger.logInfo("СООБЩЕНИЕ_ПОЛУЧЕНИЕ_ПО_ID",
                "Получение сообщения по идентификатору", context);

        try {
            Message message = entityUtils.getMessage(messageId);
            messageValidator.validateMessageAccess(currentUser, message);

            centralLogger.logInfo("СООБЩЕНИЕ_ПОЛУЧЕНО_ПО_ID",
                    "Сообщение успешно получено по идентификатору", context);

            return entityMapper.map(message, MessageResponse.class);
        } catch (Exception e) {
            centralLogger.logError("СООБЩЕНИЕ_ОШИБКА_ПОЛУЧЕНИЯ_ПО_ID",
                    "Ошибка при получении сообщения по идентификатору", context, e);
            throw e;
        }
    }

    /**
     * Получает переписку между текущим пользователем и другим пользователем с кешированием
     *
     * @param otherUserId идентификатор другого пользователя
     * @param currentUser текущий пользователь
     * @param pageRequest параметры пагинации
     * @return страница с сообщениями переписки
     */
    @Override
    @Cacheable(
            value = "conversation",
            key = "'conv:' + #currentUser.id + ':' + #otherUserId + ':page:' + #pageRequest.pageNumber + ':size:' + #pageRequest.size"
    )
    public PageResponse<MessageResponse> getConversation(Long otherUserId, User currentUser, PageRequest pageRequest) {
        Map<String, Object> context = new HashMap<>();
        context.put("currentUserId", currentUser.getId());
        context.put("otherUserId", otherUserId);
        context.put("page", pageRequest.getPageNumber());
        context.put("size", pageRequest.getSize());

        centralLogger.logInfo("ПЕРЕПИСКА_ПОЛУЧЕНИЕ_С_КЕШИРОВАНИЕМ",
                "Получение переписки с кешированием", context);

        try {
            Long id = entityUtils.getUser(otherUserId).getId();
            Page<Message> messages = messageRepository.findMessagesBetweenUsers(currentUser.getId(),
                            id, pageRequest.toPageable())
                    .orElse(Page.empty());

            Map<String, Object> resultContext = new HashMap<>(context);
            resultContext.put("totalMessages", messages.getTotalElements());
            resultContext.put("currentPageMessages", messages.getContent().size());

            centralLogger.logInfo("ПЕРЕПИСКА_ПОЛУЧЕНА_С_КЕШИРОВАНИЕМ",
                    "Переписка успешно получена с кешированием", resultContext);

            return PageResponse.of(messages.map(
                    message -> entityMapper.map(message, MessageResponse.class))
            );
        } catch (Exception e) {
            centralLogger.logError("ПЕРЕПИСКА_ОШИБКА_ПОЛУЧЕНИЯ_С_КЕШИРОВАНИЕМ",
                    "Ошибка при получении переписки с кешированием", context, e);
            throw e;
        }
    }

    /**
     * Получает отправленные сообщения пользователя
     *
     * @param currentUser текущий пользователь
     * @param pageRequest параметры пагинации
     * @return страница с отправленными сообщениями
     */
    @Override
    public PageResponse<MessageResponse> getSentMessages(User currentUser, PageRequest pageRequest) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", currentUser.getId());
        context.put("page", pageRequest.getPageNumber());
        context.put("size", pageRequest.getSize());

        centralLogger.logInfo("ОТПРАВЛЕННЫЕ_СООБЩЕНИЯ_ПОЛУЧЕНИЕ",
                "Получение отправленных сообщений", context);

        try {
            Page<Message> messages = messageRepository.findBySenderId(currentUser.getId(),
                            pageRequest.toPageable())
                    .orElse(Page.empty());

            Map<String, Object> resultContext = new HashMap<>(context);
            resultContext.put("totalMessages", messages.getTotalElements());
            resultContext.put("currentPageMessages", messages.getContent().size());

            centralLogger.logInfo("ОТПРАВЛЕННЫЕ_СООБЩЕНИЯ_ПОЛУЧЕНЫ",
                    "Отправленные сообщения успешно получены", resultContext);

            return PageResponse.of(messages.map(
                    message -> entityMapper.map(message, MessageResponse.class))
            );
        } catch (Exception e) {
            centralLogger.logError("ОТПРАВЛЕННЫЕ_СООБЩЕНИЯ_ОШИБКА_ПОЛУЧЕНИЯ",
                    "Ошибка при получении отправленных сообщений", context, e);
            throw e;
        }
    }

    /**
     * Получает полученные сообщения пользователя
     *
     * @param currentUser текущий пользователь
     * @param pageRequest параметры пагинации
     * @return страница с полученными сообщениями
     */
    @Override
    public PageResponse<MessageResponse> getReceivedMessages(User currentUser, PageRequest pageRequest) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", currentUser.getId());
        context.put("page", pageRequest.getPageNumber());
        context.put("size", pageRequest.getSize());
        context.put("status", MessageStatus.RECEIVED);

        centralLogger.logInfo("ПОЛУЧЕННЫЕ_СООБЩЕНИЯ_ПОЛУЧЕНИЕ",
                "Получение полученных сообщений", context);

        try {
            PageResponse<MessageResponse> response = getMessagesByStatus(currentUser, MessageStatus.RECEIVED, pageRequest);

            centralLogger.logInfo("ПОЛУЧЕННЫЕ_СООБЩЕНИЯ_ПОЛУЧЕНЫ",
                    "Полученные сообщения успешно получены", context);

            return response;
        } catch (Exception e) {
            centralLogger.logError("ПОЛУЧЕННЫЕ_СООБЩЕНИЯ_ОШИБКА_ПОЛУЧЕНИЯ",
                    "Ошибка при получении полученных сообщений", context, e);
            throw e;
        }
    }

    /**
     * Получает прочитанные сообщения пользователя
     *
     * @param currentUser текущий пользователь
     * @param pageRequest параметры пагинации
     * @return страница с прочитанными сообщениями
     */
    @Override
    public PageResponse<MessageResponse> getReadMessages(User currentUser, PageRequest pageRequest) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", currentUser.getId());
        context.put("page", pageRequest.getPageNumber());
        context.put("size", pageRequest.getSize());
        context.put("status", MessageStatus.READ);

        centralLogger.logInfo("ПРОЧИТАННЫЕ_СООБЩЕНИЯ_ПОЛУЧЕНИЕ",
                "Получение прочитанных сообщений", context);

        try {
            PageResponse<MessageResponse> response = getMessagesByStatus(currentUser, MessageStatus.READ, pageRequest);

            centralLogger.logInfo("ПРОЧИТАННЫЕ_СООБЩЕНИЯ_ПОЛУЧЕНЫ",
                    "Прочитанные сообщения успешно получены", context);

            return response;
        } catch (Exception e) {
            centralLogger.logError("ПРОЧИТАННЫЕ_СООБЩЕНИЯ_ОШИБКА_ПОЛУЧЕНИЯ",
                    "Ошибка при получении прочитанных сообщений", context, e);
            throw e;
        }
    }

    /**
     * Помечает сообщение как полученное
     *
     * @param messageId идентификатор сообщения
     * @param currentUser текущий пользователь
     * @return ответ с обновленным сообщением
     */
    @Transactional
    @Override
    public MessageResponse markAsReceived(Long messageId, User currentUser) {
        Map<String, Object> context = new HashMap<>();
        context.put("messageId", messageId);
        context.put("userId", currentUser.getId());
        context.put("newStatus", MessageStatus.RECEIVED);

        centralLogger.logInfo("СООБЩЕНИЕ_ПОМЕТКА_ПОЛУЧЕНО",
                "Пометка сообщения как полученного", context);

        try {
            Message message = entityUtils.getMessage(messageId);
            MessageResponse response = updateMessageStatus(messageId, currentUser, MessageStatus.RECEIVED, MessageStatus.SENT);

            messageCacheService.evictConversationCache(currentUser.getId(), message.getSender().getId());

            centralLogger.logInfo("СООБЩЕНИЕ_ПОМЕЧЕНО_ПОЛУЧЕНО",
                    "Сообщение помечено как полученное", context);

            return response;
        } catch (Exception e) {
            centralLogger.logError("СООБЩЕНИЕ_ОШИБКА_ПОМЕТКИ_ПОЛУЧЕНО",
                    "Ошибка при пометке сообщения как полученного", context, e);
            throw e;
        }
    }

    /**
     * Помечает сообщение как прочитанное
     *
     * @param messageId идентификатор сообщения
     * @param currentUser текущий пользователь
     * @return ответ с обновленным сообщением
     */
    @Transactional
    @Override
    public MessageResponse markAsRead(Long messageId, User currentUser) {
        Map<String, Object> context = new HashMap<>();
        context.put("messageId", messageId);
        context.put("userId", currentUser.getId());
        context.put("newStatus", MessageStatus.READ);

        centralLogger.logInfo("СООБЩЕНИЕ_ПОМЕТКА_ПРОЧИТАНО",
                "Пометка сообщения как прочитанного", context);

        try {
            Message message = entityUtils.getMessage(messageId);
            MessageResponse response = updateMessageStatus(messageId, currentUser, MessageStatus.READ, MessageStatus.RECEIVED, MessageStatus.SENT);

            messageCacheService.evictConversationCache(currentUser.getId(), message.getSender().getId());

            centralLogger.logInfo("СООБЩЕНИЕ_ПОМЕЧЕНО_ПРОЧИТАНО",
                    "Сообщение помечено как прочитанное", context);

            return response;
        } catch (Exception e) {
            centralLogger.logError("СООБЩЕНИЕ_ОШИБКА_ПОМЕТКИ_ПРОЧИТАНО",
                    "Ошибка при пометке сообщения как прочитанного", context, e);
            throw e;
        }
    }

    /**
     * Редактирует существующее сообщение
     *
     * @param messageId идентификатор сообщения
     * @param request запрос на редактирование
     * @param currentUser текущий пользователь
     * @return ответ с отредактированным сообщением
     */
    @Transactional
    @Override
    public MessageResponse editMessage(Long messageId, MessageRequest request, User currentUser) {
        Map<String, Object> context = new HashMap<>();
        context.put("messageId", messageId);
        context.put("userId", currentUser.getId());
        context.put("contentUpdated", request.getContent() != null);
        context.put("imageUpdated", request.getImageUrl() != null);

        centralLogger.logInfo("СООБЩЕНИЕ_РЕДАКТИРОВАНИЕ",
                "Редактирование сообщения", context);

        try {
            messageValidator.validateMessageUpdate(request, currentUser);

            Message message = entityUtils.getMessage(messageId);
            messageValidator.validateMessageOwnership(currentUser, message);

            message.setContent(request.getContent());
            message.setImageUrl(request.getImageUrl());
            message.setUpdatedAt(LocalDateTime.now());
            Message updatedMessage = messageRepository.save(message);

            messageCacheService.evictConversationCache(currentUser.getId(), message.getReceiver().getId());

            centralLogger.logInfo("СООБЩЕНИЕ_ОБНОВЛЕНО",
                    "Сообщение успешно обновлено", context);

            return entityMapper.map(updatedMessage, MessageResponse.class);
        } catch (Exception e) {
            centralLogger.logError("СООБЩЕНИЕ_ОШИБКА_РЕДАКТИРОВАНИЯ",
                    "Ошибка при редактировании сообщения", context, e);
            throw e;
        }
    }

    /**
     * Удаляет сообщение
     *
     * @param messageId идентификатор сообщения
     * @param currentUser текущий пользователь
     */
    @Transactional
    @Override
    public void deleteMessage(Long messageId, User currentUser) {
        Map<String, Object> context = new HashMap<>();
        context.put("messageId", messageId);
        context.put("userId", currentUser.getId());

        centralLogger.logInfo("СООБЩЕНИЕ_УДАЛЕНИЕ",
                "Удаление сообщения", context);

        try {
            Message message = entityUtils.getMessage(messageId);
            messageValidator.validateMessageOwnership(currentUser, message);

            Long receiverId = message.getReceiver().getId();
            Long senderId = message.getSender().getId();

            messageRepository.delete(message);

            messageCacheService.evictConversationCache(currentUser.getId(),
                    currentUser.getId().equals(senderId) ? receiverId : senderId);

            centralLogger.logInfo("СООБЩЕНИЕ_УДАЛЕНО",
                    "Сообщение успешно удалено", context);

            notificationEventPublisherService.publishMessageDeleted(this, receiverId, currentUser.getId());
        } catch (Exception e) {
            centralLogger.logError("СООБЩЕНИЕ_ОШИБКА_УДАЛЕНИЯ",
                    "Ошибка при удалении сообщения", context, e);
            throw e;
        }
    }

    /**
     * Получает сообщения по статусу для пользователя
     *
     * @param currentUser текущий пользователь
     * @param status статус сообщений
     * @param pageRequest параметры пагинации
     * @return страница с сообщениями указанного статуса
     */
    private PageResponse<MessageResponse> getMessagesByStatus(User currentUser, MessageStatus status, PageRequest pageRequest) {
        Page<Message> messages = messageRepository.findByReceiverIdAndStatus(currentUser.getId(), status,
                        pageRequest.toPageable())
                .orElse(Page.empty());
        return PageResponse.of(messages.map(
                message -> entityMapper.map(message, MessageResponse.class))
        );
    }

    /**
     * Обновляет статус сообщения
     *
     * @param messageId идентификатор сообщения
     * @param currentUser текущий пользователь
     * @param newStatus новый статус сообщения
     * @param allowedCurrentStatuses разрешенные текущие статусы для изменения
     * @return ответ с обновленным сообщением
     */
    private MessageResponse updateMessageStatus(Long messageId, User currentUser, MessageStatus newStatus, MessageStatus... allowedCurrentStatuses) {
        Message message = entityUtils.getMessage(messageId);
        messageValidator.validateMessageReceiver(currentUser, message);

        if (isStatusAllowed(message.getStatus(), allowedCurrentStatuses)) {
            message.setStatus(newStatus);
            message.setUpdatedAt(LocalDateTime.now());
            messageRepository.save(message);
        }

        return entityMapper.map(message, MessageResponse.class);
    }
}