package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.DTO.CurrentUserDTO;
import at.fhtw.tourplanner.DTO.UserLoginRequestDTO;
import at.fhtw.tourplanner.DTO.UserLoginResponseDTO;
import at.fhtw.tourplanner.entity.UserEntity;
import at.fhtw.tourplanner.repository.UserRepository;
import at.fhtw.tourplanner.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
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
        System.out.println("User found: " + user);
        if (passwordEncoder.matches(dto.password(), user.getPasswordHash())) {
            return jwtUtil.generateToken(user.getUsername());
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
