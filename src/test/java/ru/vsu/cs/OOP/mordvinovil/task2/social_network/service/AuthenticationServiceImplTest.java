package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.SignInRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.SignUpRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.JwtAuthenticationResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Role;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.security.filters.UserDetailsImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.AuthenticationServiceImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.JwtServiceImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.ProfileServiceImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.UserServiceImpl;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserServiceImpl userServiceImpl;

    @Mock
    private JwtServiceImpl jwtServiceImpl;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private ProfileServiceImpl profileServiceImpl;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationServiceImpl;

    @Test
    void signUp_ShouldReturnJwtToken() {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUsername("testuser");
        signUpRequest.setEmail("test@example.com");
        signUpRequest.setCity("Moscow");
        signUpRequest.setPassword("password123");

        User savedUser = createTestUser(1L, "testuser", "test@example.com");

        UserDetails userDetails = UserDetailsImpl.build(savedUser);

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userServiceImpl.create(any(User.class))).thenReturn(savedUser);
        when(jwtServiceImpl.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        JwtAuthenticationResponse response = authenticationServiceImpl.signUp(signUpRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        verify(passwordEncoder).encode("password123");
        verify(userServiceImpl).create(any(User.class));
        verify(profileServiceImpl).createDefaultProfile(savedUser);
        verify(jwtServiceImpl).generateToken(any(UserDetails.class));
    }

    @Test
    void signIn_ShouldReturnJwtToken() {
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setUsername("testuser");
        signInRequest.setPassword("password123");

        User user = createTestUser(1L, "testuser", "test@example.com");

        UserDetails userDetails = UserDetailsImpl.build(user);

        when(userServiceImpl.userDetailsService()).thenReturn(userDetailsService);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtServiceImpl.generateToken(userDetails)).thenReturn("jwt-token");

        JwtAuthenticationResponse response = authenticationServiceImpl.signIn(signInRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService).loadUserByUsername("testuser");
        verify(jwtServiceImpl).generateToken(userDetails);
    }

    @Test
    void signIn_ShouldCallAuthenticationManagerWithCorrectCredentials() {
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setUsername("testuser");
        signInRequest.setPassword("password123");

        User user = createTestUser(1L, "testuser", "test@example.com");

        UserDetails userDetails = UserDetailsImpl.build(user);

        when(userServiceImpl.userDetailsService()).thenReturn(userDetailsService);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtServiceImpl.generateToken(userDetails)).thenReturn("jwt-token");

        authenticationServiceImpl.signIn(signInRequest);

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("testuser", "password123")
        );
    }

    private User createTestUser(Long id, String username, String email) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("encodedPassword");
        user.setCity("Moscow");
        user.setRole(Role.ROLE_USER);
        user.setCreatedAt(LocalDateTime.now());
        user.setOnline(false);
        return user;
    }
}