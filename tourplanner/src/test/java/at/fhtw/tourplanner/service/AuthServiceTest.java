package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.DTO.UserLoginRequestDTO;
import at.fhtw.tourplanner.entity.UserEntity;
import at.fhtw.tourplanner.repository.UserRepository;
import at.fhtw.tourplanner.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoginSuccess() {
        String username = "testuser";
        String password = "password123";
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setPasswordHash("encodedPassword");

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(passwordEncoder.matches(password, "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(username, user.getId())).thenReturn("jwt-token");

        String token = authService.loginUser(new UserLoginRequestDTO(username, password));

        assertNotNull(token);
        assertEquals("jwt-token", token);
        verify(userRepository, times(1)).findByUsername(username);
        verify(passwordEncoder, times(1)).matches(password, "encodedPassword");
    }

    @Test
    void testLoginFailureUserNotFound() {
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(null);

        assertThrows(ResponseStatusException.class, () ->
                authService.loginUser(new UserLoginRequestDTO(username, "password")));
    }

    @Test
    void testLoginFailureWrongPassword() {
        String username = "testuser";
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPasswordHash("encodedPassword");

        when(userRepository.findByUsername(username)).thenReturn(user);
        when(passwordEncoder.matches("wrong", "encodedPassword")).thenReturn(false);

        assertThrows(ResponseStatusException.class, () ->
                authService.loginUser(new UserLoginRequestDTO(username, "wrong")));
    }
}
