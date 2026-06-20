package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.DTO.ForgotPasswordRequestDTO;
import at.fhtw.tourplanner.DTO.ForgotPasswordResponseDTO;
import at.fhtw.tourplanner.DTO.ResetPasswordRequestDTO;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
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
        ReflectionTestUtils.setField(authService, "frontendUrls", List.of("http://localhost:3000"));
        ReflectionTestUtils.setField(authService, "passwordResetExpirationMinutes", 30L);
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

    @Test
    void testForgotPasswordGeneratesResetUrlForExistingUser() {
        UserEntity user = new UserEntity();
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(user);

        ForgotPasswordResponseDTO response = authService.forgotPassword(
                new ForgotPasswordRequestDTO("test@example.com"));

        assertNotNull(response.resetUrl());
        assertTrue(response.resetUrl().startsWith("http://localhost:3000/reset-password/"));
        assertNotNull(response.expiresAt());
        assertNotNull(user.getPasswordResetTokenHash());
        assertEquals(64, user.getPasswordResetTokenHash().length());
        assertNotNull(user.getPasswordResetTokenExpiresAt());
        verify(userRepository).save(user);
    }

    @Test
    void testForgotPasswordDoesNotRevealUnknownEmail() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(null);

        ForgotPasswordResponseDTO response = authService.forgotPassword(
                new ForgotPasswordRequestDTO("missing@example.com"));

        assertNull(response.resetUrl());
        assertNull(response.expiresAt());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testResetPasswordSuccess() {
        UserEntity user = new UserEntity();
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        ForgotPasswordResponseDTO response = authService.forgotPassword(
                new ForgotPasswordRequestDTO("test@example.com"));
        String token = response.resetUrl().substring(response.resetUrl().lastIndexOf("/") + 1);

        when(userRepository.findByPasswordResetTokenHash(anyString())).thenReturn(user);
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");

        authService.resetPassword(new ResetPasswordRequestDTO(token, "newPassword123", "newPassword123"));

        assertEquals("encodedNewPassword", user.getPasswordHash());
        assertNull(user.getPasswordResetTokenHash());
        assertNull(user.getPasswordResetTokenExpiresAt());
        verify(userRepository, times(2)).save(user);
    }

    @Test
    void testResetPasswordInvalidTokenThrows() {
        when(userRepository.findByPasswordResetTokenHash(anyString())).thenReturn(null);

        assertThrows(ResponseStatusException.class, () ->
                authService.resetPassword(new ResetPasswordRequestDTO("bad-token", "newPassword123", "newPassword123")));
    }
}
