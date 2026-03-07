package at.fhtw.tourplanner.DTO;

import jakarta.validation.constraints.NotBlank;

public record UserLoginRequestDTO(@NotBlank String username, @NotBlank String password) {
}

