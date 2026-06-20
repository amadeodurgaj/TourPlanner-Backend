package at.fhtw.tourplanner.DTO;

import java.time.LocalDateTime;

public record ForgotPasswordResponseDTO(
        String resetUrl,
        LocalDateTime expiresAt
) {
}
