package ru.cs.vsu.social_network.eureka_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public final class EurekaServerApplication {

    /**
     * Приватный конструктор для предотвращения создания экземпляров.
     */
    private EurekaServerApplication() {
        // Utility class
    }

    /**
     * Точка входа в приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(final String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
