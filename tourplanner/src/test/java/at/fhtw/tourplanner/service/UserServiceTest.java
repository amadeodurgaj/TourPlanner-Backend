package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.DTO.UserRegisterRequestDTO;
import at.fhtw.tourplanner.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser() {
        UserRegisterRequestDTO dto = new UserRegisterRequestDTO(
                "newuser", "new@test.com", "password123", "password123");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");

        userService.registerUser(dto);

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void testRegisterDuplicateUsername() {
        UserRegisterRequestDTO dto = new UserRegisterRequestDTO(
                "existing", "test@test.com", "password123", "password123");

        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> userService.registerUser(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegisterDuplicateEmail() {
        UserRegisterRequestDTO dto = new UserRegisterRequestDTO(
                "newuser", "existing@test.com", "password123", "password123");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> userService.registerUser(dto));
        verify(userRepository, never()).save(any());
    }
}
