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

@WebMvcTest
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected JwtServiceImpl jwtServiceImpl;

    @Autowired
    protected WebApplicationContext context;

    protected MockMvcUtils mockMvcUtils;

    @BeforeEach
    void baseSetup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
        mockMvcUtils = new MockMvcUtils(mockMvc, objectMapper);
    }
}
