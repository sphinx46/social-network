package ru.cs.vsu.social_network.eureka_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public final class EurekaServerApplication {

    private EurekaServerApplication() {
        // Utility class
    }

    public static void main(final String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
