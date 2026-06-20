package at.fhtw.tourplanner.controller;

import at.fhtw.tourplanner.DTO.ChangePasswordRequestDTO;
import at.fhtw.tourplanner.DTO.UpdateProfileRequestDTO;
import at.fhtw.tourplanner.DTO.UserProfileResponseDTO;
import at.fhtw.tourplanner.DTO.UserRegisterRequestDTO;
import at.fhtw.tourplanner.DTO.UserRegisterResponseDTO;
import at.fhtw.tourplanner.service.UserService;
import at.fhtw.tourplanner.util.ApiResponseUtil;
import at.fhtw.tourplanner.util.CookieUtil;
import at.fhtw.tourplanner.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CookieUtil cookieUtil;

    // ─── Existing: Register ──────────────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<ApiResponseUtil.ApiResponse<UserRegisterResponseDTO>> registerUser(
            @Valid @RequestBody UserRegisterRequestDTO dto) {

        userService.registerUser(dto);

        UserRegisterResponseDTO response =
                new UserRegisterResponseDTO(dto.username(), dto.email());

        return ApiResponseUtil.success(response, "User registered successfully. You can now log in with your credentials.");
    }

    // ─── NEW: Get current user profile ───────────────────────────────────

    @GetMapping("/users/me")
    public ResponseEntity<ApiResponseUtil.ApiResponse<UserProfileResponseDTO>> getCurrentUserProfile(
            HttpServletRequest request) {

        UUID userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ApiResponseUtil.error("Unauthorized: Please log in to access your profile.", HttpStatus.UNAUTHORIZED);
        }

        UserProfileResponseDTO profile = userService.getUserProfile(userId);
        return ApiResponseUtil.success(profile, "Profile retrieved successfully");
    }

    // ─── NEW: Update current user profile (username & email) ────────────

    @PutMapping("/users/me")
    public ResponseEntity<ApiResponseUtil.ApiResponse<UserProfileResponseDTO>> updateCurrentUserProfile(
            @Valid @RequestBody UpdateProfileRequestDTO dto,
            HttpServletRequest request) {

        UUID userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ApiResponseUtil.error("Unauthorized: Please log in to update your profile.", HttpStatus.UNAUTHORIZED);
        }

        UserProfileResponseDTO updatedProfile = userService.updateUserProfile(userId, dto);
        return ApiResponseUtil.success(updatedProfile, "Profile updated successfully");
    }

    // ─── NEW: Change password ─────────────────────────────────────────────

    @PutMapping("/users/me/password")
    public ResponseEntity<ApiResponseUtil.ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO dto,
            HttpServletRequest request) {

        UUID userId = getUserIdFromRequest(request);
        if (userId == null) {
            return ApiResponseUtil.error("Unauthorized: Please log in to change your password.", HttpStatus.UNAUTHORIZED);
        }

        userService.changePassword(userId, dto);
        return ApiResponseUtil.success(null, "Password changed successfully");
    }

    // ─── Helper to extract userId from JWT via cookie ──────────────────

    private UUID getUserIdFromRequest(HttpServletRequest request) {
        String jwt = cookieUtil.getJwtFromCookies(request);
        if (jwt == null) return null;
        return jwtUtil.extractUserId(jwt);
    }
}