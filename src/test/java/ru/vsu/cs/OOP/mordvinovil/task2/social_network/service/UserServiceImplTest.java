package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Role;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.custom.AccessDeniedException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.UserRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.user.UserServiceImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    @Test
    void create_ShouldSaveUserWhenUsernameAndEmailAreUnique() {
        User user = createTestUser(null, "newuser", "new@example.com");

        User savedUser = createTestUser(1L, "newuser", "new@example.com");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userServiceImpl.create(user);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("newuser", result.getUsername());
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void create_ShouldThrowExceptionWhenUsernameExists() {
        User user = createTestUser(null, "existinguser", "new@example.com");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userServiceImpl.create(user));
        assertEquals(ResponseMessageConstants.FAILURE_USER_WITH_NAME_ALREADY_EXISTS, exception.getMessage());
        verify(userRepository).existsByUsername("existinguser");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void create_ShouldThrowExceptionWhenEmailExists() {
        User user = createTestUser(null, "newuser", "existing@example.com");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userServiceImpl.create(user));
        assertEquals(ResponseMessageConstants.FAILURE_USER_WITH_EMAIL_ALREADY_EXISTS, exception.getMessage());
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getByUsername_ShouldReturnUserWhenExists() {
        User user = createTestUser(1L, "testuser", "test@example.com");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        User result = userServiceImpl.getByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals(1L, result.getId());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void getByUsername_ShouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userServiceImpl.getByUsername("nonexistent"));
        assertEquals(ResponseMessageConstants.FAILURE_USER_NOT_FOUND, exception.getMessage());
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void getById_ShouldReturnUserWhenExists() {
        User user = createTestUser(1L, "testuser", "test@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userServiceImpl.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    void getById_ShouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userServiceImpl.getById(999L));
        assertEquals("Пользователь с id 999 не найден", exception.getMessage());
        verify(userRepository).findById(999L);
    }

    @Test
    void getCurrentUser_ShouldReturnAuthenticatedUser() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        User user = createTestUser(1L, "currentuser", "current@example.com");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("currentuser");
        when(userRepository.findByUsername("currentuser")).thenReturn(Optional.of(user));

        User result = userServiceImpl.getCurrentUser();

        assertNotNull(result);
        assertEquals("currentuser", result.getUsername());
        verify(userRepository).findByUsername("currentuser");
    }

    @Test
    void getCurrentUser_ShouldThrowExceptionWhenNotAuthenticated() {
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(null);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> userServiceImpl.getCurrentUser());
        assertEquals("Пользователь не аутентифицирован", exception.getMessage());
    }

    @Test
    void userDetailsService_ShouldReturnUserDetailsService() {
        UserDetailsService result = userServiceImpl.userDetailsService();
        assertNotNull(result);
    }

    @Test
    void save_ShouldCallRepositorySave() {
        User user = createTestUser(1L, "testuser", "test@example.com");

        when(userRepository.save(user)).thenReturn(user);

        User result = userServiceImpl.save(user);

        assertNotNull(result);
        verify(userRepository).save(user);
    }

    @Test
    void getAdmin_ShouldSetAdminRole() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        User user = createTestUser(1L, "testuser", "test@example.com");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        userServiceImpl.getAdmin();

        assertEquals(Role.ROLE_ADMIN, user.getRole());
        verify(userRepository).save(user);
    }

    private User createTestUser(Long id, String username, String email) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("password");
        user.setCity("Moscow");
        user.setRole(Role.ROLE_USER);
        user.setCreatedAt(LocalDateTime.now());
        user.setOnline(false);
        return user;
    }
}