package at.fhtw.tourplanner.controller;

import at.fhtw.tourplanner.DTO.UserLoginRequestDTO;
import at.fhtw.tourplanner.DTO.UserLoginResponseDTO;
import at.fhtw.tourplanner.DTO.UserRegisterRequestDTO;
import at.fhtw.tourplanner.DTO.UserRegisterResponseDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import at.fhtw.tourplanner.service.UserService;
import at.fhtw.tourplanner.util.ApiResponseUtil;



@RestController
@RequestMapping("/api")
//@CrossOrigin(origins = "${app.frontend.url}")
public class UserController {

    @Autowired UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponseUtil.ApiResponse<UserRegisterResponseDTO>> registerUser(
            @Valid @RequestBody UserRegisterRequestDTO dto) {

        userService.registerUser(dto);


        UserRegisterResponseDTO response =
                new UserRegisterResponseDTO(dto.username(), dto.email());

        return ApiResponseUtil.success(response, "User registered successfully");
    }


}

