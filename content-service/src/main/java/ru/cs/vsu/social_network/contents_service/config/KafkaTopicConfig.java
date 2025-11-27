package ru.cs.vsu.social_network.contents_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Конфигурационный класс для настройки топиков Kafka.
 * Определяет топики, используемые для обмена событиями между микросервисами.
 */
@Configuration
public class KafkaTopicConfig {

    private static final String POST_IMAGE_TOPIC = "post-image-uploaded";

    /**
     * Создает топик для событий загрузки изображений постов.
     * Используется для передачи информации о загруженных изображениях постов от upload-service к content-service.
     * В топик отправляются события после успешной загрузки изображения поста в хранилище.
     *
     * @return топик Kafka с именем "post-image-uploaded"
     */
    @Bean
    public NewTopic postImageUploadedTopic() {
        return TopicBuilder.name(POST_IMAGE_TOPIC)
                .build();
    }
}