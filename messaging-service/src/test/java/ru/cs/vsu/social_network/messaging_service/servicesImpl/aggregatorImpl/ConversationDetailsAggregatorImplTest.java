package ru.cs.vsu.social_network.messaging_service.servicesImpl.aggregatorImpl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.ConversationDetailsResponse;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.MessageResponse;
import ru.cs.vsu.social_network.messaging_service.entity.Conversation;
import ru.cs.vsu.social_network.messaging_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.messaging_service.service.aggregator.aggregatorImpl.ConversationDetailsAggregatorImpl;
import ru.cs.vsu.social_network.messaging_service.service.batch.BatchMessageService;
import ru.cs.vsu.social_network.messaging_service.utils.TestDataFactory;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationDetailsAggregatorImplTest {

    @Mock
    private EntityMapper mapper;

    @Mock
    private BatchMessageService batchMessageService;

    @InjectMocks
    private ConversationDetailsAggregatorImpl conversationDetailsAggregator;

    @Test
    @DisplayName("Агрегация деталей беседы - с сообщениями")
    void aggregateConversationDetails_whenIncludeMessages_shouldReturnResponseWithMessages() {
        UUID conversationId = TestDataFactory.TEST_CONVERSATION_ID;
        Conversation conversation = TestDataFactory.createConversationEntity();
        int messagesLimit = 10;
        List<MessageResponse> mockMessages = TestDataFactory.createMessageList(3).stream()
                .map(message -> TestDataFactory.createMessageResponse(
                        message.getId(),
                        message.getSenderId(),
                        message.getReceiverId(),
                        message.getConversation().getId()))
                .toList();

        ConversationDetailsResponse baseResponse = TestDataFactory.createConversationDetailsResponse();

        when(mapper.map(conversation, ConversationDetailsResponse.class)).thenReturn(baseResponse);
        when(batchMessageService.getMessagesByConversation(conversationId, 0, messagesLimit))
                .thenReturn(mockMessages);

        ConversationDetailsResponse result = conversationDetailsAggregator.aggregateConversationDetails(
                conversation, true, messagesLimit);

        assertNotNull(result);
        assertEquals(conversationId, result.getConversationId());
        assertEquals(3L, result.getMessagesCount());
        assertEquals(3, result.getMessages().size());
        assertNotNull(result.getLastMessageId());

        verify(mapper).map(conversation, ConversationDetailsResponse.class);
        verify(batchMessageService).getMessagesByConversation(conversationId, 0, messagesLimit);
    }

    @Test
    @DisplayName("Агрегация деталей беседы - без сообщений")
    void aggregateConversationDetails_whenExcludeMessages_shouldReturnResponseWithoutMessages() {
        UUID conversationId = TestDataFactory.TEST_CONVERSATION_ID;
        Conversation conversation = TestDataFactory.createConversationEntity();

        ConversationDetailsResponse baseResponse = TestDataFactory.createConversationDetailsResponse();

        when(mapper.map(conversation, ConversationDetailsResponse.class)).thenReturn(baseResponse);

        ConversationDetailsResponse result = conversationDetailsAggregator.aggregateConversationDetails(
                conversation, false, 10);

        assertNotNull(result);
        assertEquals(conversationId, result.getConversationId());
        assertEquals(0L, result.getMessagesCount());
        assertTrue(result.getMessages().isEmpty());
        assertNull(result.getLastMessageId());

        verify(mapper).map(conversation, ConversationDetailsResponse.class);
        verify(batchMessageService, never()).getMessagesByConversation(any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Агрегация деталей беседы - нулевой лимит сообщений")
    void aggregateConversationDetails_whenZeroMessagesLimit_shouldReturnResponseWithoutMessages() {
        UUID conversationId = TestDataFactory.TEST_CONVERSATION_ID;
        Conversation conversation = TestDataFactory.createConversationEntity();

        ConversationDetailsResponse baseResponse = TestDataFactory.createConversationDetailsResponse();

        when(mapper.map(conversation, ConversationDetailsResponse.class)).thenReturn(baseResponse);

        ConversationDetailsResponse result = conversationDetailsAggregator.aggregateConversationDetails(
                conversation, true, 0);

        assertNotNull(result);
        assertEquals(conversationId, result.getConversationId());
        assertEquals(0L, result.getMessagesCount());
        assertTrue(result.getMessages().isEmpty());
        assertNull(result.getLastMessageId());

        verify(mapper).map(conversation, ConversationDetailsResponse.class);
        verify(batchMessageService, never()).getMessagesByConversation(any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Агрегация деталей беседы - лимит превышает максимальный")
    void aggregateConversationDetails_whenLimitExceedsMax_shouldUseMaxLimit() {
        UUID conversationId = TestDataFactory.TEST_CONVERSATION_ID;
        Conversation conversation = TestDataFactory.createConversationEntity();
        int messagesLimit = 100;
        int effectiveLimit = 50;
        List<MessageResponse> mockMessages = Collections.emptyList();

        ConversationDetailsResponse baseResponse = TestDataFactory.createConversationDetailsResponse();

        when(mapper.map(conversation, ConversationDetailsResponse.class)).thenReturn(baseResponse);
        when(batchMessageService.getMessagesByConversation(conversationId, 0, effectiveLimit))
                .thenReturn(mockMessages);

        ConversationDetailsResponse result = conversationDetailsAggregator.aggregateConversationDetails(
                conversation, true, messagesLimit);

        assertNotNull(result);
        verify(batchMessageService).getMessagesByConversation(conversationId, 0, effectiveLimit);
    }

    @Test
    @DisplayName("Агрегация деталей беседы - пустой список сообщений")
    void aggregateConversationDetails_whenEmptyMessagesList_shouldReturnResponseWithEmptyMessages() {
        UUID conversationId = TestDataFactory.TEST_CONVERSATION_ID;
        Conversation conversation = TestDataFactory.createConversationEntity();
        int messagesLimit = 10;

        ConversationDetailsResponse baseResponse = TestDataFactory.createConversationDetailsResponse();

        when(mapper.map(conversation, ConversationDetailsResponse.class)).thenReturn(baseResponse);
        when(batchMessageService.getMessagesByConversation(conversationId, 0, messagesLimit))
                .thenReturn(Collections.emptyList());

        ConversationDetailsResponse result = conversationDetailsAggregator.aggregateConversationDetails(
                conversation, true, messagesLimit);

        assertNotNull(result);
        assertEquals(conversationId, result.getConversationId());
        assertEquals(0L, result.getMessagesCount());
        assertTrue(result.getMessages().isEmpty());
        assertNull(result.getLastMessageId());

        verify(mapper).map(conversation, ConversationDetailsResponse.class);
        verify(batchMessageService).getMessagesByConversation(conversationId, 0, messagesLimit);
    }

    @Test
    @DisplayName("Агрегация страницы бесед - без сообщений")
    void aggregateConversationsPage_whenExcludeMessages_shouldReturnPageWithoutMessages() {
        List<Conversation> conversations = TestDataFactory.createConversationListWithMessages(2);
        Page<Conversation> conversationsPage = new PageImpl<>(conversations);

        conversations.forEach(conversation -> {
            ConversationDetailsResponse response = TestDataFactory.createConversationDetailsResponse(
                    conversation.getId(), conversation.getUser1Id(), conversation.getUser2Id());
            when(mapper.map(conversation, ConversationDetailsResponse.class)).thenReturn(response);
        });

        Page<ConversationDetailsResponse> result = conversationDetailsAggregator.aggregateConversationsPage(
                conversationsPage, false, 10);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());

        result.getContent().forEach(response -> {
            assertTrue(response.getMessages().isEmpty());
            assertNull(response.getLastMessageId());
            assertEquals(0L, response.getMessagesCount());
        });

        verify(batchMessageService, never()).getRecentMessagesForConversations(any(), anyInt());
    }

    @Test
    @DisplayName("Агрегация страницы бесед - пустая страница")
    void aggregateConversationsPage_whenEmptyPage_shouldReturnEmptyPage() {
        Page<Conversation> emptyPage = Page.empty();

        Page<ConversationDetailsResponse> result = conversationDetailsAggregator.aggregateConversationsPage(
                emptyPage, true, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(mapper, never()).map(any(), any());
        verify(batchMessageService, never()).getRecentMessagesForConversations(any(), anyInt());
    }

    @Test
    @DisplayName("Агрегация страницы бесед - нулевой лимит сообщений")
    void aggregateConversationsPage_whenZeroMessagesLimit_shouldReturnPageWithoutMessages() {
        List<Conversation> conversations = TestDataFactory.createConversationListWithMessages(2);
        Page<Conversation> conversationsPage = new PageImpl<>(conversations);

        conversations.forEach(conversation -> {
            ConversationDetailsResponse response = TestDataFactory.createConversationDetailsResponse(
                    conversation.getId(), conversation.getUser1Id(), conversation.getUser2Id());
            when(mapper.map(conversation, ConversationDetailsResponse.class)).thenReturn(response);
        });

        Page<ConversationDetailsResponse> result = conversationDetailsAggregator.aggregateConversationsPage(
                conversationsPage, true, 0);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());

        result.getContent().forEach(response -> {
            assertTrue(response.getMessages().isEmpty());
            assertNull(response.getLastMessageId());
            assertEquals(0L, response.getMessagesCount());
        });

        verify(batchMessageService, never()).getRecentMessagesForConversations(any(), anyInt());
    }

    @Test
    @DisplayName("Агрегация страницы бесед - лимит превышает максимальный")
    void aggregateConversationsPage_whenLimitExceedsMax_shouldUseMaxLimit() {
        List<Conversation> conversations = TestDataFactory.createConversationListWithMessages(2);
        List<UUID> conversationIds = conversations.stream().map(Conversation::getId).toList();
        Page<Conversation> conversationsPage = new PageImpl<>(conversations);
        int messagesLimit = 100;
        int effectiveLimit = 50;

        Map<UUID, List<MessageResponse>> messagesMap = TestDataFactory.createRecentMessagesMap(conversationIds, effectiveLimit);

        conversations.forEach(conversation -> {
            ConversationDetailsResponse response = TestDataFactory.createConversationDetailsResponse(
                    conversation.getId(), conversation.getUser1Id(), conversation.getUser2Id());
            when(mapper.map(conversation, ConversationDetailsResponse.class)).thenReturn(response);
        });

        when(batchMessageService.getRecentMessagesForConversations(anyList(), eq(effectiveLimit)))
                .thenReturn(messagesMap);

        Page<ConversationDetailsResponse> result = conversationDetailsAggregator.aggregateConversationsPage(
                conversationsPage, true, messagesLimit);

        assertNotNull(result);
        verify(batchMessageService).getRecentMessagesForConversations(anyList(), eq(effectiveLimit));

        verify(batchMessageService).getRecentMessagesForConversations(
                argThat(list -> list.containsAll(conversationIds) && list.size() == conversationIds.size()),
                eq(effectiveLimit)
        );
    }

    @Test
    @DisplayName("Агрегация деталей беседы - беседа null")
    void aggregateConversationDetails_whenConversationNull_shouldThrowException() {
        assertThrows(NullPointerException.class, () ->
                conversationDetailsAggregator.aggregateConversationDetails(null, true, 10)
        );

        verify(mapper, never()).map(any(), any());
        verify(batchMessageService, never()).getMessagesByConversation(any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Агрегация страницы бесед - с сообщениями")
    void aggregateConversationsPage_whenIncludeMessages_shouldReturnPageWithMessages() {
        List<Conversation> conversations = TestDataFactory.createConversationListWithMessages(3);
        List<UUID> conversationIds = conversations.stream().map(Conversation::getId).toList();
        Page<Conversation> conversationsPage = new PageImpl<>(conversations);
        int messagesLimit = 5;

        Map<UUID, List<MessageResponse>> messagesMap = TestDataFactory.createRecentMessagesMap(conversationIds, messagesLimit);

        when(batchMessageService.getRecentMessagesForConversations(anyList(), eq(messagesLimit)))
                .thenReturn(messagesMap);

        List<ConversationDetailsResponse> expectedResponses = new ArrayList<>();
        for (Conversation conversation : conversations) {
            ConversationDetailsResponse response = ConversationDetailsResponse.builder()
                    .conversationId(conversation.getId())
                    .user1Id(conversation.getUser1Id())
                    .user2Id(conversation.getUser2Id())
                    .createdAt(conversation.getCreatedAt())
                    .updatedAt(conversation.getUpdatedAt())
                    .messages(messagesMap.getOrDefault(conversation.getId(), Collections.emptyList()))
                    .messagesCount((long) messagesMap.getOrDefault(conversation.getId(), Collections.emptyList()).size())
                    .lastMessageId(messagesMap.containsKey(conversation.getId()) && !messagesMap.get(conversation.getId()).isEmpty() ?
                            messagesMap.get(conversation.getId()).get(0).getMessageId() : null)
                    .build();
            expectedResponses.add(response);

            when(mapper.map(eq(conversation), eq(ConversationDetailsResponse.class)))
                    .thenReturn(response);
        }

        Page<ConversationDetailsResponse> result = conversationDetailsAggregator.aggregateConversationsPage(
                conversationsPage, true, messagesLimit);

        assertNotNull(result);
        assertEquals(3, result.getContent().size());

        result.getContent().forEach(response -> {
            assertTrue(conversationIds.contains(response.getConversationId()));
            assertNotNull(response.getMessages());
        });

        verify(batchMessageService).getRecentMessagesForConversations(anyList(), eq(messagesLimit));
    }

    @Test
    @DisplayName("Агрегация страницы бесед - исключение при маппинге")
    void aggregateConversationsPage_whenMappingException_shouldHandleGracefully() {
        List<Conversation> conversations = TestDataFactory.createConversationListWithMessages(2);
        Page<Conversation> conversationsPage = new PageImpl<>(conversations);
        int messagesLimit = 5;

        Map<UUID, List<MessageResponse>> messagesMap = TestDataFactory.createRecentMessagesMap(
                conversations.stream().map(Conversation::getId).toList(), messagesLimit);

        when(batchMessageService.getRecentMessagesForConversations(anyList(), eq(messagesLimit)))
                .thenReturn(messagesMap);
        when(mapper.map(conversations.get(0), ConversationDetailsResponse.class))
                .thenThrow(new RuntimeException("Mapping error"));

        ConversationDetailsResponse validResponse = ConversationDetailsResponse.builder()
                .conversationId(conversations.get(1).getId())
                .user1Id(conversations.get(1).getUser1Id())
                .user2Id(conversations.get(1).getUser2Id())
                .createdAt(conversations.get(1).getCreatedAt())
                .updatedAt(conversations.get(1).getUpdatedAt())
                .messages(messagesMap.getOrDefault(conversations.get(1).getId(), Collections.emptyList()))
                .messagesCount((long) messagesMap.getOrDefault(conversations.get(1).getId(), Collections.emptyList()).size())
                .lastMessageId(messagesMap.containsKey(conversations.get(1).getId()) && !messagesMap.get(conversations.get(1).getId()).isEmpty() ?
                        messagesMap.get(conversations.get(1).getId()).get(0).getMessageId() : null)
                .build();

        when(mapper.map(conversations.get(1), ConversationDetailsResponse.class))
                .thenReturn(validResponse);

        Page<ConversationDetailsResponse> result = conversationDetailsAggregator.aggregateConversationsPage(
                conversationsPage, true, messagesLimit);

        assertNotNull(result);
        List<ConversationDetailsResponse> content = result.getContent();
        assertEquals(1, content.size());
        assertEquals(conversations.get(1).getId(), content.get(0).getConversationId());

        verify(batchMessageService).getRecentMessagesForConversations(anyList(), eq(messagesLimit));
    }
}