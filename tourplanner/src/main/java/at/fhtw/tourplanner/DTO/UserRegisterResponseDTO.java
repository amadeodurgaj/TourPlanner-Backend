package at.fhtw.tourplanner.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public record UserRegisterResponseDTO (@NotBlank String username, @NotBlank @Email String email) {
}
