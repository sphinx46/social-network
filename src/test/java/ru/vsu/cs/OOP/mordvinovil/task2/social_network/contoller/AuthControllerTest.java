package ru.vsu.cs.OOP.mordvinovil.task2.social_network.contoller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller.auth.AuthController;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.user.AuthenticationService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.BaseControllerTest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.MockMvcUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestSecurityConfig;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class AuthControllerTest extends BaseControllerTest  {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    private MockMvcUtils mockMvcUtils;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        mockMvcUtils = new MockMvcUtils(mockMvc, objectMapper);
    }

    @Test
    @DisplayName("Регистрация пользователя - успешный сценарий")
    void signUp_Success() throws Exception {
        var request = TestDataFactory.createSignUpRequest();
        var response = TestDataFactory.createJwtResponse();

        when(authenticationService.signUp(any())).thenReturn(response);

        mockMvc.perform(post("/auth/sign-up")
                        .with(csrf())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token"));
    }

    @Test
    @DisplayName("Регистрация пользователя - невалидные данные")
    void signUp_InvalidData() throws Exception {
        var request = TestDataFactory.createSignUpRequest();
        request.setUsername("test");
        request.setEmail("invalid-email");
        request.setCity("");
        request.setPassword("short");

        mockMvc.perform(post("/auth/sign-up")
                        .with(csrf())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Авторизация пользователя - успешный сценарий")
    void signIn_Success() throws Exception {
        var request = TestDataFactory.createSignInRequest();
        var response = TestDataFactory.createJwtResponse();

        when(authenticationService.signIn(any())).thenReturn(response);

        mockMvc.perform(post("/auth/sign-in")
                        .with(csrf())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token"));
    }

    @Test
    @DisplayName("Авторизация пользователя - невалидные данные")
    void signIn_InvalidData() throws Exception {
        var request = TestDataFactory.createSignInRequest();
        request.setUsername("test");
        request.setPassword("short");

        mockMvc.perform(post("/auth/sign-in")
                        .with(csrf())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Регистрация пользователя - пустые поля")
    void signUp_EmptyFields() throws Exception {
        var request = TestDataFactory.createSignUpRequest();
        request.setUsername("");
        request.setEmail("");
        request.setCity("");
        request.setPassword("");

        mockMvc.perform(post("/auth/sign-up")
                        .with(csrf())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Авторизация пользователя - пустые поля")
    void signIn_EmptyFields() throws Exception {
        var request = TestDataFactory.createSignInRequest();
        request.setUsername("");
        request.setPassword("");

        mockMvc.perform(post("/auth/sign-in")
                        .with(csrf())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}