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

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private ProfileService profileService;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthenticationService authenticationService;

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
        when(userService.create(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        JwtAuthenticationResponse response = authenticationService.signUp(signUpRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        verify(passwordEncoder).encode("password123");
        verify(userService).create(any(User.class));
        verify(profileService).createDefaultProfile(savedUser);
        verify(jwtService).generateToken(any(UserDetails.class));
    }

    @Test
    void signIn_ShouldReturnJwtToken() {
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setUsername("testuser");
        signInRequest.setPassword("password123");

        User user = createTestUser(1L, "testuser", "test@example.com");

        UserDetails userDetails = UserDetailsImpl.build(user);

        when(userService.userDetailsService()).thenReturn(userDetailsService);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        JwtAuthenticationResponse response = authenticationService.signIn(signInRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService).loadUserByUsername("testuser");
        verify(jwtService).generateToken(userDetails);
    }

    @Test
    void signIn_ShouldCallAuthenticationManagerWithCorrectCredentials() {
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setUsername("testuser");
        signInRequest.setPassword("password123");

        User user = createTestUser(1L, "testuser", "test@example.com");

        UserDetails userDetails = UserDetailsImpl.build(user);

        when(userService.userDetailsService()).thenReturn(userDetailsService);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        authenticationService.signIn(signInRequest);

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