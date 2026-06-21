package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.DTO.CurrentUserDTO;
import at.fhtw.tourplanner.DTO.ForgotPasswordRequestDTO;
import at.fhtw.tourplanner.DTO.ForgotPasswordResponseDTO;
import at.fhtw.tourplanner.DTO.ResetPasswordRequestDTO;
import at.fhtw.tourplanner.DTO.UserLoginRequestDTO;
import at.fhtw.tourplanner.entity.UserEntity;
import at.fhtw.tourplanner.repository.UserRepository;
import at.fhtw.tourplanner.util.JwtUtil;
import at.fhtw.tourplanner.util.LoggerUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

@Service
public class AuthService {

    private static final Logger log = LoggerUtil.getLogger(AuthService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${app.frontend.url}")
    private List<String> frontendUrls;

    @Value("${app.password-reset.expiration-minutes:30}")
    private long passwordResetExpirationMinutes;

    public AuthService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String loginUser(UserLoginRequestDTO dto) {
        UserEntity user = userRepository.findByUsername(dto.username());
        if (user == null) {
            log.warn("Login attempt for non-existent user: {}", dto.username());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        log.debug("User found for username: {}", dto.username());
        if (!passwordEncoder.matches(dto.password(), user.getPasswordHash())) {
            log.warn("Invalid password for user: {}", dto.username());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        return jwtUtil.generateToken(user.getUsername(), user.getId());
    }

    @Transactional
    public ForgotPasswordResponseDTO forgotPassword(ForgotPasswordRequestDTO dto) {
        UserEntity user = userRepository.findByEmail(dto.email());
        if (user == null) {
            log.info("Password reset requested for unknown email: {}", dto.email());
            return new ForgotPasswordResponseDTO(null, null);
        }

        String token = generateResetToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(passwordResetExpirationMinutes);

        user.setPasswordResetTokenHash(hashToken(token));
        user.setPasswordResetTokenExpiresAt(expiresAt);
        userRepository.save(user);

        String resetUrl = buildResetUrl(token);
        log.info("Password reset token generated for user: {}", user.getUsername());
        log.debug("Password reset URL for {}: {}", user.getEmail(), resetUrl);

        return new ForgotPasswordResponseDTO(resetUrl, expiresAt);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequestDTO dto) {
        UserEntity user = userRepository.findByPasswordResetTokenHash(hashToken(dto.token()));
        if (user == null || user.getPasswordResetTokenExpiresAt() == null
                || user.getPasswordResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired password reset token");
        }

        user.setPasswordHash(passwordEncoder.encode(dto.newPassword()));
        user.setPasswordResetTokenHash(null);
        user.setPasswordResetTokenExpiresAt(null);
        userRepository.save(user);

        log.info("Password reset completed for user: {}", user.getUsername());
    }

    public CurrentUserDTO getCurrentUser(String token) {
        String username = jwtUtil.extractUsername(token);
        if (username == null || username.isEmpty()) {
            log.warn("Invalid token: username is null or empty");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token: username cannot be extracted");
        }
        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            log.warn("Current user not found for token username: {}", username);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found for the provided token");
        }
        return new CurrentUserDTO(user.getId(), user.getUsername(), user.getEmail(), user.getRegistrationDate(), token);
    }

    private String generateResetToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private String buildResetUrl(String token) {
        String frontendUrl = frontendUrls == null || frontendUrls.isEmpty()
                ? "http://localhost:3000"
                : frontendUrls.get(0);
        return frontendUrl.replaceAll("/+$", "") + "/reset-password/" + token;
    }
}
