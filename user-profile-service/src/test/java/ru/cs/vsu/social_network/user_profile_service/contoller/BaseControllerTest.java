package ru.cs.vsu.social_network.user_profile_service.contoller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.cs.vsu.social_network.user_profile_service.service.serviceImpl.JwtServiceImpl;
import ru.cs.vsu.social_network.user_profile_service.utils.MockMvcUtils;

/**
 * Базовый абстрактный класс для тестирования Spring MVC контроллеров.
 * Предоставляет общую конфигурацию MockMvc и утилиты для тестирования REST endpoints.
 *
 * Наследование от этого класса позволяет стандартизировать настройку тестов контроллеров
 * и избежать дублирования кода. Все поля protected и доступны наследникам для использования
 * в тестовых методах.
 */
@WebMvcTest
public abstract class BaseControllerTest {

    /**
     * MockMvc для выполнения HTTP запросов к контроллерам.
     * Инициализируется перед каждым тестом.
     */
    @Autowired
    protected MockMvc mockMvc;

    /**
     * ObjectMapper для сериализации и десериализации JSON.
     * Настроен Spring Boot автоматически.
     */
    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * Мок сервиса JWT аутентификации для тестирования защищенных endpoints.
     */
    @MockitoBean
    protected JwtServiceImpl jwtServiceImpl;

    /**
     * Контекст Spring приложения для настройки MockMvc.
     */
    @Autowired
    protected WebApplicationContext context;

    /**
     * Утилитный класс для упрощения работы с MockMvc.
     * Содержит common операции для тестирования REST API.
     */
    protected MockMvcUtils mockMvcUtils;

    /**
     * Базовая настройка тестового окружения перед каждым тестом.
     * Инициализирует MockMvc с контекстом приложения и создает экземпляр MockMvcUtils.
     *
     * При переопределении в наследниках всегда вызывайте super.baseSetup()
     * для корректной инициализации родительских компонентов.
     */
    @BeforeEach
    void baseSetup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
        mockMvcUtils = new MockMvcUtils(mockMvc, objectMapper);
    }
}