package at.fhtw.tourplanner.controller;

import at.fhtw.tourplanner.DTO.UserRegisterRequestDTO;
import at.fhtw.tourplanner.DTO.UserRegisterResponseDTO;
import at.fhtw.tourplanner.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import at.fhtw.tourplanner.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "${app.frontend.url}")
public class UserController {
    @Autowired UserService userService;

    @PostMapping("/register")
    public UserRegisterResponseDTO registerUser(@RequestBody UserRegisterRequestDTO dto) {
        userService.registerUser(dto);
        return new UserRegisterResponseDTO(dto.username(), dto.email());
    }
}



