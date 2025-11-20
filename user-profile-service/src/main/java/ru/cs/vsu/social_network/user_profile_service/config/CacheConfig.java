package ru.cs.vsu.social_network.user_profile_service.config;

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
import ru.cs.vsu.social_network.user_profile_service.dto.response.ProfileResponse;

import java.time.Duration;

/**
 * Конфигурационный класс для настройки Redis кеширования в приложении.
 * Включает поддержку кеширования, настраивает сериализацию данных и управление кешем.
 * Использует явное указание типов для предотвращения ошибок ClassCastException.
 *
 * @author Spring Boot Application
 * @version 1.0
 * @see CacheManager
 * @see RedisTemplate
 * @see ObjectMapper
 */
@EnableCaching
@Configuration
public class CacheConfig {

    /**
     * Создает и настраивает основной ObjectMapper для
     * сериализации/десериализации JSON.
     * Настройки включают поддержку Java 8 Date/Time API и отключение
     * записи дат в формате timestamp.
     *
     * @return настроенный экземпляр ObjectMapper с поддержкой
     *         современных Java типов
     * @see ObjectMapper
     * @see JavaTimeModule
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * Создает сериализатор JSON для Redis с явным указанием типа
     * ProfileResponse.
     *
     * @param objectMapper настроенный ObjectMapper для сериализации
     * @return экземпляр Jackson2JsonRedisSerializer для типа
     *         ProfileResponse
     * @see Jackson2JsonRedisSerializer
     * @see ProfileResponse
     */
    @Bean
    public Jackson2JsonRedisSerializer<ProfileResponse>
            profileResponseSerializer(final ObjectMapper objectMapper) {
        return new Jackson2JsonRedisSerializer<>(
                objectMapper, ProfileResponse.class);
    }

    /**
     * Создает и настраивает RedisTemplate для операций с Redis.
     * Использует String сериализатор для ключей и универсальный JSON
     * сериализатор для значений.
     * Предназначен для общих операций с Redis, не связанных с
     * кешированием.
     *
     * @param redisConnectionFactory фабрика подключений к Redis
     * @param objectMapper настроенный ObjectMapper для сериализации
     * @return настроенный экземпляр RedisTemplate для работы с Redis
     * @see RedisTemplate
     * @see RedisConnectionFactory
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            final RedisConnectionFactory redisConnectionFactory,
            final ObjectMapper objectMapper) {

        RedisTemplate<String, Object> redisTemplate =
                new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(
                new StringRedisSerializer());

        Jackson2JsonRedisSerializer<Object> serializer =
                new Jackson2JsonRedisSerializer<>(
                        objectMapper, Object.class);
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setHashValueSerializer(serializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * Время жизни записи в кеше в минутах.
     */
    private static final int CACHE_TTL_MINUTES = 30;

    /**
     * Создает и настраивает менеджер кеширования для Spring Cache
     * abstraction.
     *
     * @param connectionFactory фабрика подключений к Redis
     * @param profileResponseSerializer сериализатор для значений типа
     *                                   ProfileResponse
     * @return настроенный экземпляр CacheManager для управления кешем
     * @see CacheManager
     * @see RedisCacheManager
     * @see RedisCacheConfiguration
     */
    @Bean
    public CacheManager cacheManager(
            final RedisConnectionFactory connectionFactory,
            final Jackson2JsonRedisSerializer<ProfileResponse>
                    profileResponseSerializer) {

        RedisCacheConfiguration profileConfig =
                RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(CACHE_TTL_MINUTES))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(
                                        new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(
                                        profileResponseSerializer));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(profileConfig)
                .withCacheConfiguration("profile", profileConfig)
                .transactionAware()
                .build();
    }
}
