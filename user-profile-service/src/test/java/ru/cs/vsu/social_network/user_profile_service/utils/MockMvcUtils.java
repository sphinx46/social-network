package ru.cs.vsu.social_network.user_profile_service.utils;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

public class MockMvcUtils {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    public MockMvcUtils(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    public ResultActions performPost(String url, Object request) throws Exception {
        return mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print());
    }

    public ResultActions performPut(String url, Object request) throws Exception {
        return mockMvc.perform(put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print());
    }


    public ResultActions performPatch(String url, Object request) throws Exception {
        return mockMvc.perform(patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print());
    }

    public ResultActions performPatch(String url) throws Exception {
        return mockMvc.perform(patch(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());
    }


    public ResultActions performGet(String url) throws Exception {
        return mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());
    }

    public ResultActions performGet(String url, String... headers) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get(url)
                .contentType(MediaType.APPLICATION_JSON);
        for (int i = 0; i < headers.length; i += 2) {
            if (i + 1 < headers.length) {
                requestBuilder = requestBuilder.header(headers[i], headers[i + 1]);
            }
        }
        return mockMvc.perform(requestBuilder).andDo(print());
    }

    public ResultActions performPost(String url, Object request, String... headers) throws Exception {
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

    public ResultActions performPut(String url, Object request, String... headers) throws Exception {
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

    public ResultActions performDelete(String url) throws Exception {
        return mockMvc.perform(delete(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());
    }

    public ResultActions performMultipart(String url, MockMultipartFile file) throws Exception {
        return mockMvc.perform(multipart(url)
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print());
    }
}
