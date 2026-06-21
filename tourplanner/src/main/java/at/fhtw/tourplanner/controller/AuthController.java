package at.fhtw.tourplanner.controller;

import at.fhtw.tourplanner.DTO.CurrentUserDTO;
import at.fhtw.tourplanner.DTO.ForgotPasswordRequestDTO;
import at.fhtw.tourplanner.DTO.ForgotPasswordResponseDTO;
import at.fhtw.tourplanner.DTO.ResetPasswordRequestDTO;
import at.fhtw.tourplanner.DTO.UserLoginRequestDTO;
import at.fhtw.tourplanner.service.AuthService;
import at.fhtw.tourplanner.util.ApiResponseUtil;
import at.fhtw.tourplanner.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    AuthService authService;

    @Autowired
    CookieUtil cookieUtil;

    @Value("${app.environment.production:false}")
    private boolean isProduction;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequestDTO dto) {
        String token = authService.loginUser(dto);

        String cookieName = isProduction ? "__Host-jwt" : "jwt";
        ResponseCookie cookie = ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(isProduction)
                .path("/")
                .maxAge(60 * 60)
                .sameSite("Strict")
                .build();

        CurrentUserDTO userInfo = authService.getCurrentUser(token);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponseUtil.success(userInfo, "Login successful"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        String cookieName = isProduction ? "__Host-jwt" : "jwt";
        ResponseCookie cookie = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(isProduction)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponseUtil.success(null, "Logout successful"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponseUtil.ApiResponse<ForgotPasswordResponseDTO>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDTO dto) {

        ForgotPasswordResponseDTO response = authService.forgotPassword(dto);
        return ApiResponseUtil.success(
                response,
                "If an account exists for that email, a password reset link has been generated."
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponseUtil.ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDTO dto) {

        authService.resetPassword(dto);
        return ApiResponseUtil.success(null, "Password reset successfully");
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        String token = cookieUtil.getJwtFromCookies(request);
        if (token == null || token.isEmpty()) {
            return ApiResponseUtil.error("Unauthorized: No authentication token provided. Please log in.", HttpStatus.UNAUTHORIZED);
        }

        CurrentUserDTO userInfo = authService.getCurrentUser(token);
        if (userInfo == null) {
            return ApiResponseUtil.error("Invalid token: The provided authentication token is invalid or expired. Please log in again.", HttpStatus.UNAUTHORIZED);
        }

        return ApiResponseUtil.success(userInfo, "User info retrieved successfully");
    }
}
