package at.fhtw.tourplanner.service;

import at.fhtw.tourplanner.DTO.UserLoginRequestDTO;
import at.fhtw.tourplanner.DTO.UserRegisterRequestDTO;
import at.fhtw.tourplanner.model.User;
import at.fhtw.tourplanner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    //simple registration, no checks for existing users implemented yet
    public void registerUser(UserRegisterRequestDTO dto) {
        User user = new User();
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        userRepository.save(user);
    }

}