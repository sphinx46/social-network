package ru.cs.vsu.social_network.upload_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    private static final String AVATAR_TOPIC = "avatar-uploaded";
    private static final String POST_IMAGE_TOPIC = "post-image-uploaded";

    @Bean
    public NewTopic avatarUploadedTopic() {
        return TopicBuilder.name(AVATAR_TOPIC)
                .build();
    }

    @Bean
    public NewTopic postImageUploadedTopic() {
        return TopicBuilder.name(POST_IMAGE_TOPIC)
                .build();
    }
}
