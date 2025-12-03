package ru.cs.vsu.social_network.messaging_service.config;


import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import ru.cs.vsu.social_network.messaging_service.event.MessageImageUploadedEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Конфигурационный класс для настройки Kafka Consumer.
 * Создает и настраивает бины для обработки событий из Kafka.
 * Обеспечивает надежное потребление сообщений с обработкой ошибок десериализации.
 */
@Slf4j
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Создает конфигурацию для Kafka Consumer с настройками надежности.
     * Настраивает десериализаторы, стратегии обработки ошибок и параметры потребления.
     * Использует ErrorHandlingDeserializer для безопасной обработки некорректных сообщений.
     *
     * @param clazz класс события для десериализации
     * @return конфигурация Consumer в виде Map с настройками десериализации и надежности
     */
    private <T> Map<String, Object> consumerConfig(Class<T> clazz) {
        log.info("KAFKA_CONSUMER_CONFIG_ИНИЦИАЛИЗАЦИЯ: bootstrapServers={}, eventType={}",
                bootstrapServers, clazz.getSimpleName());

        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        config.put(ConsumerConfig.GROUP_ID_CONFIG, "messaging-service-group");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        config.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        log.info("KAFKA_CONSUMER_CONFIG_СОЗДАН: настройки={}", config.keySet());
        return config;
    }

    /**
     * Создает фабрику Consumer'ов для указанного типа событий.
     * Настраивает десериализацию JSON с обработкой ошибок и доверенными пакетами.
     * Гарантирует типобезопасность при обработке входящих сообщений.
     *
     * @param clazz класс события для десериализации
     * @return фабрика Consumer'ов для указанного типа событий
     */
    private <T> ConsumerFactory<String, T> createConsumerFactory(Class<T> clazz) {
        log.info("KAFKA_CONSUMER_FACTORY_СОЗДАНИЕ: eventType={}", clazz.getSimpleName());

        JsonDeserializer<T> jsonDeserializer = new JsonDeserializer<>(clazz);
        jsonDeserializer.addTrustedPackages("*");
        jsonDeserializer.setUseTypeMapperForKey(false);

        ErrorHandlingDeserializer<T> errorHandlingDeserializer =
                new ErrorHandlingDeserializer<>(jsonDeserializer);

        return new DefaultKafkaConsumerFactory<>(
                consumerConfig(clazz),
                new StringDeserializer(),
                errorHandlingDeserializer
        );
    }

    /**
     * Создает фабрику контейнеров слушателей для конкурентной обработки сообщений.
     * Настраивает параллельную обработку сообщений с поддержкой нескольких потоков.
     * Обеспечивает управление жизненным циклом слушателей сообщений.
     *
     * @param clazz класс события для десериализации
     * @return фабрика контейнеров слушателей для конкурентной обработки
     */
    private <T> KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, T>>
    createFactory(Class<T> clazz) {
        log.info("KAFKA_LISTENER_FACTORY_СОЗДАНИЕ: eventType={}", clazz.getSimpleName());

        ConcurrentKafkaListenerContainerFactory<String, T> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(createConsumerFactory(clazz));

        return factory;
    }

    /**
     * Создает KafkaListenerContainerFactory для обработки событий загрузки изображений в сообщениях.
     * Предоставляет высокоуровневый API для асинхронного потребления сообщений.
     * Поддерживает конкурентную обработку событий с гарантией доставки.
     *
     * @return настроенный KafkaListenerContainerFactory для работы с MessageImageUploadedEvent
     */
    @Bean
    public KafkaListenerContainerFactory
            <ConcurrentMessageListenerContainer<String, MessageImageUploadedEvent>>
    kafkaListenerContainerFactoryMessageImageUpload() {
        log.info("KAFKA_MESSAGE_IMAGE_LISTENER_FACTORY_СОЗДАНИЕ: " +
                "инициализация фабрики для MessageImageUploadedEvent");
        return createFactory(MessageImageUploadedEvent.class);
    }
}
