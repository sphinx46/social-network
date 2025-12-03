package ru.cs.vsu.social_network.messaging_service.utils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.cs.vsu.social_network.messaging_service.dto.request.messaging.*;
import ru.cs.vsu.social_network.messaging_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.messaging_service.dto.request.websocket.MessageStatusUpdate;
import ru.cs.vsu.social_network.messaging_service.dto.request.websocket.TypingIndicator;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.ConversationDetailsResponse;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.ConversationResponse;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.MessageResponse;
import ru.cs.vsu.social_network.messaging_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.messaging_service.entity.Conversation;
import ru.cs.vsu.social_network.messaging_service.entity.Message;
import ru.cs.vsu.social_network.messaging_service.entity.enums.MessageStatus;
import ru.cs.vsu.social_network.messaging_service.event.CacheEventType;
import ru.cs.vsu.social_network.messaging_service.event.GenericMessagingCacheEvent;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Фабрика тестовых данных для messaging-service.
 * Содержит методы для создания тестовых DTO, сущностей и запросов.
 */
public final class TestDataFactory {

    public static final MessageStatus TEST_MESSAGE_STATUS = MessageStatus.SENT;
    public static final MessageStatus TEST_UPDATED_MESSAGE_STATUS = MessageStatus.DELIVERED;

    /**
     * Приватный конструктор для запрета создания экземпляров утилитного класса.
     */
    private TestDataFactory() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static final UUID TEST_USER_ID = UUID.fromString("3fc4530f-5c0c-429d-8a64-810ce3bd1415");
    public static final UUID TEST_USER2_ID = UUID.fromString("e0d8a734-6f6c-4ab4-b4fe-e93cc63d8406");
    public static final UUID TEST_CONVERSATION_ID = UUID.fromString("8dce7d87-60d9-4b66-8851-e7dba4684538");
    public static final UUID TEST_MESSAGE_ID = UUID.fromString("40d4dde4-663d-4113-bd08-25f5e1d42efe");

    public static final String TEST_MESSAGE_CONTENT = "Test message content";
    public static final String TEST_UPDATED_CONTENT = "Updated message content";
    public static final String TEST_IMAGE_URL = "http://example.com/image.jpg";
    public static final String TEST_CONVERSATION_NAME = "Test Conversation";

    public static final LocalDateTime TEST_CREATED_AT = LocalDateTime.of(2024, 1, 1, 10, 0);
    public static final LocalDateTime TEST_UPDATED_AT = LocalDateTime.of(2024, 1, 1, 11, 0);

    public static MessageCreateRequest createMessageCreateRequest() {
        return MessageCreateRequest.builder()
                .receiverId(TEST_USER2_ID)
                .content(TEST_MESSAGE_CONTENT)
                .build();
    }

    public static MessageCreateRequest createMessageCreateRequest(final UUID receiverId, final String content) {
        return MessageCreateRequest.builder()
                .receiverId(receiverId)
                .content(content)
                .build();
    }

    public static MessageEditRequest createMessageEditRequest() {
        return MessageEditRequest.builder()
                .messageId(TEST_MESSAGE_ID)
                .content(TEST_UPDATED_CONTENT)
                .build();
    }

    public static MessageEditRequest createMessageEditRequest(final UUID messageId, final String content) {
        return MessageEditRequest.builder()
                .messageId(messageId)
                .content(content)
                .build();
    }

    public static MessageDeleteRequest createMessageDeleteRequest() {
        return MessageDeleteRequest.builder()
                .messageId(TEST_MESSAGE_ID)
                .build();
    }

    public static MessageDeleteRequest createMessageDeleteRequest(final UUID messageId) {
        return MessageDeleteRequest.builder()
                .messageId(messageId)
                .build();
    }

    public static MessageUploadImageRequest createMessageUploadImageRequest() {
        return MessageUploadImageRequest.builder()
                .messageId(TEST_MESSAGE_ID)
                .imageUrl(TEST_IMAGE_URL)
                .build();
    }

    public static MessageUploadImageRequest createMessageUploadImageRequest(final UUID messageId, final String imageUrl) {
        return MessageUploadImageRequest.builder()
                .messageId(messageId)
                .imageUrl(imageUrl)
                .build();
    }

