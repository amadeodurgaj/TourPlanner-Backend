package at.fhtw.tourplanner.DTO;

import at.fhtw.tourplanner.entity.TourLogEntity;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record TourLogRequestDTO(
    @NotNull(message = "Date/time is required")
    LocalDateTime dateTime,

    @NotBlank(message = "Comment is required")
    @Size(min = 1, max = 5000, message = "Comment must be between 1 and 5000 characters")
    String comment,

    @NotNull(message = "Difficulty is required")
    TourLogEntity.Difficulty difficulty,

    @PositiveOrZero(message = "Total distance must be >= 0")
    Double totalDistance,

    @PositiveOrZero(message = "Total time must be >= 0")
    Integer totalTime,

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    Integer rating
) {}
