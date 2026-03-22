package at.fhtw.tourplanner.DTO;

import jakarta.validation.constraints.NotBlank;

public record TourRequestDTO(
        @NotBlank String name,
        @NotBlank String description,
        @NotBlank String transportType,
        @NotBlank String fromLocation,
        @NotBlank String toLocation,
        double distance,
        String estimatedTime,
        Object routeInfo
) {}
