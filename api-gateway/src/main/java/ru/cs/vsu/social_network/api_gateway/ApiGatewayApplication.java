package ru.cs.vsu.social_network.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    private ApiGatewayApplication() {
        // Utility class
    }

    public static void main(final String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
