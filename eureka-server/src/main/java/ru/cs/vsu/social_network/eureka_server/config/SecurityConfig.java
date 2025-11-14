package ru.cs.vsu.social_network.eureka_server.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("EUREKA_БЕЗОПАСНОСТЬ_НАСТРОЙКА_НАЧАЛО: настройка Security Filter Chain");
        SecurityFilterChain chain = http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
        log.info("EUREKA_БЕЗОПАСНОСТЬ_НАСТРОЙКА_УСПЕХ: Security Filter Chain настроен");
        return chain;
    }
}
