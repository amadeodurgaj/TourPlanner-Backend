package at.fhtw.tourplanner.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public record UserRegisterRequestDTO (@NotBlank String username, @NotBlank @Email String email, @NotBlank String password,@NotBlank String passwordConfirmation) {
}
