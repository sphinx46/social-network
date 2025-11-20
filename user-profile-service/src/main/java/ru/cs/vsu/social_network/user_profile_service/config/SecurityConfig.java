package ru.cs.vsu.social_network.user_profile_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.cs.vsu.social_network.user_profile_service.config.filters.HeaderSignatureFilter;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private HeaderSignatureFilter headerSignatureFilter;

    /**
     * Настраивает Security Filter Chain для user-profile-service.
     * Отключает CSRF и разрешает доступ ко всем запросам,
     * так как валидация выполняется в api-gateway.
     *
     * @param http объект для настройки безопасности
     * @return настроенная цепочка фильтров безопасности
     * @throws Exception при ошибке настройки
     */
    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http)
            throws Exception {
        log.info("ПРОФИЛЬ_БЕЗОПАСНОСТЬ_НАСТРОЙКА_НАЧАЛО: "
                + "настройка Security Filter Chain");
        SecurityFilterChain chain = http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/profile/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/profile/**")
                        .permitAll()
                        .requestMatchers("/swagger-ui/**",
                                "/swagger-ui.html").permitAll()
                        .requestMatchers("/api-docs/**",
                                "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(headerSignatureFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .build();
        log.info("ПРОФИЛЬ_БЕЗОПАСНОСТЬ_НАСТРОЙКА_УСПЕХ: "
                + "Security Filter Chain настроен");
        return chain;
    }
}