    public static MessageRemoveImageRequest createMessageRemoveImageRequest() {
        return MessageRemoveImageRequest.builder()
                .messageId(TEST_MESSAGE_ID)
                .build();
    }

    public static MessageRemoveImageRequest createMessageRemoveImageRequest(final UUID messageId) {
        return MessageRemoveImageRequest.builder()
                .messageId(messageId)
                .build();
    }

    public static PageRequest createPageRequest() {
        return PageRequest.builder()
                .pageNumber(0)
                .size(10)
                .sortBy("createdAt")
                .direction(Sort.Direction.DESC)
                .build();
    }

    public static PageRequest createPageRequest(final int page, final int size) {
        return PageRequest.builder()
                .pageNumber(page)
                .size(size)
                .sortBy("createdAt")
                .direction(Sort.Direction.DESC)
                .build();
    }

    public static Conversation createConversationEntity() {
        return createConversationEntity(TEST_CONVERSATION_ID, TEST_USER_ID, TEST_USER2_ID);
    }

    public static Conversation createConversationEntity(final UUID id, final UUID user1Id, final UUID user2Id) {
        Conversation conversation = new Conversation();
        conversation.setId(id);
        conversation.setUser1Id(user1Id);
        conversation.setUser2Id(user2Id);
        conversation.setCreatedAt(TEST_CREATED_AT);
        conversation.setUpdatedAt(TEST_UPDATED_AT);
        return conversation;
    }

    public static Message createMessageEntity() {
        return createMessageEntity(TEST_MESSAGE_ID, TEST_USER_ID, TEST_USER2_ID, TEST_CONVERSATION_ID);
    }

    public static Message createMessageEntity(final UUID id, final UUID senderId, final UUID receiverId, final UUID conversationId) {
        Message message = new Message();
        message.setId(id);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(TEST_MESSAGE_CONTENT);
        message.setStatus(MessageStatus.SENT);
        message.setCreatedAt(TEST_CREATED_AT);
        message.setUpdatedAt(TEST_UPDATED_AT);

        Conversation conversation = createConversationEntity(conversationId, senderId, receiverId);
        message.setConversation(conversation);

        return message;
    }

    public static Message createMessageEntityWithImage() {
        Message message = createMessageEntity();
        message.setImageUrl(TEST_IMAGE_URL);
        return message;
    }

    public static MessageResponse createMessageResponse() {
        return createMessageResponse(TEST_MESSAGE_ID, TEST_USER_ID, TEST_USER2_ID, TEST_CONVERSATION_ID);
    }

    public static MessageResponse createMessageResponse(final UUID messageId, final UUID senderId, final UUID receiverId, final UUID conversationId) {
        return MessageResponse.builder()
                .messageId(messageId)
                .senderId(senderId)
                .receiverId(receiverId)
                .conversationId(conversationId)
                .content(TEST_MESSAGE_CONTENT)
                .imageUrl(null)
                .status(MessageStatus.SENT)
                .createdAt(TEST_CREATED_AT)
                .updatedAt(TEST_UPDATED_AT)
                .build();
    }

    public static ConversationResponse createConversationResponse() {
        return createConversationResponse(TEST_CONVERSATION_ID, TEST_USER_ID, TEST_USER2_ID);
    }

    public static ConversationResponse createConversationResponse(final UUID conversationId, final UUID user1Id, final UUID user2Id) {
        return ConversationResponse.builder()
                .conversationId(conversationId)
                .user1Id(user1Id)
                .user2Id(user2Id)
                .createdAt(TEST_CREATED_AT)
                .updatedAt(TEST_UPDATED_AT)
                .build();
    }

    public static ConversationDetailsResponse createConversationDetailsResponse() {
        return createConversationDetailsResponse(TEST_CONVERSATION_ID, TEST_USER_ID, TEST_USER2_ID);
    }

    public static ConversationDetailsResponse createConversationDetailsResponse(final UUID conversationId, final UUID user1Id, final UUID user2Id) {
        MessageResponse messageResponse = createMessageResponse();
        return ConversationDetailsResponse.builder()
                .conversationId(conversationId)
                .user1Id(user1Id)
                .user2Id(user2Id)
                .messages(List.of(messageResponse))
                .messagesCount(1L)
                .lastMessageId(TEST_MESSAGE_ID)
                .createdAt(TEST_CREATED_AT)
                .updatedAt(TEST_UPDATED_AT)
                .build();
    }

