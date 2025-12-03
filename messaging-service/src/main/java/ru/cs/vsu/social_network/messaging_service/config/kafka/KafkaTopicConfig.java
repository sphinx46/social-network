package ru.cs.vsu.social_network.messaging_service.config.kafka;

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

    /**
     * Создает топик для событий загрузки изображений в сообщениях.
     * Используется для передачи информации о загруженных изображениях от upload-service к messaging-service.
     * В топик отправляются события после успешной загрузки изображения в хранилище.
     *
     * @return топик Kafka с именем "messaging-image-uploaded"
     */
    @Bean
    public NewTopic messagingImageUploadedTopic() {
        return TopicBuilder.name("messaging-image-uploaded")
                .build();
    }
}
