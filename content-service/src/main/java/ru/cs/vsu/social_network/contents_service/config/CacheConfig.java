package ru.cs.vsu.social_network.contents_service.config;

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
import ru.cs.vsu.social_network.contents_service.dto.response.content.PostDetailsResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;

import java.time.Duration;

/**
 * Конфигурация кеширования для сервиса контента.
 * Настраивает Redis как провайдер кеша с сериализацией JSON.
 */
@EnableCaching
@Configuration
public class CacheConfig {

    /**
     * Создает настроенный ObjectMapper с поддержкой Java 8 Date/Time API.
     *
     * @return настроенный ObjectMapper
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

    /**
     * Создает RedisTemplate для работы с произвольными объектами.
     * Используется для ручных операций с Redis.
     *
     * @param redisConnectionFactory фабрика подключений к Redis
     * @param objectMapper маппер JSON для сериализации/десериализации
     * @return настроенный RedisTemplate для Object
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
     *
     * @param connectionFactory фабрика подключений к Redis
     * @param objectMapper маппер JSON для сериализации/десериализации
     * @return настроенный CacheManager
     */
    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper
    ) {
        Jackson2JsonRedisSerializer<PostDetailsResponse> postDetailsSerializer =
                new Jackson2JsonRedisSerializer<>(
                        createObjectMapperForRedis(objectMapper),
                        PostDetailsResponse.class
                );

        Jackson2JsonRedisSerializer<PageResponse<PostDetailsResponse>> pageDetailsSerializer =
                new Jackson2JsonRedisSerializer<>(
                        createObjectMapperForRedis(objectMapper),
                        objectMapper.getTypeFactory()
                                .constructParametricType(PageResponse.class, PostDetailsResponse.class)
                );

        RedisCacheConfiguration postDetailsCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(postDetailsSerializer))
                .prefixCacheNameWith("");

        RedisCacheConfiguration pageDetailsCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(pageDetailsSerializer))
                .prefixCacheNameWith("");

        return RedisCacheManager.builder(connectionFactory)
                .withCacheConfiguration("postDetails", postDetailsCacheConfig)
                .withCacheConfiguration("userPageDetails", pageDetailsCacheConfig)
                .withCacheConfiguration("allPageDetails", pageDetailsCacheConfig)
                .transactionAware()
                .build();
    }

    /**
     * Создает ObjectMapper для Redis сериализации.
     */
    private ObjectMapper createObjectMapperForRedis(ObjectMapper baseMapper) {
        ObjectMapper redisMapper = baseMapper.copy();
        redisMapper.registerModule(new JavaTimeModule());
        redisMapper.disable(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                SerializationFeature.FAIL_ON_EMPTY_BEANS
        );
        return redisMapper;
    }
}