package ru.cs.vsu.social_network.upload_service.config.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import ru.cs.vsu.social_network.upload_service.event.AvatarUploadedEvent;
import ru.cs.vsu.social_network.upload_service.event.PostImageUploadedEvent;
import ru.cs.vsu.social_network.upload_service.event.CommentImageUploadedEvent;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Создает конфигурацию для Kafka Producer с настройками надежности.
     * Настраивает сериализаторы, гарантии доставки и параметры повторных попыток.
     *
     * @return конфигурация Producer в виде Map с настройками сериализации и надежности
     */
    public Map<String, Object> producerConfig() {
        log.info("KAFKA_PRODUCER_CONFIG_ИНИЦИАЛИЗАЦИЯ: bootstrapServers={}", bootstrapServers);

        HashMap<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);

        log.info("KAFKA_PRODUCER_CONFIG_СОЗДАН: настройки={}", config.keySet());
        return config;
    }

    /**
     * Создает фабрику Producer'ов для событий загрузки аватаров.
     * Использует JsonSerializer для сериализации объектов AvatarUploadedEvent.
     * Гарантирует thread-safe создание экземпляров Producer.
     *
     * @return фабрика Producer'ов для AvatarUploadedEvent
     */
    @Bean
    public ProducerFactory<String, AvatarUploadedEvent> avatarUploadedEventProducerFactory() {
        log.info("KAFKA_PRODUCER_FACTORY_СОЗДАНИЕ: тип=AvatarUploadedEvent");

        Map<String, Object> config = producerConfig();
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * Создает KafkaTemplate для отправки событий загрузки аватаров.
     * Предоставляет высокоуровневый API для асинхронной отправки сообщений.
     * Поддерживает callback'и для обработки результатов отправки.
     *
     * @param producerFactory фабрика Producer'ов для создания экземпляров
     * @return настроенный KafkaTemplate для работы с AvatarUploadedEvent
     */
    @Bean
    public KafkaTemplate<String, AvatarUploadedEvent> avatarUploadedEventKafkaTemplate(
            final ProducerFactory<String, AvatarUploadedEvent> producerFactory) {
        log.info("KAFKA_TEMPLATE_СОЗДАНИЕ: тип=AvatarUploadedEvent");
        return new KafkaTemplate<>(producerFactory);
    }

    /**
     * Создает фабрику Producer'ов для событий загрузки изображений постов.
     * Использует JsonSerializer для сериализации объектов PostImageUploadedEvent.
     * Гарантирует thread-safe создание экземпляров Producer.
     *
     * @return фабрика Producer'ов для PostImageUploadedEvent
     */
    @Bean
    public ProducerFactory<String, PostImageUploadedEvent> postImageUploadedEventProducerFactory() {
        log.info("KAFKA_PRODUCER_FACTORY_СОЗДАНИЕ: тип=PostImageUploadedEvent");

        Map<String, Object> config = producerConfig();
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * Создает KafkaTemplate для отправки событий загрузки изображений постов.
     * Предоставляет высокоуровневый API для асинхронной отправки сообщений.
     * Поддерживает callback'и для обработки результатов отправки.
     *
     * @param producerFactory фабрика Producer'ов для создания экземпляров
     * @return настроенный KafkaTemplate для работы с PostImageUploadedEvent
     */
    @Bean
    public KafkaTemplate<String, PostImageUploadedEvent> postImageUploadedEventKafkaTemplate(
            final ProducerFactory<String, PostImageUploadedEvent> producerFactory) {
        log.info("KAFKA_TEMPLATE_СОЗДАНИЕ: тип=PostImageUploadedEvent");
        return new KafkaTemplate<>(producerFactory);
    }

    /**
     * Создает фабрику Producer'ов для событий загрузки изображений комментариев.
     * Использует JsonSerializer для сериализации объектов CommentImageUploadedEvent.
     * Гарантирует thread-safe создание экземпляров Producer.
     *
     * @return фабрика Producer'ов для CommentImageUploadedEvent
     */
    @Bean
    public ProducerFactory<String, CommentImageUploadedEvent> commentImageUploadedEventProducerFactory() {
        log.info("KAFKA_PRODUCER_FACTORY_СОЗДАНИЕ: тип=CommentImageUploadedEvent");

        Map<String, Object> config = producerConfig();
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * Создает KafkaTemplate для отправки событий загрузки изображений комментариев.
     * Предоставляет высокоуровневый API для асинхронной отправки сообщений.
     * Поддерживает callback'и для обработки результатов отправки.
     *
     * @param producerFactory фабрика Producer'ов для создания экземпляров
     * @return настроенный KafkaTemplate для работы с CommentImageUploadedEvent
     */
    @Bean
    public KafkaTemplate<String, CommentImageUploadedEvent> commentImageUploadedEventKafkaTemplate(
            final ProducerFactory<String, CommentImageUploadedEvent> producerFactory) {
        log.info("KAFKA_TEMPLATE_СОЗДАНИЕ: тип=CommentImageUploadedEvent");
        return new KafkaTemplate<>(producerFactory);
    }
}