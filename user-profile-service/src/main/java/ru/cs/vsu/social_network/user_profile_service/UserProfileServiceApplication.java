package ru.cs.vsu.social_network.user_profile_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class UserProfileServiceApplication {

    private UserProfileServiceApplication() {
    }

    public static void main(final String[] args) {
        SpringApplication.run(UserProfileServiceApplication.class, args);
    }
}
