package at.fhtw.tourplanner.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserProfileResponseDTO(
        @NotNull(message = "User ID is required")
        UUID id,

        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotNull(message = "Registration date is required")
        LocalDateTime registrationDate
) {}