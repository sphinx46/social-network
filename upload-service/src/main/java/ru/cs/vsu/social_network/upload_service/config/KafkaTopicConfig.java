package ru.cs.vsu.social_network.upload_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic avatarUploadedTopic() {
        return TopicBuilder.name("avatar.events")
                .build();
    }
}
