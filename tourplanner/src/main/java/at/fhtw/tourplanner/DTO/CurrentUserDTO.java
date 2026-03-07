package at.fhtw.tourplanner.DTO;

import jakarta.validation.constraints.NotBlank;

public record CurrentUserDTO (@NotBlank String username, @NotBlank String token) {
}
