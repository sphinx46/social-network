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
import ru.cs.vsu.social_network.messaging_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.ConversationDetailsResponse;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.ConversationResponse;
import ru.cs.vsu.social_network.messaging_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.messaging_service.entity.Conversation;
import ru.cs.vsu.social_network.messaging_service.exception.conversation.ConversationNotFoundException;
import ru.cs.vsu.social_network.messaging_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.messaging_service.provider.ConversationEntityProvider;
import ru.cs.vsu.social_network.messaging_service.repository.ConversationRepository;
import ru.cs.vsu.social_network.messaging_service.service.aggregator.ConversationDetailsAggregator;
import ru.cs.vsu.social_network.messaging_service.service.cache.MessagingCacheEventPublisherService;
import ru.cs.vsu.social_network.messaging_service.service.serviceImpl.ConversationServiceImpl;
import ru.cs.vsu.social_network.messaging_service.utils.MessageConstants;
import ru.cs.vsu.social_network.messaging_service.utils.TestDataFactory;
import ru.cs.vsu.social_network.messaging_service.utils.factory.messaging.ConversationFactory;
import ru.cs.vsu.social_network.messaging_service.validation.ConversationValidator;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationServiceImplTest {

    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private ConversationEntityProvider conversationEntityProvider;
    @Mock
    private ConversationValidator conversationValidator;
    @Mock
    private ConversationDetailsAggregator conversationDetailsAggregator;
    @Mock
    private EntityMapper entityMapper;
    @Mock
    private ConversationFactory conversationFactory;
    @Mock
    private MessagingCacheEventPublisherService cacheEventPublisherService;

    @InjectMocks
    private ConversationServiceImpl conversationService;

    @Test
    @DisplayName("Создание или получение беседы - создание новой")
    void createOrGetConversation_whenNotExists_shouldCreateNew() {
        UUID user1Id = TestDataFactory.TEST_USER_ID;
        UUID user2Id = TestDataFactory.TEST_USER2_ID;
        Conversation newConversation = TestDataFactory.createConversationEntity();
        Conversation savedConversation = TestDataFactory.createConversationEntity();
        ConversationResponse expectedResponse = TestDataFactory.createConversationResponse();

        doNothing().when(conversationValidator).validateUsersNotSame(user1Id, user2Id);
        when(conversationEntityProvider.getConversationBetweenUsers(user1Id, user2Id))
                .thenReturn(Optional.empty());
        when(conversationFactory.buildNewConversation(user1Id, user2Id)).thenReturn(newConversation);
        when(conversationRepository.save(newConversation)).thenReturn(savedConversation);
        when(entityMapper.map(savedConversation, ConversationResponse.class)).thenReturn(expectedResponse);
        doNothing().when(cacheEventPublisherService).publishConversationCreated(any(), any(), any(), any(), any());

        ConversationResponse actual = conversationService.createOrGetConversation(user1Id, user2Id);

        assertNotNull(actual);
        assertEquals(expectedResponse, actual);
        verify(conversationValidator).validateUsersNotSame(user1Id, user2Id);
        verify(conversationRepository).save(newConversation);
        verify(cacheEventPublisherService).publishConversationCreated(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Создание или получение беседы - получение существующей")
    void createOrGetConversation_whenExists_shouldReturnExisting() {
        UUID user1Id = TestDataFactory.TEST_USER_ID;
        UUID user2Id = TestDataFactory.TEST_USER2_ID;
        Conversation existingConversation = TestDataFactory.createConversationEntity();
        ConversationResponse expectedResponse = TestDataFactory.createConversationResponse();

        doNothing().when(conversationValidator).validateUsersNotSame(user1Id, user2Id);
        when(conversationEntityProvider.getConversationBetweenUsers(user1Id, user2Id))
                .thenReturn(Optional.of(existingConversation));
        when(entityMapper.map(existingConversation, ConversationResponse.class)).thenReturn(expectedResponse);

        ConversationResponse actual = conversationService.createOrGetConversation(user1Id, user2Id);

        assertNotNull(actual);
        assertEquals(expectedResponse, actual);
        verify(conversationValidator).validateUsersNotSame(user1Id, user2Id);
        verify(conversationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Получение беседы по ID - успешно")
    void getConversationById_whenExists_shouldReturnResponse() {
        UUID conversationId = TestDataFactory.TEST_CONVERSATION_ID;
        Conversation conversation = TestDataFactory.createConversationEntity();
        ConversationResponse expectedResponse = TestDataFactory.createConversationResponse();

        when(conversationEntityProvider.getById(conversationId)).thenReturn(conversation);
        when(entityMapper.map(conversation, ConversationResponse.class)).thenReturn(expectedResponse);

        ConversationResponse actual = conversationService.getConversationById(conversationId);

        assertEquals(expectedResponse, actual);
        verify(conversationEntityProvider).getById(conversationId);
    }

    @Test
    @DisplayName("Получение беседы между пользователями - успешно")
    void getConversationBetweenUsers_whenExists_shouldReturnResponse() {
        UUID user1Id = TestDataFactory.TEST_USER_ID;
        UUID user2Id = TestDataFactory.TEST_USER2_ID;
        Conversation conversation = TestDataFactory.createConversationEntity();
        ConversationResponse expectedResponse = TestDataFactory.createConversationResponse();

        when(conversationEntityProvider.getConversationBetweenUsers(user1Id, user2Id))
                .thenReturn(Optional.of(conversation));
        when(entityMapper.map(conversation, ConversationResponse.class)).thenReturn(expectedResponse);

        ConversationResponse actual = conversationService.getConversationBetweenUsers(user1Id, user2Id);

        assertEquals(expectedResponse, actual);
        verify(conversationEntityProvider).getConversationBetweenUsers(user1Id, user2Id);
    }

    @Test
    @DisplayName("Получение беседы между пользователями - не найдена")
    void getConversationBetweenUsers_whenNotExists_shouldThrowException() {
        UUID user1Id = TestDataFactory.TEST_USER_ID;
        UUID user2Id = TestDataFactory.TEST_USER2_ID;

        when(conversationEntityProvider.getConversationBetweenUsers(user1Id, user2Id))
                .thenReturn(Optional.empty());

        assertThrows(ConversationNotFoundException.class,
                () -> conversationService.getConversationBetweenUsers(user1Id, user2Id));

        verify(conversationEntityProvider).getConversationBetweenUsers(user1Id, user2Id);
    }

    @Test
    @DisplayName("Получение бесед пользователя - успешно")
    void getUserConversations_whenExists_shouldReturnPageResponse() {
        UUID userId = TestDataFactory.TEST_USER_ID;
        PageRequest pageRequest = TestDataFactory.createPageRequest();
        Conversation conversation = TestDataFactory.createConversationEntity();
        List<Conversation> conversations = List.of(conversation);
        Page<Conversation> page = new PageImpl<>(conversations, pageRequest.toPageable(), conversations.size());
        ConversationResponse conversationResponse = TestDataFactory.createConversationResponse();
        PageResponse<ConversationResponse> expectedResponse = TestDataFactory.createPageResponse(List.of(conversationResponse));

        when(conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId, pageRequest.toPageable()))
                .thenReturn(page);
        when(entityMapper.map(conversation, ConversationResponse.class)).thenReturn(conversationResponse);

        PageResponse<ConversationResponse> actual = conversationService.getUserConversations(userId, pageRequest);

        assertNotNull(actual);
        assertEquals(1, actual.getContent().size());
        verify(conversationRepository).findByUserIdOrderByUpdatedAtDesc(userId, pageRequest.toPageable());
    }

    @Test
    @DisplayName("Получение беседы с сообщениями - успешно")
    void getConversationWithMessages_whenExists_shouldReturnResponse() {
        UUID conversationId = TestDataFactory.TEST_CONVERSATION_ID;
        int messagesLimit = 10;
        Conversation conversation = TestDataFactory.createConversationEntity();
        ConversationDetailsResponse expectedResponse = TestDataFactory.createConversationDetailsResponse();

        when(conversationEntityProvider.getById(conversationId)).thenReturn(conversation);
        when(conversationDetailsAggregator.aggregateConversationDetails(conversation, true, messagesLimit))
                .thenReturn(expectedResponse);

        ConversationDetailsResponse actual = conversationService.getConversationWithMessages(conversationId, messagesLimit);

        assertEquals(expectedResponse, actual);
        verify(conversationEntityProvider).getById(conversationId);
        verify(conversationDetailsAggregator).aggregateConversationDetails(conversation, true, messagesLimit);
    }

    @Test
    @DisplayName("Получение беседы между пользователями с сообщениями - успешно")
    void getConversationBetweenUsersWithMessages_whenExists_shouldReturnPageResponse() {
        UUID user1Id = TestDataFactory.TEST_USER_ID;
        UUID user2Id = TestDataFactory.TEST_USER2_ID;
        PageRequest pageRequest = TestDataFactory.createPageRequest();
        Conversation conversation = TestDataFactory.createConversationEntity();
        ConversationDetailsResponse detailsResponse = TestDataFactory.createConversationDetailsResponse();
        Page<ConversationDetailsResponse> detailsPage = new PageImpl<>(
                List.of(detailsResponse), pageRequest.toPageable(), 1
        );
        PageResponse<ConversationDetailsResponse> expectedResponse = TestDataFactory.createPageResponse(List.of(detailsResponse));

        when(conversationEntityProvider.getConversationBetweenUsers(user1Id, user2Id))
                .thenReturn(Optional.of(conversation));
        when(conversationDetailsAggregator.aggregateConversationsPage(any(Page.class), eq(true), eq(pageRequest.getSize())))
                .thenReturn(detailsPage);

        PageResponse<ConversationDetailsResponse> actual = conversationService
                .getConversationBetweenUsersWithMessages(user1Id, user2Id, pageRequest);

        assertNotNull(actual);
        assertEquals(1, actual.getContent().size());
        verify(conversationEntityProvider).getConversationBetweenUsers(user1Id, user2Id);
    }

    @Test
    @DisplayName("Удаление беседы - успешно")
    void deleteConversation_whenUserIsOwner_shouldDelete() {
        UUID userId = TestDataFactory.TEST_USER_ID;
        UUID conversationId = TestDataFactory.TEST_CONVERSATION_ID;
        Conversation conversation = TestDataFactory.createConversationEntity();

        doNothing().when(conversationValidator).validateOwnership(userId, conversationId);
        when(conversationEntityProvider.getById(conversationId)).thenReturn(conversation);
        when(conversationRepository.deleteAllByConversationId(conversationId)).thenReturn(5);

        conversationService.deleteConversation(userId, conversationId);

        verify(conversationValidator).validateOwnership(userId, conversationId);
        verify(conversationRepository).deleteAllByConversationId(conversationId);
        verify(conversationRepository).delete(conversation);
    }

    @Test
    @DisplayName("Удаление беседы - доступ запрещен")
    void deleteConversation_whenUserNotOwner_shouldThrowException() {
        UUID anotherUserId = UUID.randomUUID();
        UUID conversationId = TestDataFactory.TEST_CONVERSATION_ID;

        doThrow(new AccessDeniedException(MessageConstants.ACCESS_DENIED))
                .when(conversationValidator).validateOwnership(anotherUserId, conversationId);

        assertThrows(AccessDeniedException.class,
                () -> conversationService.deleteConversation(anotherUserId, conversationId));

        verify(conversationValidator).validateOwnership(anotherUserId, conversationId);
        verify(conversationRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Проверка существования беседы между пользователями - существует")
    void existsConversationBetweenUsers_whenExists_shouldReturnTrue() {
        UUID user1Id = TestDataFactory.TEST_USER_ID;
        UUID user2Id = TestDataFactory.TEST_USER2_ID;

        when(conversationEntityProvider.existsConversationBetweenUsers(user1Id, user2Id)).thenReturn(true);

        boolean actual = conversationService.existsConversationBetweenUsers(user1Id, user2Id);

        assertTrue(actual);
        verify(conversationEntityProvider).existsConversationBetweenUsers(user1Id, user2Id);
    }

    @Test
    @DisplayName("Проверка существования беседы между пользователями - не существует")
    void existsConversationBetweenUsers_whenNotExists_shouldReturnFalse() {
        UUID user1Id = TestDataFactory.TEST_USER_ID;
        UUID user2Id = TestDataFactory.TEST_USER2_ID;

        when(conversationEntityProvider.existsConversationBetweenUsers(user1Id, user2Id)).thenReturn(false);

        boolean actual = conversationService.existsConversationBetweenUsers(user1Id, user2Id);

        assertFalse(actual);
        verify(conversationEntityProvider).existsConversationBetweenUsers(user1Id, user2Id);
    }
}