package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.DTO.CurrentUserDTO;
import at.fhtw.tourplanner.DTO.UserLoginRequestDTO;
import at.fhtw.tourplanner.entity.UserEntity;
import at.fhtw.tourplanner.repository.UserRepository;
import at.fhtw.tourplanner.util.JwtUtil;
import at.fhtw.tourplanner.util.LoggerUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final Logger log = LoggerUtil.getLogger(AuthService.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;


    @Autowired
    JwtUtil jwtUtil;

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
        return new CurrentUserDTO(user.getUsername(), token);
    }
}
