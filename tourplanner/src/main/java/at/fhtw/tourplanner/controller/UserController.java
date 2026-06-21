package at.fhtw.tourplanner.controller;

import at.fhtw.tourplanner.DTO.*;
import at.fhtw.tourplanner.service.UserService;
import at.fhtw.tourplanner.util.ApiResponseUtil;
import at.fhtw.tourplanner.util.CookieUtil;
import at.fhtw.tourplanner.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;



@RestController
@RequestMapping("/api")
//@CrossOrigin(origins = "${app.frontend.url}")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;

    public UserController(UserService userService, JwtUtil jwtUtil, CookieUtil cookieUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.cookieUtil = cookieUtil;
    }

    private UUID getUserIdFromJwt(HttpServletRequest request) {
        String jwt = cookieUtil.getJwtFromCookies(request);
        if (jwt == null) return null;
        return jwtUtil.extractUserId(jwt);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponseUtil.ApiResponse<UserRegisterResponseDTO>> registerUser(
            @Valid @RequestBody UserRegisterRequestDTO dto) {

        userService.registerUser(dto);

        UserRegisterResponseDTO response =
                new UserRegisterResponseDTO(dto.username(), dto.email());

        return ApiResponseUtil.success(response, "User registered successfully. You can now log in with your credentials.");
    }

    @PutMapping("/users/me")
    public ResponseEntity<ApiResponseUtil.ApiResponse<CurrentUserDTO>> updateProfile(
            @Valid @RequestBody UpdateProfileRequestDTO dto,
            HttpServletRequest request) {

        UUID userId = getUserIdFromJwt(request);
        if (userId == null) {
            return ApiResponseUtil.error("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        CurrentUserDTO updated = userService.updateProfile(userId, dto);
        return ApiResponseUtil.success(updated, "Profile updated successfully");
    }

    @PutMapping("/users/me/password")
    public ResponseEntity<ApiResponseUtil.ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO dto,
            HttpServletRequest request) {

        UUID userId = getUserIdFromJwt(request);
        if (userId == null) {
            return ApiResponseUtil.error("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        userService.changePassword(userId, dto);
        return ApiResponseUtil.success(null, "Password changed successfully");
    }
}

