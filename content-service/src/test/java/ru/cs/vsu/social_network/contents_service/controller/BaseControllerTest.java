package ru.cs.vsu.social_network.contents_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.cs.vsu.social_network.contents_service.utils.MockMvcUtils;

/**
 * Базовый класс для stand-alone тестирования REST-контроллеров content-service.
 * Инкапсулирует создание {@link MockMvc} и утилит для упрощения написания тестов.
 */
public abstract class BaseControllerTest {

    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper;
    protected MockMvcUtils mockMvcUtils;

    /**
     * Возвращает тестируемый контроллер.
     *
     * @return экземпляр контроллера
     */
    protected abstract Object controllerUnderTest();

    /**
     * Возвращает список advice-классов, которые нужно подключить.
     *
     * @return массив advice-компонентов
     */
    protected Object[] controllerAdvices() {
        return new Object[0];
    }

    @BeforeEach
    void baseSetup() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(controllerUnderTest())
                .setControllerAdvice(controllerAdvices())
                .build();
        mockMvcUtils = new MockMvcUtils(mockMvc, objectMapper);
    }
}

