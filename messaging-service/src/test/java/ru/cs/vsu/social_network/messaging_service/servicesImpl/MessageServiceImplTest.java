package ru.cs.vsu.social_network.messaging_service.servicesImpl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.access.AccessDeniedException;
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
import ru.cs.vsu.social_network.messaging_service.service.batch.BatchMessageService;
import ru.cs.vsu.social_network.messaging_service.service.cache.MessagingCacheEventPublisherService;
import ru.cs.vsu.social_network.messaging_service.service.serviceImpl.MessageServiceImpl;
import ru.cs.vsu.social_network.messaging_service.utils.MessageConstants;
import ru.cs.vsu.social_network.messaging_service.utils.TestDataFactory;
import ru.cs.vsu.social_network.messaging_service.utils.factory.messaging.MessageFactory;
import ru.cs.vsu.social_network.messaging_service.validation.ConversationValidator;
import ru.cs.vsu.social_network.messaging_service.validation.MessageValidator;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    private MessageRepository messageRepository;
    @Mock
    private MessageEntityProvider messageEntityProvider;
    @Mock
    private MessageValidator messageValidator;
    @Mock
    private EntityMapper entityMapper;
    @Mock
    private MessageFactory messageFactory;
    @Mock
    private BatchMessageService batchMessageService;
    @Mock
    private ConversationValidator conversationValidator;
    @Mock
    private ConversationEntityProvider conversationEntityProvider;
    @Mock
    private MessagingCacheEventPublisherService cacheEventPublisherService;

    @InjectMocks
    private MessageServiceImpl messageService;

    @Test
    @DisplayName("Создание сообщения - успешно")
    void createMessage_whenRequestIsValid_shouldReturnResponse() {
        UUID userId = TestDataFactory.TEST_USER_ID;
        UUID receiverId = TestDataFactory.TEST_USER2_ID;
        MessageCreateRequest request = TestDataFactory.createMessageCreateRequest(receiverId, "Test message");
        Message message = TestDataFactory.createMessageEntity();
        Message savedMessage = TestDataFactory.createMessageEntity();
        MessageResponse expectedResponse = TestDataFactory.createMessageResponse();

        when(messageFactory.create(userId, request)).thenReturn(message);
        when(messageRepository.save(message)).thenReturn(savedMessage);
        when(entityMapper.map(savedMessage, MessageResponse.class)).thenReturn(expectedResponse);
        doNothing().when(cacheEventPublisherService).publishMessageCreated(any(), any(), any(), any(), any(), any());

        MessageResponse actual = messageService.createMessage(userId, request);

        assertNotNull(actual);
        assertEquals(expectedResponse, actual);
        verify(messageRepository).save(message);
        verify(cacheEventPublisherService).publishMessageCreated(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Редактирование сообщения - успешно")
    void editMessage_whenRequestIsValid_shouldReturnResponse() {
        UUID userId = TestDataFactory.TEST_USER_ID;
        MessageEditRequest request = TestDataFactory.createMessageEditRequest(TestDataFactory.TEST_MESSAGE_ID, "Updated content");
        Message message = TestDataFactory.createMessageEntity();
        Message updatedMessage = TestDataFactory.createMessageEntity();
        MessageResponse expectedResponse = TestDataFactory.createMessageResponse();

        doNothing().when(messageValidator).validateOwnership(userId, TestDataFactory.TEST_MESSAGE_ID);
        when(messageEntityProvider.getById(TestDataFactory.TEST_MESSAGE_ID)).thenReturn(message);
        when(messageRepository.save(message)).thenReturn(updatedMessage);
        when(entityMapper.map(updatedMessage, MessageResponse.class)).thenReturn(expectedResponse);
        doNothing().when(cacheEventPublisherService).publishMessageUpdated(any(), any(), any(), any());

        MessageResponse actual = messageService.editMessage(userId, request);

        assertEquals("Updated content", message.getContent());
        assertEquals(expectedResponse, actual);
        verify(messageValidator).validateOwnership(userId, TestDataFactory.TEST_MESSAGE_ID);
    }

    @Test
    @DisplayName("Редактирование сообщения - доступ запрещен")
    void editMessage_whenUserNotOwner_shouldThrowException() {
        UUID anotherUserId = UUID.randomUUID();
        MessageEditRequest request = TestDataFactory.createMessageEditRequest();

        doThrow(new AccessDeniedException(MessageConstants.ACCESS_DENIED))
                .when(messageValidator).validateOwnership(anotherUserId, TestDataFactory.TEST_MESSAGE_ID);

        assertThrows(AccessDeniedException.class, () -> messageService.editMessage(anotherUserId, request));

        verify(messageValidator).validateOwnership(anotherUserId, TestDataFactory.TEST_MESSAGE_ID);
        verify(messageRepository, never()).save(any());
    }

    @Test
    @DisplayName("Удаление сообщения - успешно")
    void deleteMessage_whenRequestIsValid_shouldReturnResponse() {
        UUID userId = TestDataFactory.TEST_USER_ID;
        MessageDeleteRequest request = TestDataFactory.createMessageDeleteRequest();
        Message message = TestDataFactory.createMessageEntity();
        MessageResponse expectedResponse = TestDataFactory.createMessageResponse();

        doNothing().when(messageValidator).validateOwnership(userId, TestDataFactory.TEST_MESSAGE_ID);
        when(messageEntityProvider.getById(TestDataFactory.TEST_MESSAGE_ID)).thenReturn(message);
        when(entityMapper.map(message, MessageResponse.class)).thenReturn(expectedResponse);
        doNothing().when(cacheEventPublisherService).publishMessageDeleted(any(), any(), any(), any());

        MessageResponse actual = messageService.deleteMessage(userId, request);

        assertEquals(expectedResponse, actual);
        verify(messageValidator).validateOwnership(userId, TestDataFactory.TEST_MESSAGE_ID);
        verify(messageRepository).delete(message);
        verify(cacheEventPublisherService).publishMessageDeleted(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Загрузка изображения сообщения - успешно")
    void uploadImage_whenRequestIsValid_shouldReturnResponse() {
        UUID userId = TestDataFactory.TEST_USER_ID;
        String imageUrl = "http://example.com/image.jpg";
        MessageUploadImageRequest request = TestDataFactory.createMessageUploadImageRequest(TestDataFactory.TEST_MESSAGE_ID, imageUrl);
        Message message = TestDataFactory.createMessageEntity();
        Message updatedMessage = TestDataFactory.createMessageEntity();
        MessageResponse expectedResponse = TestDataFactory.createMessageResponse();

        doNothing().when(messageValidator).validateOwnership(userId, TestDataFactory.TEST_MESSAGE_ID);
        when(messageEntityProvider.getById(TestDataFactory.TEST_MESSAGE_ID)).thenReturn(message);
        when(messageRepository.save(message)).thenReturn(updatedMessage);
        when(entityMapper.map(updatedMessage, MessageResponse.class)).thenReturn(expectedResponse);
        doNothing().when(cacheEventPublisherService).publishMessageImageUploaded(any(), any(), any(), any());

        MessageResponse actual = messageService.uploadImage(userId, request);

        assertEquals(imageUrl, message.getImageUrl());
        assertEquals(expectedResponse, actual);
        verify(messageValidator).validateOwnership(userId, TestDataFactory.TEST_MESSAGE_ID);
    }

    @Test
    @DisplayName("Загрузка изображения сообщения - пустой URL")
    void uploadImage_whenImageUrlEmpty_shouldThrowException() {
        UUID userId = TestDataFactory.TEST_USER_ID;
        MessageUploadImageRequest request = TestDataFactory.createMessageUploadImageRequest(TestDataFactory.TEST_MESSAGE_ID, " ");
        Message message = TestDataFactory.createMessageEntity();

        doNothing().when(messageValidator).validateOwnership(userId, TestDataFactory.TEST_MESSAGE_ID);
        when(messageEntityProvider.getById(TestDataFactory.TEST_MESSAGE_ID)).thenReturn(message);

        assertThrows(MessageUploadImageException.class, () -> messageService.uploadImage(userId, request));

        verify(messageValidator).validateOwnership(userId, TestDataFactory.TEST_MESSAGE_ID);
        verify(messageRepository, never()).save(any());
    }

    @Test
    @DisplayName("Удаление изображения сообщения - успешно")
    void removeImage_whenRequestIsValid_shouldReturnResponse() {
        UUID userId = TestDataFactory.TEST_USER_ID;
        MessageRemoveImageRequest request = TestDataFactory.createMessageRemoveImageRequest();
        Message message = TestDataFactory.createMessageEntityWithImage();
        Message updatedMessage = TestDataFactory.createMessageEntity();
        MessageResponse expectedResponse = TestDataFactory.createMessageResponse();

        doNothing().when(messageValidator).validateOwnership(userId, TestDataFactory.TEST_MESSAGE_ID);
        when(messageEntityProvider.getById(TestDataFactory.TEST_MESSAGE_ID)).thenReturn(message);
        when(messageRepository.save(message)).thenReturn(updatedMessage);
        when(entityMapper.map(updatedMessage, MessageResponse.class)).thenReturn(expectedResponse);
        doNothing().when(cacheEventPublisherService).publishMessageUpdated(any(), any(), any(), any());

        MessageResponse actual = messageService.removeImage(userId, request);

        assertNull(message.getImageUrl());
        assertEquals(expectedResponse, actual);
        verify(messageValidator).validateOwnership(userId, TestDataFactory.TEST_MESSAGE_ID);
    }

    @Test
    @DisplayName("Получение сообщения по ID - успешно")
    void getMessageById_whenMessageExists_shouldReturnResponse() {
        UUID messageId = TestDataFactory.TEST_MESSAGE_ID;
        Message message = TestDataFactory.createMessageEntity();
        MessageResponse expectedResponse = TestDataFactory.createMessageResponse();

        when(messageEntityProvider.getById(messageId)).thenReturn(message);
        when(entityMapper.map(message, MessageResponse.class)).thenReturn(expectedResponse);

        MessageResponse actual = messageService.getMessageById(messageId);

        assertEquals(expectedResponse, actual);
        verify(messageEntityProvider).getById(messageId);
    }

    @Test
    @DisplayName("Отметка сообщений как доставленных - успешно")
    void markMessagesAsDelivered_whenMessagesExist_shouldReturnCount() {
        UUID receiverId = TestDataFactory.TEST_USER_ID;
        List<UUID> messageIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        int expectedCount = 2;

        when(batchMessageService.batchUpdateMessagesStatus(receiverId, messageIds, MessageStatus.SENT, MessageStatus.DELIVERED))
                .thenReturn(expectedCount);

        int actual = messageService.markMessagesAsDelivered(receiverId, messageIds);

        assertEquals(expectedCount, actual);
        verify(batchMessageService).batchUpdateMessagesStatus(receiverId, messageIds, MessageStatus.SENT, MessageStatus.DELIVERED);
    }

    @Test
    @DisplayName("Отметка беседы как прочитанной - успешно")
    void markConversationAsRead_whenConversationExists_shouldReturnCount() {
        UUID userId = TestDataFactory.TEST_USER_ID;
        UUID conversationId = TestDataFactory.TEST_CONVERSATION_ID;
        int expectedCount = 5;

        doNothing().when(conversationValidator).validateAccessToChat(conversationId, userId);
        when(batchMessageService.batchMarkConversationAsRead(userId, conversationId)).thenReturn(expectedCount);
        when(conversationEntityProvider.getById(conversationId)).thenReturn(TestDataFactory.createConversationEntity());
        doNothing().when(cacheEventPublisherService).publishMessagesRead(any(), any(), any(), any());

        int actual = messageService.markConversationAsRead(userId, conversationId);

        assertEquals(expectedCount, actual);
        verify(conversationValidator).validateAccessToChat(conversationId, userId);
        verify(batchMessageService).batchMarkConversationAsRead(userId, conversationId);
    }

    @Test
    @DisplayName("Получение количества непрочитанных сообщений - успешно")
    void getUnreadMessagesCount_whenMessagesExist_shouldReturnCount() {
        UUID receiverId = TestDataFactory.TEST_USER_ID;
        Long sentCount = 3L;
        Long deliveredCount = 2L;
        Long expectedTotal = 5L;

        when(batchMessageService.getUnreadMessagesCountByUser(receiverId, MessageStatus.SENT)).thenReturn(sentCount);
        when(batchMessageService.getUnreadMessagesCountByUser(receiverId, MessageStatus.DELIVERED)).thenReturn(deliveredCount);

        Long actual = messageService.getUnreadMessagesCount(receiverId);

        assertEquals(expectedTotal, actual);
        verify(batchMessageService).getUnreadMessagesCountByUser(receiverId, MessageStatus.SENT);
        verify(batchMessageService).getUnreadMessagesCountByUser(receiverId, MessageStatus.DELIVERED);
    }

    @Test
    @DisplayName("Получение сообщений между пользователями - успешно")
    void getMessagesBetweenUsers_whenMessagesExist_shouldReturnPageResponse() {
        UUID senderId = TestDataFactory.TEST_USER_ID;
        UUID receiverId = TestDataFactory.TEST_USER2_ID;
        PageRequest pageRequest = TestDataFactory.createPageRequest();
        Message message = TestDataFactory.createMessageEntity();
        List<Message> messages = List.of(message);
        Page<Message> page = new PageImpl<>(messages, pageRequest.toPageable(), messages.size());
        MessageResponse messageResponse = TestDataFactory.createMessageResponse();
        PageResponse<MessageResponse> expectedResponse = TestDataFactory.createPageResponse(List.of(messageResponse));

        when(messageRepository.findMessagesBetweenUsers(senderId, receiverId, pageRequest.toPageable())).thenReturn(page);
        when(entityMapper.map(message, MessageResponse.class)).thenReturn(messageResponse);

        PageResponse<MessageResponse> actual = messageService.getMessagesBetweenUsers(senderId, receiverId, pageRequest);

        assertNotNull(actual);
        assertEquals(1, actual.getContent().size());
        verify(messageRepository).findMessagesBetweenUsers(senderId, receiverId, pageRequest.toPageable());
    }

    @Test
    @DisplayName("Получение сообщений отправителя - успешно")
    void getMessagesBySender_whenMessagesExist_shouldReturnPageResponse() {
        UUID userId = TestDataFactory.TEST_USER_ID;
        PageRequest pageRequest = TestDataFactory.createPageRequest();
        Message message = TestDataFactory.createMessageEntity();
        List<Message> messages = List.of(message);
        Page<Message> page = new PageImpl<>(messages, pageRequest.toPageable(), messages.size());
        MessageResponse messageResponse = TestDataFactory.createMessageResponse();
        PageResponse<MessageResponse> expectedResponse = TestDataFactory.createPageResponse(List.of(messageResponse));

        when(messageRepository.findBySenderIdOrderByCreatedAtDesc(userId, pageRequest.toPageable())).thenReturn(page);
        when(entityMapper.map(message, MessageResponse.class)).thenReturn(messageResponse);

        PageResponse<MessageResponse> actual = messageService.getMessagesBySender(userId, pageRequest);

        assertNotNull(actual);
        assertEquals(1, actual.getContent().size());
        verify(messageRepository).findBySenderIdOrderByCreatedAtDesc(userId, pageRequest.toPageable());
    }
}