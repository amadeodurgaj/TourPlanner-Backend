package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.DTO.ChangePasswordRequestDTO;
import at.fhtw.tourplanner.DTO.CurrentUserDTO;
import at.fhtw.tourplanner.DTO.UpdateProfileRequestDTO;
import at.fhtw.tourplanner.DTO.UserRegisterRequestDTO;
import at.fhtw.tourplanner.entity.UserEntity;
import at.fhtw.tourplanner.exception.ResourceNotFoundException;
import at.fhtw.tourplanner.repository.UserRepository;
import at.fhtw.tourplanner.util.LoggerUtil;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
public class UserService {

    private static final Logger log = LoggerUtil.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // registration function, checks are done in the dto
    @Transactional
    public void registerUser(UserRegisterRequestDTO dto) {
        if (!dto.password().equals(dto.passwordConfirmation())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match. Please ensure both password fields contain the same value.");
        }
        if (userRepository.existsByUsername(dto.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username '" + dto.username() + "' already exists. Please choose a different username.");
        }
        if (userRepository.existsByEmail(dto.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email '" + dto.email() + "' already exists. Please use a different email address.");
        }

        UserEntity user = new UserEntity();
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        user.setRegistrationDate(LocalDateTime.now());
        userRepository.save(user);
        log.info("User registered successfully: {}", dto.username());
    }

    @Transactional
    public CurrentUserDTO updateProfile(UUID userId, UpdateProfileRequestDTO dto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!user.getUsername().equals(dto.username()) && userRepository.existsByUsername(dto.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username '" + dto.username() + "' already exists.");
        }
        if (!user.getEmail().equals(dto.email()) && userRepository.existsByEmail(dto.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email '" + dto.email() + "' already exists.");
        }

        user.setUsername(dto.username());
        user.setEmail(dto.email());
        userRepository.save(user);
        log.info("Profile updated for user: {} (ID: {})", dto.username(), userId);

        return new CurrentUserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getRegistrationDate(), null);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequestDTO dto) {
        if (!dto.newPassword().equals(dto.confirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New passwords do not match.");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!passwordEncoder.matches(dto.oldPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect.");
        }

        user.setPasswordHash(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {} (ID: {})", user.getUsername(), userId);
    }
}
