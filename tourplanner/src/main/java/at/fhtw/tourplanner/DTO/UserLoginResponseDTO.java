package at.fhtw.tourplanner.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record UserLoginResponseDTO (@NotBlank String username, @NotBlank @Email String email, @NotBlank UUID token) {
}
