package ru.cs.vsu.social_network.upload_service.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.util.Map;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * Небольшой набор вспомогательных методов для выполнения HTTP-запросов
 * в тестах контроллеров. Позволяет избегать дублирования однотипного кода
 * при настройке {@link MockMvc} и сериализации тел запросов.
 */
public final class MockMvcUtils {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    public MockMvcUtils(final MockMvc mockMvc, final ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    /**
     * Выполняет GET-запрос с произвольными заголовками.
     *
     * @param url     относительный путь
     * @param headers заголовки, передаваемые парами "ключ-значение"
     * @return результат выполнения запроса
     */
    public ResultActions performGet(final String url, final String... headers) throws Exception {
        MockHttpServletRequestBuilder builder = get(url).contentType(MediaType.APPLICATION_JSON);
        return mockMvc.perform(applyHeaders(builder, headers)).andDo(print());
    }

    /**
     * Выполняет DELETE-запрос.
     *
     * @param url     относительный путь
     * @param headers дополнительные заголовки
     * @return результат выполнения запроса
     */
    public ResultActions performDelete(final String url, final String... headers) throws Exception {
        MockHttpServletRequestBuilder builder = delete(url).contentType(MediaType.APPLICATION_JSON);
        return mockMvc.perform(applyHeaders(builder, headers)).andDo(print());
    }

    /**
     * Выполняет POST-запрос с JSON телом.
     *
     * @param url     относительный путь
     * @param body    объект, который будет сериализован в JSON
     * @param headers дополнительные заголовки
     * @return результат выполнения запроса
     */
    public ResultActions performPost(final String url, final Object body, final String... headers) throws Exception {
        MockHttpServletRequestBuilder builder = post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
        return mockMvc.perform(applyHeaders(builder, headers)).andDo(print());
    }

    /**
     * Выполняет PUT-запрос с JSON телом.
     *
     * @param url     относительный путь
     * @param body    объект для сериализации
     * @param headers дополнительные заголовки
     * @return результат выполнения запроса
     */
    public ResultActions performPut(final String url, final Object body, final String... headers) throws Exception {
        MockHttpServletRequestBuilder builder = put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
        return mockMvc.perform(applyHeaders(builder, headers)).andDo(print());
    }

    /**
     * Выполняет multipart-запрос с передачей файла и параметров формы.
     *
     * @param url     относительный путь
     * @param file    загружаемый файл
     * @param params  дополнительные параметры формы
     * @param headers дополнительные заголовки
     * @return результат выполнения запроса
     */
    public ResultActions performMultipart(final String url,
                                          final MockMultipartFile file,
                                          final Map<String, String> params,
                                          final String... headers) throws Exception {
        MockMultipartHttpServletRequestBuilder builder =
                (MockMultipartHttpServletRequestBuilder) MockMvcRequestBuilders.multipart(url)
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA);
        params.forEach(builder::param);
        for (int i = 0; i + 1 < headers.length; i += 2) {
            builder.header(headers[i], headers[i + 1]);
        }
        return mockMvc.perform(builder).andDo(print());
    }

    private MockHttpServletRequestBuilder applyHeaders(final MockHttpServletRequestBuilder builder,
                                                       final String... headers) {
        for (int i = 0; i + 1 < headers.length; i += 2) {
            builder.header(headers[i], headers[i + 1]);
        }
        return builder;
    }
}

