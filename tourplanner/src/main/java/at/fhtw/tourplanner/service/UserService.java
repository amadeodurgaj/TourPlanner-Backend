package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.DTO.UserLoginRequestDTO;
import at.fhtw.tourplanner.DTO.UserRegisterRequestDTO;
import at.fhtw.tourplanner.model.User;
import at.fhtw.tourplanner.repository.UserRepository;
import at.fhtw.tourplanner.util.DateUtils;
import at.fhtw.tourplanner.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;


@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    @Autowired
    JwtUtil jwtUtil;

    // registration function, checks are done in the dto
    public void registerUser(UserRegisterRequestDTO dto) {
        if (!dto.password().equals(dto.passwordConfirmation())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        User user = new User();
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        user.setRegistrationDate(LocalDateTime.now());
        userRepository.save(user);
    }



}