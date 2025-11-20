package ru.cs.vsu.social_network.user_profile_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class UserProfileServiceApplication {

    /**
     * Приватный конструктор для предотвращения создания экземпляров.
     */
    private UserProfileServiceApplication() {
        // Utility class
    }

    /**
     * Точка входа в приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(final String[] args) {
        SpringApplication.run(UserProfileServiceApplication.class, args);
    }
}
