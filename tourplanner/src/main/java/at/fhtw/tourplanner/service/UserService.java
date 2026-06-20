package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.DTO.ChangePasswordRequestDTO;
import at.fhtw.tourplanner.DTO.UpdateProfileRequestDTO;
import at.fhtw.tourplanner.DTO.UserProfileResponseDTO;
import at.fhtw.tourplanner.DTO.UserRegisterRequestDTO;
import at.fhtw.tourplanner.entity.UserEntity;
import at.fhtw.tourplanner.exception.ResourceNotFoundException;
import at.fhtw.tourplanner.repository.UserRepository;
import at.fhtw.tourplanner.util.LoggerUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    UserRepository userRepository;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public void registerUser(UserRegisterRequestDTO dto) {
        if (!dto.password().equals(dto.passwordConfirmation())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Passwords do not match. Please ensure both password fields contain the same value.");
        }
        if (userRepository.existsByUsername(dto.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Username '" + dto.username() + "' already exists. Please choose a different username.");
        }
        if (userRepository.existsByEmail(dto.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Email '" + dto.email() + "' already exists. Please use a different email address.");
        }

        UserEntity user = new UserEntity();
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        user.setRegistrationDate(LocalDateTime.now());
        userRepository.save(user);
        log.info("User registered successfully: {}", dto.username());
    }


    public UserProfileResponseDTO getUserProfile(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        log.debug("Fetched profile for user: {}", user.getUsername());

        return new UserProfileResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRegistrationDate()
        );
    }


    @Transactional
    public UserProfileResponseDTO updateUserProfile(UUID userId, UpdateProfileRequestDTO dto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Check if the new username is already taken by another user
        if (!user.getUsername().equals(dto.username()) && userRepository.existsByUsername(dto.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Username '" + dto.username() + "' is already taken. Please choose a different username.");
        }

        // Check if the new email is already taken by another user
        if (!user.getEmail().equals(dto.email()) && userRepository.existsByEmail(dto.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Email '" + dto.email() + "' is already registered. Please use a different email address.");
        }

        // Apply updates
        user.setUsername(dto.username());
        user.setEmail(dto.email());

        UserEntity updatedUser = userRepository.save(user);
        log.info("Profile updated for user: {}", updatedUser.getUsername());

        return new UserProfileResponseDTO(
                updatedUser.getId(),
                updatedUser.getUsername(),
                updatedUser.getEmail(),
                updatedUser.getRegistrationDate()
        );
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequestDTO dto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Verify current password
        if (!passwordEncoder.matches(dto.oldPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Current password is incorrect. Please try again.");
        }

        // Encode and set the new password
        String encodedNewPassword = passwordEncoder.encode(dto.newPassword());
        user.setPasswordHash(encodedNewPassword);
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", user.getUsername());
    }
}