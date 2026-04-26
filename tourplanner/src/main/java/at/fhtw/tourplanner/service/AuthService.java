package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.DTO.CurrentUserDTO;
import at.fhtw.tourplanner.DTO.UserLoginRequestDTO;
import at.fhtw.tourplanner.entity.UserEntity;
import at.fhtw.tourplanner.repository.UserRepository;
import at.fhtw.tourplanner.util.JwtUtil;
import at.fhtw.tourplanner.util.LoggerUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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
        boolean UserExists = userRepository.existsByUsername(dto.username());
        if (!UserExists) {
            return null;
        }
        UserEntity user = userRepository.findByUsername(dto.username());
        log.debug("User found for username: {}", dto.username());
        if (passwordEncoder.matches(dto.password(), user.getPasswordHash())) {
            return jwtUtil.generateToken(user.getUsername(), user.getId());
        }
        return null;
    }

    public CurrentUserDTO getCurrentUser(String token) {
        String username = jwtUtil.extractUsername(token);
        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            return null;
        }
        return new CurrentUserDTO(user.getUsername(), token);
    }
}
