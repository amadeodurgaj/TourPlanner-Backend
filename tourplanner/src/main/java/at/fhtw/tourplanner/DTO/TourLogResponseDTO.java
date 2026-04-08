package at.fhtw.tourplanner.DTO;

import java.time.LocalDateTime;
import java.util.UUID;

public record TourLogResponseDTO(
    UUID id,
    LocalDateTime dateTime,
    String comment,
    String difficulty,
    Double totalDistance,
    Integer totalTime,
    Integer rating,
    UUID tourId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
