package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

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
                        .with(TestSecurityUtils.withCsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print());
    }

    public ResultActions performPut(String url, Object request) throws Exception {
        return mockMvc.perform(put(url)
                        .with(TestSecurityUtils.withCsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print());
    }


    public ResultActions performPatch(String url, Object request) throws Exception {
        return mockMvc.perform(patch(url)
                        .with(TestSecurityUtils.withCsrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print());
    }

    public ResultActions performPatch(String url) throws Exception {
        return mockMvc.perform(patch(url)
                        .with(TestSecurityUtils.withCsrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());
    }


    public ResultActions performGet(String url) throws Exception {
        return mockMvc.perform(get(url)
                        .with(TestSecurityUtils.withCsrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());
    }

    public ResultActions performDelete(String url) throws Exception {
        return mockMvc.perform(delete(url)
                        .with(TestSecurityUtils.withCsrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());
    }

    public ResultActions performMultipart(String url, MockMultipartFile file) throws Exception {
        return mockMvc.perform(multipart(url)
                        .file(file)
                        .with(TestSecurityUtils.withCsrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print());
    }

    public MockMultipartFile createMockImageFile() {
        return new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );
    }
}