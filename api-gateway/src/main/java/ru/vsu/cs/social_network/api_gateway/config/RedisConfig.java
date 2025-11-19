package ru.vsu.cs.social_network.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host:redis}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    /**
     * Создает фабрику подключений к Redis для хранения сессий и rate limiting.
     *
     * @return фабрика подключений к Redis
     */
    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisHost, redisPort);
        factory.setValidateConnection(true);
        factory.setShareNativeConnection(false);
        return factory;
    }

    /**
     * Создает реактивную фабрику подключений к Redis для rate limiting.
     * LettuceConnectionFactory реализует оба интерфейса (RedisConnectionFactory и ReactiveRedisConnectionFactory).
     *
     * @return реактивная фабрика подключений к Redis
     */
    @Bean
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisHost, redisPort);
        factory.setValidateConnection(true);
        factory.setShareNativeConnection(false);
        factory.afterPropertiesSet();
        return factory;
    }
}