    public static <T> Page<T> createPage(final List<T> content) {
        return new PageImpl<>(content, Pageable.ofSize(10), content.size());
    }

    public static <T> PageResponse<T> createPageResponse(final List<T> content) {
        return PageResponse.<T>builder()
                .content(content)
                .currentPage(0)
                .pageSize(10)
                .totalElements((long) content.size())
                .totalPages(1)
                .first(true)
                .last(true)
                .build();
    }

    public static GenericMessagingCacheEvent createCacheEvent(final CacheEventType eventType, final UUID entityId) {
        return new GenericMessagingCacheEvent(
                new Object(),
                eventType,
                entityId,
                Map.of()
        );
    }

    public static MessageStatusUpdate createMessageStatusUpdate() {
        return MessageStatusUpdate.builder()
                .conversationId(TEST_CONVERSATION_ID)
                .readCount(5)
                .status(MessageStatus.READ.name())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static TypingIndicator createTypingIndicator() {
        return TypingIndicator.builder()
                .userId(TEST_USER_ID)
                .conversationId(TEST_CONVERSATION_ID)
                .typing(true)
                .build();
    }

    public static List<UUID> createMessageIds(final int count) {
        List<UUID> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ids.add(UUID.randomUUID());
        }
        return ids;
    }

    public static List<Message> createMessageList(final int count) {
        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            messages.add(createMessageEntity(
                    UUID.randomUUID(),
                    TEST_USER_ID,
                    TEST_USER2_ID,
                    TEST_CONVERSATION_ID
            ));
        }
        return messages;
    }

    public static List<Conversation> createConversationList(final int count) {
        List<Conversation> conversations = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            conversations.add(createConversationEntity(
                    UUID.randomUUID(),
                    TEST_USER_ID,
                    UUID.randomUUID()
            ));
        }
        return conversations;
    }

    public static Map<UUID, Long> createUnreadMessagesCountMap(List<UUID> conversationIds) {
        final Map<UUID, Long> result = new HashMap<>();
        for (UUID conversationId : conversationIds) {
            result.put(conversationId, 5L);
        }
        return result;
    }

    public static Map<UUID, List<MessageResponse>> createRecentMessagesMap(List<UUID> conversationIds, int limit) {
        final Map<UUID, List<MessageResponse>> result = new HashMap<>();
        for (UUID conversationId : conversationIds) {
            List<MessageResponse> messages = new ArrayList<>();
            for (int i = 0; i < Math.min(limit, 3); i++) {
                messages.add(createMessageResponse(
                        UUID.randomUUID(),
                        i % 2 == 0 ? TEST_USER_ID : TEST_USER2_ID,
                        i % 2 == 0 ? TEST_USER2_ID : TEST_USER_ID,
                        conversationId
                ));
            }
            result.put(conversationId, messages);
        }
        return result;
    }

    public static List<Message> createMessageEntityList(final int count, final UUID conversationId) {
        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            messages.add(createMessageEntity(
                    UUID.randomUUID(),
                    TEST_USER_ID,
                    TEST_USER2_ID,
                    conversationId
            ));
        }
        return messages;
    }

    public static List<Message> createUnreadMessageEntityList(final int count, final UUID receiverId, final UUID conversationId) {
        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Message message = createMessageEntity(
                    UUID.randomUUID(),
                    i % 2 == 0 ? TEST_USER_ID : TEST_USER2_ID,
                    receiverId,
                    conversationId
            );
            message.setStatus(i % 2 == 0 ? MessageStatus.SENT : MessageStatus.DELIVERED);
            messages.add(message);
        }
        return messages;
    }

    public static List<Conversation> createConversationListWithMessages(int count) {
        List<Conversation> conversations = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Conversation conversation = createConversationEntity(
                    UUID.randomUUID(),
                    TEST_USER_ID,
                    UUID.randomUUID()
            );
            conversations.add(conversation);
        }
        return conversations;
    }

    public static Page<Conversation> createConversationPage(List<Conversation> conversations) {
        return new PageImpl<>(conversations, org.springframework.data.domain.PageRequest.of(0, 10), conversations.size());
    }

    public static ConversationDetailsResponse createConversationDetailsResponseWithMessages(UUID conversationId, int messageCount) {
        ConversationDetailsResponse response = createConversationDetailsResponse(conversationId, TEST_USER_ID, TEST_USER2_ID);
        List<MessageResponse> messages = new ArrayList<>();
        for (int i = 0; i < messageCount; i++) {
            messages.add(createMessageResponse(
                    UUID.randomUUID(),
                    i % 2 == 0 ? TEST_USER_ID : TEST_USER2_ID,
                    i % 2 == 0 ? TEST_USER2_ID : TEST_USER_ID,
                    conversationId
            ));
        }
        response.setMessages(messages);
        response.setMessagesCount((long) messageCount);
        if (!messages.isEmpty()) {
            response.setLastMessageId(messages.get(messages.size() - 1).getMessageId());
        }
        return response;
    }

    public static UUID createRandomUUID() {
        return UUID.randomUUID();
    }

    public static UUID createTestConversationId() {
        return UUID.fromString("8dce7d87-60d9-4b66-8851-e7dba4684538");
    }

    public static UUID createTestMessageId() {
        return UUID.fromString("40d4dde4-663d-4113-bd08-25f5e1d42efe");
    }

    public static UUID createTestUserId1() {
        return UUID.fromString("3fc4530f-5c0c-429d-8a64-810ce3bd1415");
    }

    public static UUID createTestUserId2() {
        return UUID.fromString("e0d8a734-6f6c-4ab4-b4fe-e93cc63d8406");
    }

    public static List<UUID> createConversationIds(int count) {
        List<UUID> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ids.add(UUID.randomUUID());
        }
        return ids;
    }

    public static Map<UUID, CacheEventType> createPendingConversationMap(int count) {
        Map<UUID, CacheEventType> map = new HashMap<>();
        for (int i = 0; i < count; i++) {
            map.put(UUID.randomUUID(), CacheEventType.MESSAGE_CREATED);
        }
        return map;
    }

    public static Map<UUID, CacheEventType> createPendingMessageMap(int count) {
        Map<UUID, CacheEventType> map = new HashMap<>();
        for (int i = 0; i < count; i++) {
            map.put(UUID.randomUUID(), CacheEventType.MESSAGE_UPDATED);
        }
        return map;
    }

    public static Map<UUID, CacheEventType> createPendingUserMap(int count) {
        Map<UUID, CacheEventType> map = new HashMap<>();
        for (int i = 0; i < count; i++) {
            map.put(UUID.randomUUID(), CacheEventType.MESSAGES_READ);
        }
        return map;
    }

    public static Set<String> createRedisKeysForUser(UUID userId) {
        Set<String> keys = new HashSet<>();
        keys.add("userConversations::user:" + userId + ":page:0");
        keys.add("userConversations::user:" + userId + ":page:1");
        keys.add("userConversations::" + userId + ":detailed:true");
        keys.add(userId + "::conversations:page:0");
        return keys;
    }

    public static Set<String> createRedisKeysForConversation(UUID conversationId) {
        Set<String> keys = new HashSet<>();
        keys.add("conversationDetails::conversation:" + conversationId + ":messages:true");
        keys.add("conversationMessages::conversation:" + conversationId + ":page:0");
        keys.add("conversationDetails::" + conversationId + ":full");
        keys.add("conversationMessages::" + conversationId + ":sorted");
        return keys;
    }

    public static Set<String> createRedisKeysForMessage(UUID messageId) {
        Set<String> keys = new HashSet<>();
        keys.add("message::message:" + messageId + ":details");
        keys.add("message::" + messageId);
        keys.add("message:details:" + messageId);
        return keys;
    }

    public static Set<String> createRedisKeysForFirstPages() {
        Set<String> keys = new HashSet<>();
        keys.add("userConversations::page:0");
        keys.add("userConversations::page:1");
        keys.add("userConversations::page:2");
        keys.add("userConversations::user:" + createTestUserId1() + ":page:0");
        return keys;
    }
}