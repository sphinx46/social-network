package ru.cs.vsu.social_network.messaging_service.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.cs.vsu.social_network.messaging_service.config.filters.HeaderSignatureFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final HeaderSignatureFilter headerSignatureFilter;

    /**
     * Настраивает цепочку фильтров безопасности.
     *
     * @param http конфигурация HttpSecurity
     * @return подготовленная цепочка фильтров
     * @throws Exception при ошибке конфигурации
     */
    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        log.info("БЕЗОПАСНОСТЬ_НАСТРОЙКА: старт конфигурации SecurityFilterChain");
        SecurityFilterChain chain = http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(registry -> registry
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api-docs/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/ws-messaging/**").permitAll()
                        .requestMatchers("/api/messaging/test/**").permitAll()
                        .anyRequest().authenticated()
                )

                .addFilterBefore(headerSignatureFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
        log.info("БЕЗОПАСНОСТЬ_НАСТРОЙКА: конфигурация SecurityFilterChain завершена");
        return chain;
    }
}

