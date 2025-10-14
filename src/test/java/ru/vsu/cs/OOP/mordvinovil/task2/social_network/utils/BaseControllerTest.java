package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.JwtService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.UserService;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@WebMvcTest
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected UserService userService;

    @MockitoBean
    protected JwtService jwtService;

    @Autowired
    protected WebApplicationContext context;

    protected MockMvcUtils mockMvcUtils;
    protected User testUser;

    @BeforeEach
    void baseSetup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        mockMvcUtils = new MockMvcUtils(mockMvc, objectMapper);
        testUser = TestDataFactory.createTestUser();
        when(userService.getCurrentUser()).thenReturn(testUser);
    }

    protected void setupUserService(User user) {
        when(userService.getCurrentUser()).thenReturn(user);
    }

    protected User createTestUser(Long id, String username) {
        return TestDataFactory.createTestUser(id, username);
    }
}