package ru.vsu.cs.OOP.mordvinovil.task2.social_network.contoller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.user.AuthenticationService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.MockMvcUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

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

        mockMvcUtils.performPost("/auth/sign-up", request)
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

        mockMvcUtils.performPost("/auth/sign-up", request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Авторизация пользователя - успешный сценарий")
    void signIn_Success() throws Exception {
        var request = TestDataFactory.createSignInRequest();
        var response = TestDataFactory.createJwtResponse();

        when(authenticationService.signIn(any())).thenReturn(response);

        mockMvcUtils.performPost("/auth/sign-in", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token"));
    }

    @Test
    @DisplayName("Авторизация пользователя - невалидные данные")
    void signIn_InvalidData() throws Exception {
        var request = TestDataFactory.createSignInRequest();
        request.setUsername("test");
        request.setPassword("short");

        mockMvcUtils.performPost("/auth/sign-in", request)
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

        mockMvcUtils.performPost("/auth/sign-up", request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Авторизация пользователя - пустые поля")
    void signIn_EmptyFields() throws Exception {
        var request = TestDataFactory.createSignInRequest();
        request.setUsername("");
        request.setPassword("");

        mockMvcUtils.performPost("/auth/sign-in", request)
                .andExpect(status().isBadRequest());
    }
}