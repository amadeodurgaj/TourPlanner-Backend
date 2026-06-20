package at.fhtw.tourplanner.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequestDTO(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email
) {
}
