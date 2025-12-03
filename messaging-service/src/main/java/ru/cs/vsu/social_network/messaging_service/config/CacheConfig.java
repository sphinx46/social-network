package ru.cs.vsu.social_network.messaging_service.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.ConversationDetailsResponse;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.ConversationResponse;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.MessageResponse;
import ru.cs.vsu.social_network.messaging_service.dto.response.pageable.PageResponse;

import java.time.Duration;

/**
 * Конфигурация кеширования для сервиса мессенджера.
 * Настраивает Redis как провайдер кеша с сериализацией JSON и явным указанием типов данных
 * для корректной десериализации.
 */
@EnableCaching
@Configuration
public class CacheConfig {

    public static final String CONVERSATION_DETAILS_CACHE = "conversationDetails";
    public static final String CONVERSATION_MESSAGES_CACHE = "conversationMessages";
    public static final String USER_CONVERSATIONS_CACHE = "userConversations";
    public static final String MESSAGE_CACHE = "message";
    public static final String CONVERSATION_BETWEEN_USERS_CACHE = "conversationBetweenUsers";

    /**
     * Создает основной ObjectMapper для всего приложения.
     * Регистрирует модуль для работы с Java 8 Date/Time API.
     *
     * @return настроенный ObjectMapper
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper;
    }

    /**
     * Создает RedisTemplate для работы с произвольными объектами в Redis.
     * Использует JSON сериализацию для значений и строковую для ключей.
     *
     * @param redisConnectionFactory фабрика подключений к Redis
     * @param objectMapper ObjectMapper для сериализации JSON
     * @return настроенный RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory redisConnectionFactory,
            ObjectMapper objectMapper
    ) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(
                createObjectMapperForRedis(objectMapper),
                Object.class
        );

        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setHashValueSerializer(serializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * Создает CacheManager для Spring Cache аннотаций.
     * Конфигурирует различные кеши с явным указанием типов данных
     * для предотвращения ошибок десериализации.
     *
     * @param connectionFactory фабрика подключений к Redis
     * @param objectMapper ObjectMapper для сериализации JSON
     * @return настроенный CacheManager
     */
    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper
    ) {
        ObjectMapper redisObjectMapper = createObjectMapperForRedis(objectMapper);

        Jackson2JsonRedisSerializer<ConversationDetailsResponse> conversationDetailsSerializer =
                new Jackson2JsonRedisSerializer<>(
                        redisObjectMapper,
                        ConversationDetailsResponse.class
                );

        Jackson2JsonRedisSerializer<ConversationResponse> conversationSerializer =
                new Jackson2JsonRedisSerializer<>(
                        redisObjectMapper,
                        ConversationResponse.class
                );

        Jackson2JsonRedisSerializer<MessageResponse> messageSerializer =
                new Jackson2JsonRedisSerializer<>(
                        redisObjectMapper,
                        MessageResponse.class
                );

        Jackson2JsonRedisSerializer<PageResponse<ConversationDetailsResponse>> conversationDetailsPageSerializer =
                new Jackson2JsonRedisSerializer<>(
                        redisObjectMapper,
                        redisObjectMapper.getTypeFactory()
                                .constructParametricType(PageResponse.class, ConversationDetailsResponse.class)
                );

        Jackson2JsonRedisSerializer<PageResponse<MessageResponse>> messagesPageSerializer =
                new Jackson2JsonRedisSerializer<>(
                        redisObjectMapper,
                        redisObjectMapper.getTypeFactory()
                                .constructParametricType(PageResponse.class, MessageResponse.class)
                );

        RedisCacheConfiguration conversationDetailsCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(conversationDetailsSerializer))
                .prefixCacheNameWith("");

        RedisCacheConfiguration conversationMessagesCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(2))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(conversationDetailsPageSerializer))
                .prefixCacheNameWith("");

        RedisCacheConfiguration userConversationsCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(20))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(conversationDetailsPageSerializer))
                .prefixCacheNameWith("");

        RedisCacheConfiguration messageCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(4))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(messageSerializer))
                .prefixCacheNameWith("");

        RedisCacheConfiguration conversationBetweenUsersCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(conversationSerializer))
                .prefixCacheNameWith("");

        return RedisCacheManager.builder(connectionFactory)
                .withCacheConfiguration(CONVERSATION_DETAILS_CACHE, conversationDetailsCacheConfig)
                .withCacheConfiguration(CONVERSATION_MESSAGES_CACHE, conversationMessagesCacheConfig)
                .withCacheConfiguration(USER_CONVERSATIONS_CACHE, userConversationsCacheConfig)
                .withCacheConfiguration(MESSAGE_CACHE, messageCacheConfig)
                .withCacheConfiguration(CONVERSATION_BETWEEN_USERS_CACHE, conversationBetweenUsersCacheConfig)
                .transactionAware()
                .build();
    }

    /**
     * Создает копию ObjectMapper с дополнительными настройками для Redis сериализации.
     * Отключает запись дат как timestamp и обработку пустых бинов.
     *
     * @param baseMapper базовый ObjectMapper
     * @return ObjectMapper с настройками для Redis
     */
    private ObjectMapper createObjectMapperForRedis(ObjectMapper baseMapper) {
        ObjectMapper redisMapper = baseMapper.copy();
        redisMapper.registerModule(new JavaTimeModule());
        redisMapper.disable(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                SerializationFeature.FAIL_ON_EMPTY_BEANS
        );

        redisMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return redisMapper;
    }

    /**
     * Утилитный метод для получения конфигурации кеша по имени.
     * Используется для единообразного получения TTL для различных кешей.
     *
     * @param cacheName имя кеша
     * @return конфигурация кеша
     */
    public static RedisCacheConfiguration getCacheConfiguration(String cacheName) {
        Duration ttl;

        switch (cacheName) {
            case CONVERSATION_DETAILS_CACHE:
                ttl = Duration.ofMinutes(30);
                break;
            case CONVERSATION_MESSAGES_CACHE:
                ttl = Duration.ofHours(2);
                break;
            case USER_CONVERSATIONS_CACHE:
                ttl = Duration.ofMinutes(20);
                break;
            case MESSAGE_CACHE:
                ttl = Duration.ofHours(4);
                break;
            case CONVERSATION_BETWEEN_USERS_CACHE:
                ttl = Duration.ofHours(1);
                break;
            default:
                ttl = Duration.ofMinutes(30);
        }

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .prefixCacheNameWith("");
    }
}