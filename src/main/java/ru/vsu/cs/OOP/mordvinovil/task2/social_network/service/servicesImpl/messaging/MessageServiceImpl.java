package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.messaging;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.MessageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.MessageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Message;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.MessageStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.notification.NotificationEventPublisherService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.MessageRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.messaging.MessageService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.MessageFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.MessageValidator;

import java.time.LocalDateTime;

import static ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.MessageStatusValidator.isStatusAllowed;

@Primary
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final EntityMapper entityMapper;
    private final MessageFactory messageFactory;
    private final MessageValidator messageValidator;
    private final EntityUtils entityUtils;
    private final NotificationEventPublisherService notificationEventPublisherService;

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
        messageValidator.validateMessageCreation(request, currentUser);

        User receiver = entityUtils.getUser(request.getReceiverUserId());
        Message message = messageFactory.createMessage(currentUser, receiver, request);
        Message savedMessage = messageRepository.save(message);

        notificationEventPublisherService.publishMessageReceived(this, request.getReceiverUserId(),
                currentUser.getId(), message.getContent());

        return entityMapper.map(savedMessage, MessageResponse.class);
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
        Message message = entityUtils.getMessage(messageId);
        messageValidator.validateMessageAccess(currentUser, message);
        return entityMapper.map(message, MessageResponse.class);
    }

    /**
     * Получает переписку между текущим пользователем и другим пользователем
     *
     * @param otherUserId идентификатор другого пользователя
     * @param currentUser текущий пользователь
     * @param pageRequest параметры пагинации
     * @return страница с сообщениями переписки
     */
    @Override
    public PageResponse<MessageResponse> getConversation(Long otherUserId, User currentUser, PageRequest pageRequest) {
        Long id = entityUtils.getUser(otherUserId).getId();
        Page<Message> messages = messageRepository.findMessagesBetweenUsers(currentUser.getId(),
                        id, pageRequest.toPageable())
                .orElse(Page.empty());

        return PageResponse.of(messages.map(
                message -> entityMapper.map(message, MessageResponse.class))
        );
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
        Page<Message> messages = messageRepository.findBySenderId(currentUser.getId(),
                        pageRequest.toPageable())
                .orElse(Page.empty());
        return PageResponse.of(messages.map(
                message -> entityMapper.map(message, MessageResponse.class))
        );
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
        return getMessagesByStatus(currentUser, MessageStatus.RECEIVED, pageRequest);
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
        return getMessagesByStatus(currentUser, MessageStatus.READ, pageRequest);
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
        return updateMessageStatus(messageId, currentUser, MessageStatus.RECEIVED, MessageStatus.SENT);
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
        return updateMessageStatus(messageId, currentUser, MessageStatus.READ, MessageStatus.RECEIVED, MessageStatus.SENT);
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
        messageValidator.validateMessageUpdate(request, currentUser);

        Message message = entityUtils.getMessage(messageId);
        messageValidator.validateMessageOwnership(currentUser, message);

        message.setContent(request.getContent());
        message.setImageUrl(request.getImageUrl());
        message.setUpdatedAt(LocalDateTime.now());
        Message updatedMessage = messageRepository.save(message);

        return entityMapper.map(updatedMessage, MessageResponse.class);
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
        Message message = entityUtils.getMessage(messageId);
        messageValidator.validateMessageOwnership(currentUser, message);
        messageRepository.delete(message);

        notificationEventPublisherService.publishMessageDeleted(this, message.getReceiver().getId(), currentUser.getId());
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