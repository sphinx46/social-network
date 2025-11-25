package ru.cs.vsu.social_network.user_profile_service.config;

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
     * Создает топик для событий загрузки аватаров.
     * Используется для передачи информации о загруженных аватарах от upload-service к user-profile-service.
     * В топик отправляются события после успешной загрузки аватара в хранилище.
     *
     * @return топик Kafka с именем "avatar.uploaded"
     */
    @Bean
    public NewTopic avatarUploadedTopic() {
        return TopicBuilder.name("avatar-uploaded")
                .build();
    }
}