package ru.cs.vsu.social_network.user_profile_service.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * Утилитный класс для упрощения тестирования Spring MVC контроллеров.
 * Инкапсулирует common операции с MockMvc и автоматически логирует результаты.
 */
public class MockMvcUtils {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    /**
     * Создает экземпляр утилиты с указанными MockMvc и ObjectMapper.
     */
    public MockMvcUtils(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    /**
     * Выполняет POST запрос с JSON телом и автоматическим логированием.
     */
    public ResultActions performPost(final String url, Object request) throws Exception {
        return mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print());
    }

    /**
     * Выполняет PUT запрос с JSON телом и автоматическим логированием.
     */
    public ResultActions performPut(final String url, Object request) throws Exception {
        return mockMvc.perform(put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print());
    }

    /**
     * Выполняет PATCH запрос с JSON телом и автоматическим логированием.
     */
    public ResultActions performPatch(final String url, Object request) throws Exception {
        return mockMvc.perform(patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print());
    }

    /**
     * Выполняет PATCH запрос без тела и с автоматическим логированием.
     */
    public ResultActions performPatch(final String url) throws Exception {
        return mockMvc.perform(patch(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());
    }

    /**
     * Выполняет GET запрос и автоматически логирует результат.
     */
    public ResultActions performGet(final String url) throws Exception {
        return mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());
    }

    /**
     * Выполняет GET запрос с указанными заголовками и автоматическим логированием.
     * Заголовки передаются парами: "Header-Name", "header-value"
     */
    public ResultActions performGet(final String url, String... headers) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get(url)
                .contentType(MediaType.APPLICATION_JSON);
        for (int i = 0; i < headers.length; i += 2) {
            if (i + 1 < headers.length) {
                requestBuilder = requestBuilder.header(headers[i], headers[i + 1]);
            }
        }
        return mockMvc.perform(requestBuilder).andDo(print());
    }

    /**
     * Выполняет POST запрос с JSON телом и указанными заголовками.
     * Заголовки передаются парами: "Header-Name", "header-value"
     */
    public ResultActions performPost(final String url, Object request, String... headers) throws Exception {
        var requestBuilder = post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request));
        for (int i = 0; i < headers.length; i += 2) {
            if (i + 1 < headers.length) {
                requestBuilder = requestBuilder.header(headers[i], headers[i + 1]);
            }
        }
        return mockMvc.perform(requestBuilder).andDo(print());
    }

    /**
     * Выполняет PUT запрос с JSON телом и указанными заголовками.
     * Заголовки передаются парами: "Header-Name", "header-value"
     */
    public ResultActions performPut(final String url, Object request, String... headers) throws Exception {
        var requestBuilder = put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request));
        for (int i = 0; i < headers.length; i += 2) {
            if (i + 1 < headers.length) {
                requestBuilder = requestBuilder.header(headers[i], headers[i + 1]);
            }
        }
        return mockMvc.perform(requestBuilder).andDo(print());
    }

    /**
     * Выполняет DELETE запрос и автоматически логирует результат.
     */
    public ResultActions performDelete(final String url) throws Exception {
        return mockMvc.perform(delete(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());
    }

    /**
     * Выполняет multipart запрос для загрузки файла и автоматически логирует результат.
     */
    public ResultActions performMultipart(final String url, MockMultipartFile file) throws Exception {
        return mockMvc.perform(multipart(url)
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print());
    }
}