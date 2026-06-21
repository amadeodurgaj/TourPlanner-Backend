package at.fhtw.tourplanner.DTO;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.UUID;

public record CurrentUserDTO (
        @NotBlank UUID id,
        @NotBlank String username,
        @NotBlank String email,
        LocalDateTime registrationDate,
        @NotBlank String token
) {
}
