package ru.vsu.cs.social_network.api_gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Slf4j
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host:redis}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    /**
     * Создает фабрику подключений к Redis для хранения сессий.
     *
     * @return фабрика подключений к Redis
     */
    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        log.info("ШЛЮЗ_РЕДИС_НАСТРОЙКА_НАЧАЛО: настройка Redis connection factory для host: {}, port: {}", redisHost, redisPort);
        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisHost, redisPort);
        factory.setValidateConnection(true);
        factory.setShareNativeConnection(false);
        log.info("ШЛЮЗ_РЕДИС_НАСТРОЙКА_УСПЕХ: Redis connection factory настроен");
        return factory;
    }
}
