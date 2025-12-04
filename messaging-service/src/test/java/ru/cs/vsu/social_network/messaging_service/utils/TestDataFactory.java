package ru.cs.vsu.social_network.messaging_service.utils;

import org.springframework.data.domain.Sort;
import ru.cs.vsu.social_network.messaging_service.dto.request.messaging.*;
import ru.cs.vsu.social_network.messaging_service.dto.request.pageable.PageRequest;
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

    public static final LocalDateTime TEST_CREATED_AT = LocalDateTime.of(2024, 1, 1, 10, 0);
    public static final LocalDateTime TEST_UPDATED_AT = LocalDateTime.of(2024, 1, 1, 11, 0);

    /**
     * Создает тестовый запрос на создание сообщения с параметрами по умолчанию.
     *
     * @return запрос на создание сообщения
     */
    public static MessageCreateRequest createMessageCreateRequest() {
        return MessageCreateRequest.builder()
                .receiverId(TEST_USER2_ID)
                .content(TEST_MESSAGE_CONTENT)
                .build();
    }

    /**
     * Создает тестовый запрос на создание сообщения с указанными параметрами.
     *
     * @param receiverId идентификатор получателя
     * @param content содержание сообщения
     * @return запрос на создание сообщения
     */
    public static MessageCreateRequest createMessageCreateRequest(final UUID receiverId, final String content) {
        return MessageCreateRequest.builder()
                .receiverId(receiverId)
                .content(content)
                .build();
    }

    /**
     * Создает тестовый запрос на редактирование сообщения с параметрами по умолчанию.
     *
     * @return запрос на редактирование сообщения
     */
    public static MessageEditRequest createMessageEditRequest() {
        return MessageEditRequest.builder()
                .messageId(TEST_MESSAGE_ID)
                .content(TEST_UPDATED_CONTENT)
                .build();
    }

    /**
     * Создает тестовый запрос на редактирование сообщения с указанными параметрами.
     *
     * @param messageId идентификатор сообщения
     * @param content новое содержание сообщения
     * @return запрос на редактирование сообщения
     */
    public static MessageEditRequest createMessageEditRequest(final UUID messageId, final String content) {
        return MessageEditRequest.builder()
                .messageId(messageId)
                .content(content)
                .build();
    }

    /**
     * Создает тестовый запрос на удаление сообщения с параметрами по умолчанию.
     *
     * @return запрос на удаление сообщения
     */
    public static MessageDeleteRequest createMessageDeleteRequest() {
        return MessageDeleteRequest.builder()
                .messageId(TEST_MESSAGE_ID)
                .build();
    }

    /**
     * Создает тестовый запрос на загрузку изображения для сообщения с параметрами по умолчанию.
     *
     * @return запрос на загрузку изображения
     */
    public static MessageUploadImageRequest createMessageUploadImageRequest() {
        return MessageUploadImageRequest.builder()
                .messageId(TEST_MESSAGE_ID)
                .imageUrl(TEST_IMAGE_URL)
                .build();
    }

    /**
     * Создает тестовый запрос на загрузку изображения для сообщения с указанными параметрами.
     *
     * @param messageId идентификатор сообщения
     * @param imageUrl URL изображения
     * @return запрос на загрузку изображения
     */
    public static MessageUploadImageRequest createMessageUploadImageRequest(final UUID messageId, final String imageUrl) {
        return MessageUploadImageRequest.builder()
                .messageId(messageId)
                .imageUrl(imageUrl)
                .build();
    }

    /**
     * Создает тестовый запрос на удаление изображения из сообщения с параметрами по умолчанию.
     *
     * @return запрос на удаление изображения
     */
    public static MessageRemoveImageRequest createMessageRemoveImageRequest() {
        return MessageRemoveImageRequest.builder()
                .messageId(TEST_MESSAGE_ID)
                .build();
    }

    /**
     * Создает тестовый запрос на постраничное получение данных с параметрами по умолчанию.
     *
     * @return запрос на постраничное получение данных
     */
    public static PageRequest createPageRequest() {
        return PageRequest.builder()
                .pageNumber(0)
                .size(10)
                .sortBy("createdAt")
                .direction(Sort.Direction.DESC)
                .build();
    }

    /**
     * Создает тестовую сущность беседы с параметрами по умолчанию.
     *
     * @return сущность беседы
     */
    public static Conversation createConversationEntity() {
        return createConversationEntity(TEST_CONVERSATION_ID, TEST_USER_ID, TEST_USER2_ID);
    }

    /**
     * Создает тестовую сущность беседы с указанными параметрами.
     *
     * @param id идентификатор беседы
     * @param user1Id идентификатор первого пользователя
     * @param user2Id идентификатор второго пользователя
     * @return сущность беседы
     */
    public static Conversation createConversationEntity(final UUID id, final UUID user1Id, final UUID user2Id) {
        Conversation conversation = new Conversation();
        conversation.setId(id);
        conversation.setUser1Id(user1Id);
        conversation.setUser2Id(user2Id);
        conversation.setCreatedAt(TEST_CREATED_AT);
        conversation.setUpdatedAt(TEST_UPDATED_AT);
        return conversation;
    }

    /**
     * Создает тестовую сущность сообщения с параметрами по умолчанию.
     *
     * @return сущность сообщения
     */
    public static Message createMessageEntity() {
        return createMessageEntity(TEST_MESSAGE_ID, TEST_USER_ID, TEST_USER2_ID, TEST_CONVERSATION_ID);
    }

    /**
     * Создает тестовую сущность сообщения с указанными параметрами.
     *
     * @param id идентификатор сообщения
     * @param senderId идентификатор отправителя
     * @param receiverId идентификатор получателя
     * @param conversationId идентификатор беседы
     * @return сущность сообщения
     */
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

    /**
     * Создает тестовую сущность сообщения с изображением с параметрами по умолчанию.
     *
     * @return сущность сообщения с изображением
     */
    public static Message createMessageEntityWithImage() {
        Message message = createMessageEntity();
        message.setImageUrl(TEST_IMAGE_URL);
        return message;
    }

    /**
     * Создает тестовый ответ с данными сообщения с параметрами по умолчанию.
     *
     * @return ответ с данными сообщения
     */
    public static MessageResponse createMessageResponse() {
        return createMessageResponse(TEST_MESSAGE_ID, TEST_USER_ID, TEST_USER2_ID, TEST_CONVERSATION_ID);
    }

    /**
     * Создает тестовый ответ с данными сообщения с указанными параметрами.
     *
     * @param messageId идентификатор сообщения
     * @param senderId идентификатор отправителя
     * @param receiverId идентификатор получателя
     * @param conversationId идентификатор беседы
     * @return ответ с данными сообщения
     */
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

    /**
     * Создает тестовый ответ с данными беседы с параметрами по умолчанию.
     *
     * @return ответ с данными беседы
     */
    public static ConversationResponse createConversationResponse() {
        return createConversationResponse(TEST_CONVERSATION_ID, TEST_USER_ID, TEST_USER2_ID);
    }

    /**
     * Создает тестовый ответ с данными беседы с указанными параметрами.
     *
     * @param conversationId идентификатор беседы
     * @param user1Id идентификатор первого пользователя
     * @param user2Id идентификатор второго пользователя
     * @return ответ с данными беседы
     */
    public static ConversationResponse createConversationResponse(final UUID conversationId, final UUID user1Id, final UUID user2Id) {
        return ConversationResponse.builder()
                .conversationId(conversationId)
                .user1Id(user1Id)
                .user2Id(user2Id)
                .createdAt(TEST_CREATED_AT)
                .updatedAt(TEST_UPDATED_AT)
                .build();
    }

    /**
     * Создает тестовый ответ с детальными данными беседы с параметрами по умолчанию.
     *
     * @return ответ с детальными данными беседы
     */
    public static ConversationDetailsResponse createConversationDetailsResponse() {
        return createConversationDetailsResponse(TEST_CONVERSATION_ID, TEST_USER_ID, TEST_USER2_ID);
    }

    /**
     * Создает тестовый ответ с детальными данными беседы с указанными параметрами.
     *
     * @param conversationId идентификатор беседы
     * @param user1Id идентификатор первого пользователя
     * @param user2Id идентификатор второго пользователя
     * @return ответ с детальными данными беседы
     */
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

    /**
     * Создает тестовый постраничный ответ с указанным содержимым.
     *
     * @param <T> тип элементов в содержимом
     * @param content список элементов содержимого
     * @return постраничный ответ
     */
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

    /**
     * Создает тестовое кэш-событие с указанным типом и идентификатором сущности.
     *
     * @param eventType тип события
     * @param entityId идентификатор сущности
     * @return кэш-событие
     */
    public static GenericMessagingCacheEvent createCacheEvent(final CacheEventType eventType, final UUID entityId) {
        return new GenericMessagingCacheEvent(
                new Object(),
                eventType,
                entityId,
                Map.of()
        );
    }

    /**
     * Создает тестовый индикатор набора текста с параметрами по умолчанию.
     *
     * @return индикатор набора текста
     */
    public static TypingIndicator createTypingIndicator() {
        return TypingIndicator.builder()
                .userId(TEST_USER_ID)
                .conversationId(TEST_CONVERSATION_ID)
                .typing(true)
                .build();
    }

    /**
     * Создает список тестовых идентификаторов сообщений указанного размера.
     *
     * @param count количество идентификаторов для создания
     * @return список идентификаторов сообщений
     */
    public static List<UUID> createMessageIds(final int count) {
        List<UUID> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ids.add(UUID.randomUUID());
        }
        return ids;
    }

    /**
     * Создает список тестовых сущностей сообщений указанного размера.
     *
     * @param count количество сообщений для создания
     * @return список сущностей сообщений
     */
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

    /**
     * Создает список тестовых сущностей бесед указанного размера.
     *
     * @param count количество бесед для создания
     * @return список сущностей бесед
     */
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

    /**
     * Создает карту количества непрочитанных сообщений для указанных идентификаторов бесед.
     *
     * @param conversationIds список идентификаторов бесед
     * @return карта, где ключ - идентификатор беседы, значение - количество непрочитанных сообщений
     */
    public static Map<UUID, Long> createUnreadMessagesCountMap(List<UUID> conversationIds) {
        final Map<UUID, Long> result = new HashMap<>();
        for (UUID conversationId : conversationIds) {
            result.put(conversationId, 5L);
        }
        return result;
    }

    /**
     * Создает карту последних сообщений для указанных идентификаторов бесед.
     *
     * @param conversationIds список идентификаторов бесед
     * @param limit максимальное количество сообщений для каждой беседы
     * @return карта, где ключ - идентификатор беседы, значение - список последних сообщений
     */
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

    /**
     * Создает список тестовых сущностей сообщений для указанной беседы.
     *
     * @param count количество сообщений для создания
     * @param conversationId идентификатор беседы
     * @return список сущностей сообщений
     */
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

    /**
     * Создает список тестовых сущностей непрочитанных сообщений.
     *
     * @param count количество сообщений для создания
     * @param receiverId идентификатор получателя
     * @param conversationId идентификатор беседы
     * @return список сущностей непрочитанных сообщений
     */
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

    /**
     * Создает список тестовых сущностей бесед с сообщениями.
     *
     * @param count количество бесед для создания
     * @return список сущностей бесед
     */
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

    /**
     * Создает тестовый идентификатор беседы.
     *
     * @return идентификатор беседы
     */
    public static UUID createTestConversationId() {
        return UUID.fromString("8dce7d87-60d9-4b66-8851-e7dba4684538");
    }

    /**
     * Создает тестовый идентификатор сообщения.
     *
     * @return идентификатор сообщения
     */
    public static UUID createTestMessageId() {
        return UUID.fromString("40d4dde4-663d-4113-bd08-25f5e1d42efe");
    }

    /**
     * Создает тестовый идентификатор первого пользователя.
     *
     * @return идентификатор первого пользователя
     */
    public static UUID createTestUserId1() {
        return UUID.fromString("3fc4530f-5c0c-429d-8a64-810ce3bd1415");
    }

    /**
     * Создает тестовый идентификатор второго пользователя.
     *
     * @return идентификатор второго пользователя
     */
    public static UUID createTestUserId2() {
        return UUID.fromString("e0d8a734-6f6c-4ab4-b4fe-e93cc63d8406");
    }

    /**
     * Создает набор тестовых ключей Redis для пользователя.
     *
     * @param userId идентификатор пользователя
     * @return набор ключей Redis
     */
    public static Set<String> createRedisKeysForUser(UUID userId) {
        Set<String> keys = new HashSet<>();
        keys.add("userConversations::user:" + userId + ":page:0");
        keys.add("userConversations::user:" + userId + ":page:1");
        keys.add("userConversations::" + userId + ":detailed:true");
        keys.add(userId + "::conversations:page:0");
        return keys;
    }

    /**
     * Создает набор тестовых ключей Redis для беседы.
     *
     * @param conversationId идентификатор беседы
     * @return набор ключей Redis
     */
    public static Set<String> createRedisKeysForConversation(UUID conversationId) {
        Set<String> keys = new HashSet<>();
        keys.add("conversationDetails::conversation:" + conversationId + ":messages:true");
        keys.add("conversationMessages::conversation:" + conversationId + ":page:0");
        keys.add("conversationDetails::" + conversationId + ":full");
        keys.add("conversationMessages::" + conversationId + ":sorted");
        return keys;
    }

    /**
     * Создает набор тестовых ключей Redis для сообщения.
     *
     * @param messageId идентификатор сообщения
     * @return набор ключей Redis
     */
    public static Set<String> createRedisKeysForMessage(UUID messageId) {
        Set<String> keys = new HashSet<>();
        keys.add("message::message:" + messageId + ":details");
        keys.add("message::" + messageId);
        keys.add("message:details:" + messageId);
        return keys;
    }

    /**
     * Создает набор тестовых ключей Redis для первых страниц.
     *
     * @return набор ключей Redis
     */
    public static Set<String> createRedisKeysForFirstPages() {
        Set<String> keys = new HashSet<>();
        keys.add("userConversations::page:0");
        keys.add("userConversations::page:1");
        keys.add("userConversations::page:2");
        keys.add("userConversations::user:" + createTestUserId1() + ":page:0");
        return keys;
    }
}